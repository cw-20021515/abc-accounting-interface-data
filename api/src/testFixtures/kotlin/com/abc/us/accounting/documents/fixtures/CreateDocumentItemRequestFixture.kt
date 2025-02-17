package com.abc.us.accounting.documents.fixtures

import com.abc.us.accounting.config.SpringContext
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.type.AccountSide
import com.abc.us.accounting.documents.model.DocumentItemRequest
import com.abc.us.accounting.documents.service.AccountServiceable
import com.abc.us.accounting.documents.service.CompanyServiceable
import com.github.javafaker.Faker
import java.math.BigDecimal
import java.util.*

object CreateDocumentItemRequestFixture {
    private val faker = Faker()

    private var accountService: AccountServiceable
    private var companyService: CompanyServiceable

    init {
        // AccountRepository 빈을 직접 주입
        SpringContext.getBean(AccountServiceable::class.java).let {
            accountService = it
        }
        SpringContext.getBean(CompanyServiceable::class.java).let {
            companyService = it
        }
    }

    fun generates(
        count: Int,
        companyCode: CompanyCode? = null,
        accountCode: String? = null,
        accountSide: AccountSide? = null,
        txCurrency: String? = null,
        txAmount: BigDecimal? = null,
        text: String? = null,
        costCenter: String? = null,
        profitCenter: String? = null,
        segment: String? = null,
        project: String? = null,
        customerId: String? = null,
        vendorId: String? = null,
    ): List<DocumentItemRequest> {
        return (1..count).map {
            generate(
                companyCode = companyCode ?: CompanyCode.randomSalesCompany(),
                accountCode = accountCode ?: DocumentItemFixture.accountCodeList(companyCode!!).random(),
                accountSide = accountSide ?: AccountSide.entries.random(),
                txCurrency = txCurrency ?: DocumentItemFixture.currencies.random(),
                txAmount = txAmount ?: BigDecimal(faker.commerce().price(10.0, 1000.0)),
                text = text ?: faker.lorem().sentence(),
            )
        }
    }

    fun generate(
        companyCode: CompanyCode = CompanyCode.randomSalesCompany(),
        accountCode: String,
        accountSide: AccountSide = AccountSide.entries.random(),
        txCurrency: String? = null,
        txAmount: BigDecimal = BigDecimal(faker.commerce().price(10.0, 1000.0)),
        text: String = faker.lorem().sentence(),
        costCenter: String = faker.commerce().department(),
        profitCenter: String? = faker.commerce().department(),
        segment: String? = null,
        project: String? = null,
        customerId: String? = null,
        vendorId: String? = null
    ): DocumentItemRequest {

        val account = accountService.getAccount(companyCode, accountCode)
        val adjustedCustomerId = if ( account.accountType.customerIdRequired() ) {
            customerId ?: UUID.randomUUID().toString()
        } else {
            customerId
        }
        val adjustedVendorId = if ( account.accountType.vendorIdRequired() ) {
            vendorId ?: UUID.randomUUID().toString()
        } else {
            vendorId
        }
        return DocumentItemRequest(
            companyCode = companyCode,
            accountCode = accountCode,
            accountSide = accountSide,
            txCurrency = txCurrency ?: companyService.getCompanyCurrency(companyCode).name,
            txAmount = txAmount,
            text = text,
            costCenter = costCenter,
            profitCenter = profitCenter,
            segment = segment,
            project = project,
            customerId = adjustedCustomerId,
            vendorId = adjustedVendorId
        )
    }
}