package com.abc.us.accounting.documents.domain.repository

import com.abc.us.accounting.documents.domain.entity.DocumentTemplateKey
import com.abc.us.accounting.documents.domain.type.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles


@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class BasicDocumentTemplateRepositoryTests(
    private val documentTemplateRepository: DocumentTemplateRepository,
    private val documentTemplateItemRepository: DocumentTemplateItemRepository,
) : FunSpec({
    val companyCode = CompanyCode.T200
//
//    test("document template repository") {
//        val data = documentTemplateRepository.findAll().filter { it.docTemplateKey.companyCode == companyCode }
//        logger.info("data size:${data.size}")
//        data.size shouldBe 90
//        val docTemplateCode = DocumentTemplateCode.ONETIME_PRODUCT_SHIPPED
//        val docTemplateKey = DocumentTemplateKey.of(companyCode, docTemplateCode)
//        val docTemplate = documentTemplateRepository.findById(docTemplateKey).get()
//        docTemplate.docTemplateKey shouldBe docTemplateKey
//        docTemplate.documentType shouldBe DocumentType.GOODS_ISSUE
//        docTemplate.bizCategory shouldBe BizCategory.CUSTOMER
//        docTemplate.bizSystem shouldBe BizSystemType.ONETIME
//        docTemplate.bizProcess shouldBe BizProcessType.ORDER
//        docTemplate.bizEvent shouldBe BizEventType.PRODUCT_SHIPPED
//        docTemplate.korText shouldBe "[일시불] 제품출고"
//        docTemplate.isActive shouldBe true
//    }
//
//
//    test("document template item repository") {
//        val data = documentTemplateItemRepository.findAll().filter { it.docTemplateKey.companyCode == companyCode }
//        data.size shouldBe 146
//
//        val docTemplateCode = DocumentTemplateCode.ONETIME_PRODUCT_SHIPPED
//        val docTemplateKey = DocumentTemplateKey.of(companyCode, docTemplateCode)
//        val docTemplateItems = documentTemplateItemRepository.findByDocTemplateKey(docTemplateKey)
//
//        docTemplateItems.size shouldBe 2
//        docTemplateItems[0].lineNumber shouldBe 1
//        docTemplateItems[0].docTemplateKey shouldBe docTemplateKey
//        docTemplateItems[0].accountCode shouldBe "1230010"
//        docTemplateItems[0].accountSide shouldBe AccountSide.DEBIT
//
//        docTemplateItems[1].lineNumber shouldBe 2
//        docTemplateItems[1].docTemplateKey shouldBe docTemplateKey
//        docTemplateItems[1].accountCode shouldBe "1201010"
//        docTemplateItems[1].accountSide shouldBe AccountSide.CREDIT
//    }
}) {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}
