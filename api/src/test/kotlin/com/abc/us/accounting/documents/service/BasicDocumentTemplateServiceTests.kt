package com.abc.us.accounting.documents.service

import com.abc.us.accounting.documents.domain.entity.DocumentTemplateItem
import com.abc.us.accounting.documents.domain.entity.DocumentTemplateKey
import com.abc.us.accounting.documents.domain.repository.DocumentTemplateItemRepository
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.type.DocumentTemplateCode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class BasicDocumentTemplateServiceTests (
    private val documentTemplateItemRepository: DocumentTemplateItemRepository,
    val documentTemplateService: DocumentTemplateService
): FunSpec({
    val logger = LoggerFactory.getLogger(this::class.java)

    val companyCode = CompanyCode.T200


    test("[템플릿코드] 반제대상 템플릿코드 매핑") {
        val cachedDocumentTemplateItemMap :MutableMap<DocumentTemplateKey, List<DocumentTemplateItem> > = mutableMapOf()
        val allTemplateItemData = documentTemplateItemRepository.findAllByCompanyCode(companyCode)
        val filteredTemplateItemData = allTemplateItemData.filter { it.docTemplateKey.companyCode == companyCode }

        cachedDocumentTemplateItemMap.putAll(filteredTemplateItemData.groupBy { it.docTemplateKey })

        // clearing 대상 template item 을 찾는다.
        val clearing = allTemplateItemData
            .filter { it.refDocTemplateCode != null }

        logger.debug("clearing size: ${clearing.size}")
        clearing.forEach {
            logger.debug("clearing: $it")
        }

        clearing.size shouldBe 8

        // cleared 대상 template item 을 찾는다.
        val cleared = cachedDocumentTemplateItemMap.values.flatten()
            .filter { item -> clearing.any{
                it.accountCode == item.accountCode
                        && it.docTemplateKey.companyCode == item.docTemplateKey.companyCode
                        && it.refDocTemplateCode == item.docTemplateKey.docTemplateCode
                        && it.accountSide != item.accountSide
            } }

        logger.debug("cleared size: ${cleared.size}")
        cleared.forEach {
            logger.debug("cleared: $it")
        }

        cleared.size shouldBe 8

        val pairs = clearing.mapNotNull { clearingItem ->
            val clearedItem = cleared.firstOrNull {
                    clearingItem.accountCode == it.accountCode
                    && clearingItem.docTemplateKey.companyCode == it.docTemplateKey.companyCode
                    && clearingItem.refDocTemplateCode == it.docTemplateKey.docTemplateCode
                    && clearingItem.accountSide != it.accountSide
            }
            if (clearedItem != null) {
                Pair(clearingItem, clearedItem)
            } else {
                null
            }
        }.sortedBy { it.first.id }

        logger.debug("map size: ${pairs.size}")
        pairs.forEach{ logger.debug("pair: {}", it) }

        pairs.size shouldBe 8
        pairs.forEach {
            it.first.accountCode shouldBe it.second.accountCode
            it.first.accountSide shouldNotBe it.second.accountSide
            it.first.docTemplateKey.companyCode shouldBe it.second.docTemplateKey.companyCode
            it.first.refDocTemplateCode shouldBe it.second.docTemplateKey.docTemplateCode
        }
        pairs.first().first.docTemplateKey.docTemplateCode shouldBe DocumentTemplateCode.ONETIME_PAYMENT_DEPOSIT
        pairs.first().second.docTemplateKey.docTemplateCode shouldBe DocumentTemplateCode.ONETIME_PAYMENT_RECEIVED
    }

    test("[템플릿코드] 반제대상 템플릿코드 매핑 조회 - repository") {
        val docTemplateKeys = DocumentTemplateCode.entries.map { DocumentTemplateKey (companyCode, it) }
        val pairs = documentTemplateItemRepository.findDocTemplateItemPairsForClearing(docTemplateKeys)
            .sortedBy { it.first.id }
        logger.debug("map size: ${pairs.size}")
        pairs.forEach{ logger.debug("pair: {}", it) }

        pairs.size shouldBe 8
        pairs.forEach {
            it.first.accountCode shouldBe it.second.accountCode
            it.first.accountSide shouldNotBe it.second.accountSide
            it.first.docTemplateKey.companyCode shouldBe it.second.docTemplateKey.companyCode
            it.first.refDocTemplateCode  shouldBe it.second.docTemplateKey.docTemplateCode
        }
        pairs.first().first.docTemplateKey.docTemplateCode shouldBe DocumentTemplateCode.ONETIME_PAYMENT_DEPOSIT
        pairs.first().second.docTemplateKey.docTemplateCode shouldBe DocumentTemplateCode.ONETIME_PAYMENT_RECEIVED
    }
})