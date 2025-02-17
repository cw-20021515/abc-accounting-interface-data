package com.abc.us.accounting.documents.domain.repository

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.entity.AccountKey
import com.abc.us.accounting.documents.domain.type.AccountType
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.type.SystemSourceType
import com.abc.us.accounting.supports.utils.StringRange
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class BasicAccountRepositoryTests(
    private val accountRepository: AccountRepository,
) : AnnotationSpec(){

    val companyCode = Constants.TEST_COMPANY_CODE

    @Test
    fun `account repository by ABC`() {
        val data = accountRepository.findBySystemSource(companyCode, SystemSourceType.ABC)
        logger.info("data size:${data.size}")
        val account = accountRepository.findById(AccountKey.of(companyCode, "4113010"));

        account.get().accountClass shouldBe com.abc.us.accounting.documents.domain.type.AccountClass.REVENUE
        account.get().description shouldBe "렌탈료매출-재화"
        account.get().name shouldBe "Sales - Rental"
        account.get().accountType shouldBe AccountType.SALES
        account.get().isActive shouldBe true
        account.get().isOpenItemMgmt shouldBe false
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}
