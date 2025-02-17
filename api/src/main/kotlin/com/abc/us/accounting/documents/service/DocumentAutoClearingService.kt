package com.abc.us.accounting.documents.service

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.type.DocumentAttributeType
import com.abc.us.accounting.documents.model.*
import mu.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class DocumentAutoClearingService(
    private val documentTemplateService: DocumentTemplateService,
    private val documentServiceable: DocumentServiceable
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    // 실시간 잔액 업데이트 (한개만 돌아야 함 - 동시성 문제)
    @Async
    @EventListener
    @Transactional
    fun handleClearingEvent(event: ClearingEvent) {
        when (event) {
            is ClearingEvent.PostClearing -> {
                postClearing(event.context, event.requests)
            }
        }
    }

    /**
     * 전표생성후 자동반제처리
     */
    @Transactional
    fun postClearing(context:DocumentServiceContext, requests: List<DocumentResult>):List<DocumentResult> {
        logger.info("postClearing:request size: ${requests.size} by context:$context")
        if ( requests.isEmpty() ) {
            logger.warn("No requests found by postClearing, skipped!!")
            return emptyList()
        }
        if ( requests.size > Constants.DOCUMENT_BATCH_SIZE ) {
            logger.warn("Too many requests found by postClearing, requests:${requests.size}")
            if ( context.enableBatchLimit ) {
                throw IllegalArgumentException("Too many requests found by postClearing, requests:${requests.size}")
            }
        }
        val companyCodes = requests.map { it.companyCode }.distinct()
        require(companyCodes.size == 1) { "companyCodes must be same, companyCodes:${companyCodes}" }
        val companyCode = companyCodes.first()

        // 전표 반제를 위한 전표 템플릿코드 확인
        val clearingPairs = documentTemplateService.findDocTemplateItemPairsForClearing(companyCode)
        val refTemplateKeys = clearingPairs.map { it.second.docTemplateKey }.distinct()
        val refTemplates = documentTemplateService.findDocTemplates(refTemplateKeys)

        // 전표 반제를 위한 DocItems 추출
        val clearingDocItems = requests.flatMap { it.docItems }.filter { item ->
            clearingPairs.any {
                it.first.docTemplateKey == item.toDocTemplateKey()
                        && it.first.accountCode == item.accountCode
                        && it.first.accountSide == item.accountSide
            }
        }

        val lookupRefItems = requests.map { document ->
            val curClearingDocItems = clearingDocItems.filter { it.docId == document.docId }

            val lookupRefDocItems = curClearingDocItems.map { item ->
                val refTemplateKey = clearingPairs.find {
                    it.first.docTemplateKey == item.toDocTemplateKey() &&
                    it.first.accountCode == item.accountCode &&
                    it.first.accountSide == item.accountSide
                }?.second?.docTemplateKey
                val refTemplate = refTemplates.firstOrNull { it.docTemplateKey == refTemplateKey }
                require (refTemplate != null) { "RefTemplate not found: $refTemplateKey" }
                require (document.companyCode == refTemplate.docTemplateKey.companyCode) { "CompanyCode not matched: ${document.companyCode} != ${refTemplate.docTemplateKey.companyCode}" }

                LookupRefDocItemRequest(
                    docType = refTemplate.documentType,
                    companyCode = document.companyCode,
                    docTemplateCode = refTemplateKey!!.docTemplateCode,
                    accountCode = item.accountCode,
                    accountSide = item.accountSide.reverse(),
                    customerId = item.customerId,
                    vendorId = item.vendorId,
                    orderItemId = item.attributes.filter { it.type == DocumentAttributeType.ORDER_ITEM_ID }.map { it.value }.firstOrNull(),
                )
            }
            lookupRefDocItems
        }.flatten()

        val refDocItemResults:List<RefDocItemResult> = documentServiceable.lookupRefDocItems(context, lookupRefItems)
        logger.info("found refDocItems: ${refDocItemResults.size} by lookupRefDocItems: ${lookupRefItems.size}")
        if ( refDocItemResults.isEmpty() ) {
            logger.warn("No refDocItemResults found by lookupRefItems: ${lookupRefItems.size}, requests: ${requests.size}, clearing skipped!!")
            return emptyList()
        }

        val clearingRequests = requests
            .filter { document -> clearingDocItems.any {it.docId == document.docId } }
            .map { document ->
                val curClearingDocItems = clearingDocItems.filter { it.docId == document.docId}

                val curRefDocItems = refDocItemResults.asSequence().filter { document.companyCode == it.companyCode }
                    .filter { curClearingDocItems.map { it.accountCode }.contains(it.accountCode) }
                    .filter { curClearingDocItems.map { it.accountSide }.contains(it.accountSide.reverse()) }
                    .filter { (it.customerId != null) || (it.vendorId!= null) }
                    .filter {  dto -> (dto.orderItemId == null) ||
                            curClearingDocItems.any { it.attributes.filter { it.type == DocumentAttributeType.ORDER_ITEM_ID }.map { it.value }.contains(dto.orderItemId) }
                    }.filter { refDocItem ->
                        val customerCheck = curClearingDocItems.mapNotNull { it.customerId }.contains(refDocItem.customerId)
                        val vendorCheck = curClearingDocItems.mapNotNull { it.vendorId }.contains(refDocItem.vendorId)
                        customerCheck || vendorCheck
                    }.toList()

                val clearingRequest = ClearingDocumentRequest(
                    docType = document.docType,
                    docHash = document.docHash,
                    postingDate = document.postingDate,
                    documentDate = document.documentDate,

                    companyCode = document.companyCode,
                    txCurrency = document.txCurrency,
                    reference = document.reference,
                    text = document.text,
                    createTime = document.createTime,
                    createdBy = document.createdBy,
                    docOrigin = document.docOrigin!!.toRequest(),
                    refDocItemIds = curRefDocItems.map { it.docItemId },
                )
                clearingRequest.docItems.addAll( document.docItems.map { it.toRequest() } )

                clearingRequest
            }.filter { it.refDocItemIds.isNotEmpty() }

        val results = documentServiceable.clearing(context, clearingRequests)

        return results
    }


    /**
     * 자동반제 배치
     * 1) 반제대상 전표 조회: lookupForAutoClearing
     * 2) 반제대상 전표 반제처리: postClearing
     */
    @Transactional
    fun processAutoClearing (context: DocumentServiceContext, companyCode: CompanyCode, startTime: OffsetDateTime, endTime: OffsetDateTime = OffsetDateTime.now()):List<DocumentResult> {
        require(startTime.isBefore(endTime)) { "startTime must be before endTime, startTime:${startTime}, endTime:${endTime}" }
        logger.info { "processAutoClearing: context:$context, companyCode:$companyCode, $startTime ~ $endTime" }

        val candidates = documentServiceable.lookupForClearing(context, companyCode, startTime, endTime)

        if ( context.enableBatchLimit ){
            val results = candidates
                .chunked(Constants.DOCUMENT_BATCH_SIZE).map { chunk ->
                    postClearing(context, chunk)
                }.flatten()
            return results
        }
        return postClearing(context, candidates)
    }
}


