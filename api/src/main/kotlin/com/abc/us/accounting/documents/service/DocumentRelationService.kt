package com.abc.us.accounting.documents.service

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.entity.DocumentItem
import com.abc.us.accounting.documents.domain.entity.DocumentItemRelation
import com.abc.us.accounting.documents.domain.type.RelationType
import com.abc.us.accounting.documents.model.DocumentItemResult
import com.abc.us.accounting.documents.model.DocumentServiceContext
import com.abc.us.accounting.supports.utils.BigDecimals.toScale
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.math.BigDecimal


interface DocumentRelationServiceable {

    fun getEffectiveClearingDocumentItemRelations(context: DocumentServiceContext, refDocItemIds:List<String> ): List<DocumentItemRelation>
    fun getClearingRelationType(context: DocumentServiceContext, refDocItems:List<DocumentItemResult>, docItems:List<DocumentItemResult>, previousDocItems:List<DocumentItemResult> = listOf()): RelationType
}

@Service
class DocumentRelationService(
    private val persistenceService: DocumentPersistenceService
) : DocumentRelationServiceable {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * 반제가능여부 계산
     * 참조전표ID를 기반으로 반제 가능한 전표항목을 추출
     */
    override fun getEffectiveClearingDocumentItemRelations(context: DocumentServiceContext, refDocItemIds:List<String> ): List<DocumentItemRelation> {
        // 반제전표가 역분개 되어 반제가 가능한지 점검
//        val relations = persistenceService.findDocumentItemRelations(refDocItemIds, refDocItemIds)
//        val activeRelations = relations.filter { refDocItemIds.contains(it.docItemId) }
//        val activeRelationTypes = RelationType.entries
        val activeRelationTypes = listOf(RelationType.CLEARING, RelationType.PARTIAL_CLEARING, RelationType.REVERSING)
        val activeRelations = persistenceService.findDocumentItemRelations(refDocItemIds, listOf(), activeRelationTypes)
        if ( context.debug ) {
            logger.debug { "activeRelations:${activeRelations.size}, by refDocItemIds:${refDocItemIds.size}, relationTypes:${activeRelationTypes}" }
        }

        // 1) 참조전표항목ID는 관계를 맺는 주체가 되면 안됨
        if (activeRelations.isEmpty()) {
            return listOf()
        }

        // 2) 참조전표항목ID는 역분개(active/passive 모두) 상태면 안됨
        val passiveRelations = persistenceService.findDocumentItemRelations(listOf(), refDocItemIds, RelationType.entries )
        if ( context.debug ) {
            logger.debug { "passiveRelations:${passiveRelations.size} by refDocItemIds:${refDocItemIds.size}, relationTypes:${activeRelationTypes}" }
        }
        val reversalRelations = passiveRelations.filter { it.relationType == RelationType.REVERSING }
        // TODO: reversal일때 처리방안 고민 필요
        // 이미 역분개된 전표임. 추가 수정은 불가함
        require(reversalRelations.isEmpty()) { "Already reversed document item ids:${reversalRelations.map { it.docItemId }}" }


        // 3) 참조전표항목ID에 반제한 전표항목(clearing, active)이 역분개(reversing, active/passive) 되면 반제가능여부 계산에서 제외해야 함
        // 3-1) 참조전표항목ID에 반제한 전표항목(clearing/partial clearing, active)한 항목을 추출
//        val candidateDocItems = relations.filter { it.relationType == RelationType.CLEARING || it.relationType == RelationType.PARTIAL_CLEARING }
        val candidateRelations = passiveRelations.filter { it.relationType == RelationType.CLEARING || it.relationType == RelationType.PARTIAL_CLEARING }
        val candidateDocItemIds = candidateRelations.map { it.docItemId }

        val reversingRelations = persistenceService.findDocumentItemRelations(listOf(), candidateDocItemIds, listOf(RelationType.REVERSING) )
        val reversedDocItmIds = reversingRelations.map { it.refDocItemId }

        // 반제여부 확인
        val filteredDocItems = activeRelations.filter { it -> !reversedDocItmIds.contains(it.docItemId) }

        // 3-2) 이미 반제된 것은 있으면 안됨
        val alreadyClearedDocItems =  filteredDocItems.filter { it.relationType == RelationType.CLEARING }
        require(alreadyClearedDocItems.isEmpty()) { "Already cleared document item ids:${alreadyClearedDocItems.map { it.docItemId }}" }

        // 3-3) 반제계산시 참고할 만한 documentItemRelations 만 추출
        return filteredDocItems
    }


    /**
     * 반제유형 판단
     * : 완전반제, 부분반제 여부를 판단
     * 1) 완전반제: 반제대상 항목의 잔액이 0이 되는 경우 (기존에 부분반제되었다면, 남은 금액이 0이 되는지 판단)
     * 2) 부분반제: 반제대상 항목의 잔액이 남아있는 경우
     */
    fun calculationClearingRelationType (context: DocumentServiceContext,
                                         refDocItems: List<DocumentItem>,
                                         docItems: List<DocumentItem>,
                                         pcDocItems: List<DocumentItem>): RelationType {
        var relationType:RelationType = RelationType.PARTIAL_CLEARING

        try {
//            val refDocItemIds = refDocItems.map { it.id }
//            val effectiveClearingDocItemRelations = getEffectiveClearingDocumentItemRelations(context, refDocItemIds)
//            if ( effectiveClearingDocItemRelations.isNotEmpty() ) {
//                relationType = RelationType.PARTIAL_CLEARING
//                return relationType
//            }

            val refAccountCodes = refDocItems.map { it.accountCode }.distinct()

            for (refAccountCode in refAccountCodes) {
                val refDocItemsByAccountCode = refDocItems.filter { it.accountCode == refAccountCode }
                val docItemsByAccountCode = docItems.filter { it.accountCode == refAccountCode }

                val pcDocItemsByAccountCode = pcDocItems.filter { it.accountCode == refAccountCode }


                val refTxAmount = refDocItemsByAccountCode.sumOf { it.txMoney.amount }.setScale(Constants.ACCOUNTING_SCALE)
                val pcTxAmount = pcDocItemsByAccountCode.sumOf { it.txMoney.amount }.setScale(Constants.ACCOUNTING_SCALE)
                val docTxAmount = docItemsByAccountCode.sumOf { it.txMoney.amount }.setScale(Constants.ACCOUNTING_SCALE)

                val remainingTxAmount = refTxAmount.minus(pcTxAmount)
                require(remainingTxAmount >= docTxAmount ) { "refTxAmount:$refTxAmount, pcTxAmount: $pcTxAmount, docTxAmount:$docTxAmount" +
                        ", refDocItemsByAccountCode:${refDocItemsByAccountCode.map { it.id }}" +
                        ", pcDocItemsByAccountCode:${pcDocItemsByAccountCode.map { it.id }}" +
                        ", docItemsByAccountCode:${docItemsByAccountCode.map { it.id }}" }

                if (remainingTxAmount == docTxAmount) {
                    relationType = RelationType.CLEARING
                } else if (docTxAmount > BigDecimal.ZERO) {
                    relationType = RelationType.PARTIAL_CLEARING
                }

                if (context.debug) {
                    logger.debug { "relationType:$relationType, remainingTxAmount:$remainingTxAmount, docTxAmount:$docTxAmount, refTxAmount:$refTxAmount, pcTxAmount: $pcTxAmount" +
                            ", refDocItemsByAccountCode:${refDocItemsByAccountCode.map { it.id }}" +
                            ", pcDocItemsByAccountCode:${pcDocItemsByAccountCode.map { it.id }}" +
                            ", docItemsByAccountCode:${docItemsByAccountCode.map { it.id }}" }
                }

                if (relationType == RelationType.CLEARING) {
                    return relationType
                }
            }
            return relationType

        }finally {
            logger.info { "relationType:$relationType, refDocItems:${refDocItems.map { it.id }}, docItems:${docItems.map { it.id }}, pcDocItems:${pcDocItems.map { it.id }}" }
        }
    }

    override fun getClearingRelationType(context: DocumentServiceContext, refDocItemResults:List<DocumentItemResult>,
                                         docItemResults:List<DocumentItemResult>,
                                         pcDocItemResults:List<DocumentItemResult> ): RelationType{
//        logger.debug { "refDocItems:${refDocItemResults.map { it.docItemId }}, docItems:${docItemResults.map { it.docItemId }}, pcDocItems:${pcDocItemResults.map { it.docItemId }}" }
        var relationType:RelationType = RelationType.PARTIAL_CLEARING

        try {
//            val refDocItemIds = refDocItemResults.map { it.docItemId }
//            val effectiveClearingDocItemRelations = getEffectiveClearingDocumentItemRelations(context, refDocItemIds)
//            if ( effectiveClearingDocItemRelations.isNotEmpty() ) {
//                return relationType
//            }

            val refAccountCodes = refDocItemResults.map { it.accountCode }.distinct()
            for (refAccountCode in refAccountCodes) {
                val refDocItemsByAccountCode = refDocItemResults.filter { it.accountCode == refAccountCode }
                val docItemsByAccountCode = docItemResults.filter { it.accountCode == refAccountCode }

                val pcDocItemsByAccountCode = pcDocItemResults.filter { it.accountCode == refAccountCode }

                val refTxAmount = refDocItemsByAccountCode.sumOf { it.txAmount }.toScale()
                val pcTxAmount = pcDocItemsByAccountCode.sumOf { it.txAmount }.toScale()
                val docTxAmount = docItemsByAccountCode.sumOf { it.txAmount }.toScale()

                val remainingTxAmount = refTxAmount - pcTxAmount
                require(remainingTxAmount >= docTxAmount ) { "refTxAmount:$refTxAmount, pcTxAmount: $pcTxAmount, docTxAmount:$docTxAmount" +
                        ", refDocItemsByAccountCode:${refDocItemsByAccountCode.map { it.docItemId }}" +
                        ", pcDocItemsByAccountCode:${pcDocItemsByAccountCode.map { it.docItemId }}" +
                        ", docItemsByAccountCode:${docItemsByAccountCode.map { it.docItemId }}" }

                if (remainingTxAmount == docTxAmount) {
                    relationType = RelationType.CLEARING
                    return relationType
                }
            }
            return relationType
        }finally {
            logger.info { "relationType:$relationType, refDocItems:${refDocItemResults.map { it.docItemId }}, docItems:${docItemResults.map { it.docItemId }}, pcDocItems:${pcDocItemResults.map { it.docItemId }}" }
        }
    }
}