package com.abc.us.accounting.documents.fixtures

import com.abc.us.accounting.documents.domain.entity.Document
import com.abc.us.accounting.documents.domain.entity.DocumentItem
import com.abc.us.accounting.documents.domain.type.AccountSide
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.type.DocumentItemStatus
import com.abc.us.accounting.supports.utils.IdGenerator
import com.github.javafaker.Faker
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

// 정합성은 확인 안함
// 랜덤으로 생성할 목적
object DocumentItemFixture {
    private val faker = Faker()
    fun createDocumentItems(
        count: Int,
        document: Document? = null,
        lineNumber: Int? = null,
        docItemStatus: DocumentItemStatus? = null,
        accountCode: String? = null,
        accountSide: AccountSide? = null,
        companyCode: CompanyCode? = null,
        txCurrency: String? = null,
        originalAmount: BigDecimal? = null,
        currency: String? = null,
        amount: BigDecimal? = null,
        exchangeRateId: String? = null,
        text: String? = null,

        costCenter: String? = null,
        profitCenter: String? = null,
        segment: String? = null,
        project: String? = null,
        customerId: String? = null,
        vendorId: String? = null,

        createTime: OffsetDateTime? = null,
    ): List<DocumentItem> {
        val companyCode = companyCode ?: CompanyCode.randomSalesCompany()
        return (1..count).map {
            val adjustedDocument = document ?: DocumentFixture.createDocument()
            val adjustedlineNumber = lineNumber ?: it
            createDocumentItem(
                id = adjustedDocument.id + "-" + adjustedlineNumber,
                document = adjustedDocument,
                lineNumber = adjustedlineNumber,
                accountCode = accountCode ?: accountCodeList(companyCode).random(),
                accountSide = accountSide ?: AccountSide.entries.random(),
                companyCode = companyCode,
                txCurrency = txCurrency ?: currencies.random(),
                txAmount = originalAmount ?: BigDecimal(faker.commerce().price(10.0, 1000.0)),
                currency = currency ?: document?.txMoney!!.currencyCode(),
                amount = amount ?: originalAmount,
                exchangeRateId = exchangeRateId,
                text = text ?: faker.lorem().sentence(),
                costCenter = costCenter ?: faker.commerce().department(),
                profitCenter = profitCenter ?: faker.code().isbn10(),
                segment = null,
                project = null,
                customerId = customerId ?: UUID.randomUUID().toString(),
                vendorId = vendorId ?: faker.commerce().department(),
                createTime = createTime ?: OffsetDateTime.now(),
            )
        }
    }

    fun createDocumentItem(
        id: String = IdGenerator.generateId(),
        document: Document = DocumentFixture.createDocument(),
        lineNumber: Int = faker.number().numberBetween(1, 10),

        docItemStatus:DocumentItemStatus = DocumentItemStatus.entries.random(),
        accountCode: String = accountCodeList(document.companyCode).random(),
        accountSide: AccountSide = AccountSide.entries.random(),
        companyCode: CompanyCode = document.companyCode,
        txCurrency: String? = document.txMoney.currencyCode(),
        txAmount: BigDecimal = BigDecimal(faker.commerce().price(10.0, 1000.0)),
        currency: String? = document.txMoney.currencyCode(),
        amount: BigDecimal? = null,
        exchangeRateId: String? = null,
        text: String = faker.lorem().sentence(),

        costCenter: String? = faker.commerce().department(),
        profitCenter: String? = faker.code().isbn10(),
        segment: String? = null,
        project: String? = null,
        customerId: String? = UUID.randomUUID().toString(),
        vendorId: String? = faker.commerce().department(),

        createTime: OffsetDateTime? = null,
    ): DocumentItem {
        return DocumentItem(
            _id = id,
            docId = document.id,
            lineNumber = lineNumber,
            docItemStatus = docItemStatus,
            accountCode = accountCode,
            accountSide = accountSide,
            companyCode = companyCode,
            txMoney = document.txMoney,
            money = document.money,
            exchangeRateId = exchangeRateId,
            text = text,
            costCenter = costCenter ?: faker.commerce().department(),
            profitCenter = profitCenter ?: faker.code().isbn10(),
            segment = null,
            project = null,
            customerId = customerId ?: UUID.randomUUID().toString(),
            vendorId = vendorId ?: faker.commerce().department(),
            createTime = createTime ?: OffsetDateTime.now(),
        )
    }

    fun accountCodeList(companyCode: CompanyCode): List<String> {
        return TestDocumentTemplateMapping.entries.flatMap { it.getAccountInfos(companyCode) }.map { it.code }
    }

    val currencies = listOf("USD", "KRW")
}