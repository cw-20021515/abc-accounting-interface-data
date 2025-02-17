package com.abc.us.accounting.documents.fixtures

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.config.SpringContext
import com.abc.us.accounting.documents.domain.entity.AccountKey
import com.abc.us.accounting.documents.domain.entity.AccountBalanceRecord
import com.abc.us.accounting.documents.domain.type.BalanceRecordType
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.model.OpeningBalanceRequest
import com.abc.us.accounting.documents.service.AccountServiceable
import com.abc.us.accounting.supports.utils.BigDecimals.toScale
import com.abc.us.accounting.supports.utils.IdGenerator
import com.github.javafaker.Faker
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

object AccountModelsFixture {
    private val faker = Faker()
    val START_DATE: LocalDate = LocalDate.of(2024, 10, 1)
    val END_DATE: LocalDate = LocalDate.of(2030, 12, 31)

    private val logger = LoggerFactory.getLogger(this::class.java)

    private var accountService: AccountServiceable
    init {
        // AccountRepository 빈을 직접 주입
        SpringContext.getBean(AccountServiceable::class.java).let {
            accountService = it
        }
    }

    fun randomOpeningBalanceRequest(): OpeningBalanceRequest {
        return randomOpeningBalanceRequests(1).first()
    }

    fun randomOpeningBalanceRequests(count: Int, companyCode: CompanyCode? = null): List<OpeningBalanceRequest> {

        val testCompanyCode = companyCode ?: Constants.TEST_COMPANY_CODE
        val accounts = TestDocumentTemplateMapping.entries.map { it.getAccountInfos(testCompanyCode).map { it.code } }.flatten().distinct()
        val shuffled = accounts.shuffled()
        val adjustedCount = if (count > shuffled.size || count <= 0) shuffled.size else count

        return (1..adjustedCount).map {
            OpeningBalanceRequest(
                companyCode = testCompanyCode,
                accountCode = shuffled[it % shuffled.size],
                amount = BigDecimal(faker.number().numberBetween(0, 10000)).toScale()
            )
        }
    }

    fun randomOpeningBalanceRecord(count: Int, recordType: BalanceRecordType?=null, localDate: LocalDate ? = null): List<AccountBalanceRecord> {
        val recordType = recordType ?: BalanceRecordType.OPENING_BALANCE
        val requests = randomOpeningBalanceRequests(count)
        val localDate = localDate ?: LocalDate.now()
        return requests.map { request ->
            openingAccountBalanceRecord(recordType, request, localDate)
        }
    }


    fun openingAccountBalanceRecord(recordType: BalanceRecordType, request: OpeningBalanceRequest, localDate: LocalDate = LocalDate.now()): AccountBalanceRecord {
        val account = accountService.getAccount(request.companyCode, request.accountCode)
        val accountNature = account.accountClass.natualAccountSide
        val changeAmount = request.amount
        val balanceAfterChange = changeAmount
        val accumulatedDebitAfterChange = accountNature.debitAmount(changeAmount, accountNature)
        val accumulatedCreditAfterChange = accountNature.creditAmount(changeAmount, accountNature)

        return AccountBalanceRecord(
            id = IdGenerator.generateNumericId(),
            accountKey = AccountKey(
                companyCode = request.companyCode,
                accountCode = request.accountCode
            ),
            accountNature = accountNature,
            docItemId = recordType.name,
            documentDate = localDate,
            postingDate = localDate,
            entryDate = localDate,
            recordType = recordType,
            changeAmount = changeAmount,
            balanceAfterChange = balanceAfterChange,
            accumulatedDebitAfterChange = accumulatedDebitAfterChange,
            accumulatedCreditAfterChange = accumulatedCreditAfterChange,
            recordTime = OffsetDateTime.now()
        )
    }
}