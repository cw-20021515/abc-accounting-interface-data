package com.abc.us.accounting.documents.service

import com.abc.us.accounting.documents.domain.entity.ApprovalRuleCondition
import com.abc.us.accounting.documents.domain.entity.DocumentApprovalRule
import com.abc.us.accounting.documents.domain.repository.DocumentApprovalRuleRepository
import com.abc.us.accounting.documents.domain.type.BizSystemType
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.type.DocumentTemplateCode
import com.abc.us.accounting.documents.domain.type.DocumentType
import com.abc.us.accounting.documents.fixtures.CreateDocumentRequestFixture
import com.abc.us.accounting.documents.fixtures.TestDocumentTemplateMapping
import com.abc.us.accounting.documents.model.DocumentServiceContext
import com.abc.us.accounting.supports.utils.Range
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal


@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class BasicDocumentApprovalRuleServiceTests  (
    private val documentService: DocumentService,
    private val ruleService: DocumentApprovalRuleService,
    private val ruleRepository: DocumentApprovalRuleRepository,
): AnnotationSpec(){

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    @AfterEach
    fun cleanup() {
        logger.info("Cleaning up documents applied to database")
        ruleService.cleanup()
    }

    val companyCode = CompanyCode.T200



    @Test
    fun `basic approval rule repository test`() {
        val rules = generateRules()
        logger.info("rules: $rules")

        run {
            ruleRepository.save(rules[0])
            val found = ruleRepository.findAllByIsActiveOrderByPriorityDesc(true)
            found shouldHaveSize 1
        }
        run {
            ruleRepository.save(rules[1])
            val found = ruleRepository.findAllByIsActiveOrderByPriorityDesc(true)
            found shouldHaveSize 2
        }
        run {
            ruleRepository.save(rules[2])
            val found = ruleRepository.findAllByIsActiveOrderByPriorityDesc(true)
            found shouldHaveSize 3
        }

        run {
            ruleRepository.save(rules[3])
            val found = ruleRepository.findAllByIsActiveOrderByPriorityDesc(true)
            found shouldHaveSize 3
        }

    }


    @Test
    fun `basic approval rule service test`() {
        val rules = generateRules()

        run {
            ruleService.createRule(rules[0])
            val list = ruleService.listRules()
            list.size shouldBe 1
            list[0].name shouldBe "test1"
        }

        run {
            ruleService.createRule(rules[1])
            val list = ruleService.listRules()
            list.size shouldBe 2
            list[0].name shouldBe "test2"
        }

        run {
            ruleService.createRule(rules[2])
            val list = ruleService.listRules()
            list.size shouldBe 3
            list[0].name shouldBe "test2"
        }

        run {
            ruleService.createRule(rules[3])
            val list = ruleService.listRules()
            list.size shouldBe 3
            list[0].name shouldBe "test2"
        }
    }


    @Test
    fun `basic posting test - amount rule` () {
        val context = DocumentServiceContext.ONLY_DEBUG
        val rules = listOf(
            DocumentApprovalRule(
                name = "test1",
                description = "test1",
                companyCode = CompanyCode.T200,
                priority = 30,
                conditions = listOf(
                    ApprovalRuleCondition(
//                        bizSystemTypes = listOf(BizSystemType.ABC_ADMIN.name),
                        amountRange = Range(BigDecimal(2000)),
                    ),
                ),
                requiresApproval = true,
                isActive = true
            )
        )
        ruleService.createRules(rules)


        // 정상 포스팅
        run {
            val request = CreateDocumentRequestFixture.generateByTemplate(DocumentType.ACCOUNTING_DOCUMENT,
                TestDocumentTemplateMapping.FINANCIAL_LEASE_FILTER_SHIPPED, companyCode= CompanyCode.T200)

            val results = documentService.posting(context, listOf(request))
            logger.info("Posting result: $results")

            results.size shouldBe 1
            results.forEach { result ->
                result.docType shouldBe DocumentType.ACCOUNTING_DOCUMENT
                result.docId shouldContain DocumentType.ACCOUNTING_DOCUMENT.code
            }
        }

        // 검증오류 발생
        run {
            val request = CreateDocumentRequestFixture.generateByTemplate(
                DocumentType.ACCOUNTING_DOCUMENT,
                TestDocumentTemplateMapping.FINANCIAL_LEASE_FILTER_SHIPPED,
                totalAmount = BigDecimal(5000),
                companyCode= CompanyCode.T200)


            val exception1 = shouldThrow<IllegalStateException> {
                documentService.posting(context, listOf(request))
            }
            exception1.message shouldContain "requires to approval by rule"
        }
    }



    fun generateRules():List<DocumentApprovalRule>{
        val rule1 = DocumentApprovalRule(
            name = "test1",
            description = "test1",
            companyCode = CompanyCode.T200,
            priority = 30,
            conditions = listOf(
                ApprovalRuleCondition(
                    bizSystemTypes = listOf(BizSystemType.ABC_ADMIN.name),
                    amountRange = Range(BigDecimal(1_000)),
                ),
            ),
            requiresApproval = true,
            isActive = true
        )

        val rule2 = DocumentApprovalRule(
            name = "test2",
            description = "test2",
            companyCode = CompanyCode.T200,
            priority = 50,
            conditions = listOf(
                ApprovalRuleCondition(
                    bizSystemTypes = listOf(BizSystemType.ONETIME.name),
                    docTemplateCodes = listOf(DocumentTemplateCode.ONETIME_PRODUCT_SHIPPED.name),
                    amountRange = Range(BigDecimal(1_000), BigDecimal(Int.MAX_VALUE)),
                    accountCodeRange = Range("1100000", "1200000"),
                ),
                ApprovalRuleCondition(
                    bizSystemTypes = listOf(BizSystemType.ABC_ACCOUNTING.name),
                    costCenters = listOf("C001", "C002", "C003"),
                )
            ),
            requiresApproval = true,
            isActive = true
        )

        val rule3 = DocumentApprovalRule(
            name = "test3",
            description = "test3",
            companyCode = CompanyCode.T200,
            priority = 10,
            conditions = listOf(
                ApprovalRuleCondition(
                    bizSystemTypes = listOf(BizSystemType.ONETIME.name),
                    docTemplateCodes = listOf(DocumentTemplateCode.ONETIME_PRODUCT_SHIPPED.name),
                )
            ),
            requiresApproval = true,
            isActive = true
        )

        val rule4 = DocumentApprovalRule(
            name = "test4",
            description = "test4",
            companyCode = CompanyCode.T300,
            priority = 20,
            conditions = listOf(
                ApprovalRuleCondition(
                    bizSystemTypes = listOf(BizSystemType.ONETIME.name),
                    docTemplateCodes = listOf(DocumentTemplateCode.ONETIME_PRODUCT_SHIPPED.name),
                )
            ),
            requiresApproval = true,
            isActive = false
        )

        return listOf(rule1, rule2, rule3, rule4)
    }

    fun documentRule():List<DocumentApprovalRule>{
        val rule1 = DocumentApprovalRule(
            name = "test1",
            description = "test1",
            companyCode = CompanyCode.T100,
            priority = 30,
            conditions = listOf(
                ApprovalRuleCondition(
                    bizSystemTypes = listOf(BizSystemType.ABC_ADMIN.name),
                    amountRange = Range(BigDecimal(1_000)),
                ),
            ),
            requiresApproval = true,
            isActive = true
        )

        return listOf(rule1)
    }
}