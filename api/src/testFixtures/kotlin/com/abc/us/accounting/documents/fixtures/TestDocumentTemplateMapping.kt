package com.abc.us.accounting.documents.fixtures

import com.abc.us.accounting.commons.domain.type.CurrencyCode
import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.config.SpringContext
import com.abc.us.accounting.documents.domain.entity.DocumentTemplateKey
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.type.DocumentTemplateCode
import com.abc.us.accounting.documents.domain.type.FieldRequirement
import com.abc.us.accounting.documents.domain.type.AccountSide
import com.abc.us.accounting.documents.model.DocumentItemRequest
import com.abc.us.accounting.documents.service.AccountServiceable
import com.abc.us.accounting.supports.utils.BigDecimals.toScale
import java.math.BigDecimal
import java.util.*



enum class TestAccountCode (val oldAccountCode:String, val newAccountCode: String, val description:String){
    CREDIT_CARD_RECEIVABLES("1136010", "1126020", "카드미수금"),
    ADVANCED_FROM_CUSTOMERS("2111010", "2111010", "선수금"),
    UNEARNED_SERVICE_INCOME_OLEASE("2111010", "2111040", "선수금-운용(비리스)"),
    UNEARNED_SERVICE_INCOME_FLEASE("2111010", "2111050", "선수금-금융(비리스)"),
    ACCOUNT_RECEIVABLE_SALES("1117010", "1117050", "외상매출금-일시불"),
    ACCOUNT_RECEIVABLE_RENTAL("1117030", "1117010", "렌탈미수금-렌탈료"),
    FINANCE_LEASE_RECEIVABLE_BILLING("1117040", "1117041", "금융리스채권-월렌탈료"),
    FINANCE_LEASE_RECEIVABLE_EQUIPMENT_CURRENT("1117060", "1117043", "금융리스채권-할부발생"),
    SALES_RENTAL("4103020", "4113010", "상품매출-운용리스, 렌탈료매출-재화"),

    TAX_PAYABLE_STATE("2115020", "2117011", "예수금-판매세-State"),
    TAX_PAYABLE_COUNTY("2115030", "2117012", "예수금-판매세-County"),
    TAX_PAYABLE_CITY("2115040", "2117013", "예수금-판매세-City"),
    TAX_PAYABLE_SPECIAL("2115050", "2117014", "예수금-판매세-Special"),
    ;
    fun getAccountCode(companyCode: CompanyCode):String {
        if ( companyCode.isNewAccountCode() ) {
            return newAccountCode
        }
        return oldAccountCode
    }
    companion object {
        fun find(companyCode: CompanyCode, accountCode: String): TestAccountCode {
            if ( companyCode.isNewAccountCode() ) {
                return entries.find { it.newAccountCode == accountCode } ?:
                    throw IllegalArgumentException("TestAccountCode not found by companyCode:$companyCode, accountCode:$accountCode ")
            } else {
                return entries.find { it.oldAccountCode == accountCode } ?:
                    throw IllegalArgumentException("TestAccountCode not found by companyCode:$companyCode, accountCode:$accountCode ")
            }
        }
    }
}


enum class SalesTaxFixture(val accountCode:TestAccountCode, val engText:String, val rate:BigDecimal) {
    STATE(TestAccountCode.TAX_PAYABLE_STATE, "State", BigDecimal(0.0625).toScale(Constants.ACCOUNTING_SCALE)),
    COUNTY(TestAccountCode.TAX_PAYABLE_COUNTY, "County", BigDecimal(0).toScale(Constants.ACCOUNTING_SCALE)),
    CITY(TestAccountCode.TAX_PAYABLE_CITY, "City", BigDecimal(0.005).toScale(Constants.ACCOUNTING_SCALE)),
    SPECIAL(TestAccountCode.TAX_PAYABLE_SPECIAL, "Special", BigDecimal(0.015).toScale(Constants.ACCOUNTING_SCALE))
    ;

    fun salesTaxRate(): BigDecimal {
        return rate
    }

    fun salesTaxAmount(amount: BigDecimal): BigDecimal {
        return amount.multiply(salesTaxRate())
    }

    companion object {
        fun find (companyCode: CompanyCode, accountCode: String): SalesTaxFixture? {
            return entries.find { it.accountCode.getAccountCode(companyCode) == accountCode }
        }

        fun fromAccountCode(value:TestAccountCode): SalesTaxFixture{
            SalesTaxFixture.entries.forEach {
                if (it.accountCode == value) {
                    return it
                }
            }
            throw IllegalArgumentException("Invalid DefaultSalesTax value: $value")
        }

        fun getTotalSalesTaxRate():BigDecimal {
            return listOf(STATE, COUNTY, CITY, SPECIAL).map { it.rate }.sumOf { it }
        }

        fun getSalesTaxAmount(amount: BigDecimal): BigDecimal {
            return listOf(STATE, COUNTY, CITY, SPECIAL).map { it.salesTaxAmount(amount) }.sumOf { it }
        }

        fun generateDocItems(companyCode: CompanyCode, txCurrency: CurrencyCode, amount:BigDecimal, accountInfos:List<AccountInfo>, customerId: String? = null, vendorId: String? = null): List<DocumentItemRequest> {
            return accountInfos.map {accountInfo ->

                val testAccountCode:TestAccountCode = TestAccountCode.find(companyCode, accountInfo.code)
                val salesTax: SalesTaxFixture = fromAccountCode(testAccountCode)

                DocumentItemRequest(
                    companyCode = companyCode,
                    accountCode = accountInfo.code,
                    accountSide = accountInfo.accountSide,
                    txCurrency = txCurrency.name,
                    txAmount = salesTax.salesTaxAmount(amount),
                    text = "${salesTax.name} Sales Tax",
                    costCenter = "cost_center",
                    customerId = customerId,
                    vendorId = vendorId
                )
            }
        }
    }
}

data class AccountInfo(
    val code: String,
    val description: String,
    val accountSide: AccountSide,
    val fieldRequirement: FieldRequirement
){
    override fun toString(): String {
        return "AccountInfo(code=$code, description=$description, accountSide=$accountSide, requirementType=$fieldRequirement)"
    }
}



fun makeAccountInfos(templateCode: DocumentTemplateCode, companyCode: CompanyCode): List<AccountInfo> {
    return when (templateCode) {
        DocumentTemplateCode.ONETIME_PAYMENT_RECEIVED -> {
            if (companyCode.isNewAccountCode())  {
                 listOf(
                    AccountInfo("1126020", "카드미수금", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("2111010", "선수금", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("1136010", "카드미수금", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("2111010", "선수금", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }
        }
        DocumentTemplateCode.ONETIME_PAYMENT_DEPOSIT -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("1105001", "보통예금(1)", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("5423010", "지급수수료-금융거래수수료", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1126020", "카드미수금", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }else {
                listOf(
                    AccountInfo("1101010", "현금-보통예금", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("5423010", "지급수수료-결제수수료", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1136010", "카드미수금", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }
        }
        DocumentTemplateCode.ONETIME_PRODUCT_SHIPPED -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("1201011", "시송품-상품", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1201010", "상품", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("1230010", "시송품-상품", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1201010", "상품", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }
        }
        DocumentTemplateCode.ONETIME_SALES_RECOGNITION -> {
            if ( companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("1117050", "외상매출금-일시불 판매", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("4101010", "상품매출액-일시불", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2117011", "예수금-판매세-State", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2117012", "예수금-판매세-County", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2117013", "예수금-판매세-City", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2117014", "예수금-판매세-Special", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("1117010", "외상매출금-일시불", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("4103010", "상품매출-일시불", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2115020", "예수금-판매세-State", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2115030", "예수금-판매세-County", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2115040", "예수금-판매세-City", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2115050", "예수금-판매세-Special", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }
        }
        DocumentTemplateCode.ONETIME_COGS_RECOGNITION -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("5103010", "상품국내매출원가-상품출고(일시불)", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1201011", "시송품-상품", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("5103010", "상품매출원가-일시불", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1230010", "시송품-상품", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }
        }
        DocumentTemplateCode.ONETIME_ADVANCE_PAYMENT_OFFSET -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("2111010", "선수금", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1117050", "외상매출금-일시불 판매", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("2111010", "선수금", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1117010", "외상매출금-일시불", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }
        }
        DocumentTemplateCode.ONETIME_PRICE_DIFFERENCE -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("5103010", "상품국내매출원가-상품출고(일시불)", AccountSide.DEBIT, FieldRequirement.OPTIONAL),
                    AccountInfo("5203050", "가격차이(상품)", AccountSide.CREDIT, FieldRequirement.OPTIONAL)
                )
            } else {
                listOf(
                    AccountInfo("5103010", "상품매출원가-일시불", AccountSide.DEBIT, FieldRequirement.OPTIONAL),
                    AccountInfo("5199030", "가격차이-상품", AccountSide.CREDIT, FieldRequirement.OPTIONAL)
                )
            }
        }

        DocumentTemplateCode.ONETIME_PAYMENT_REFUND -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("2111010", "선수금", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("2107010", "미지급금", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("5423010", "지급수수료-금융거래수수료", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("2111010", "선수금", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("2107010", "미지급금", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("5423020", "지급수수료-카드수수료", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }
        }
        DocumentTemplateCode.ONETIME_RETURN_SALES_CANCELLED -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("4101010", "상품매출-일시불", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("2117011", "예수금-판매세-State", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("2117012", "예수금-판매세-County", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("2117013", "예수금-판매세-City", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("2117014", "예수금-판매세-Special", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1117050", "외상매출금-일시불 판매", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("4103010", "상품매출-일시불", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("2115020", "예수금-판매세-State", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("2115030", "예수금-판매세-County", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("2115040", "예수금-판매세-City", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("2115050", "예수금-판매세-Special", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1117010", "외상매출금-일시불", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }
        }
        DocumentTemplateCode.ONETIME_RETURN_PAYMENT_REFUND -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("5103010", "상품국내매출원가-상품출고(일시불)", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("2111010", "선수금", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("2111010", "선수금", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2107010", "미지급금", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("1117010", "외상매출금-일시불", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("2111010", "선수금", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("2111010", "선수금", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2107010", "미지급금", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }
        }
        DocumentTemplateCode.ONETIME_RETURN_PAYMENT_RECEIVED -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("1117100", "외상매출금-용역수입", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("4116021", "용역수입(이전설치및해체)", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2117011", "예수금-판매세-State", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2117012", "예수금-판매세-County", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2117013", "예수금-판매세-City", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2117014", "예수금-판매세-Special", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("1117011", "외상매출금-회수비", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("4116010", "용역매출-설치해체", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2115020", "예수금-판매세-State", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2115030", "예수금-판매세-County", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2115040", "예수금-판매세-City", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2115050", "예수금-판매세-Special", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }
        }
        DocumentTemplateCode.ONETIME_REPLACEMENT_PRODUCT_SHIPPED -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("1201011", "시송품-상품", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1201010", "상품", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("1230010", "시송품-상품", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1201010", "상품", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }
        }
        DocumentTemplateCode.ONETIME_REPLACEMENT_INSTALLATION_COMPLETED -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("5103010", "상품국내매출원가-상품출고(일시불)", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1201011", "시송품-상품", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("5103010", "상품매출원가-일시불", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1230010", "시송품-상품", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }
        }
        DocumentTemplateCode.ONETIME_PRODUCT_RECEIVED_GRADE_B -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("1201010", "상품", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("9113820", "임시-반환입고", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                )
            } else {
                listOf(
                    AccountInfo("1201020", "상품-B등급", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1900010", "임시-반환입고", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                )
            }
        }
        DocumentTemplateCode.ONETIME_INVENTORY_DISPOSED_GRADE_B -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("9113820", "임시-반환입고", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1201010", "상품", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("1900010", "임시-반환입고", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1201020", "상품-B등급", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }
        }
        DocumentTemplateCode.OLEASE_PRODUCT_SHIPPED -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("1201011", "시송품-상품", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1201010", "상품", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }else {
                listOf(
                    AccountInfo("1230010", "시송품-상품", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1201010", "상품", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }
        }
        DocumentTemplateCode.OLEASE_RENTAL_ASSET_ACQUISITION -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("1428010", "렌탈자산", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1201011", "시송품-상품", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }else {
                listOf(
                    AccountInfo("1428010", "렌탈자산", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1230010", "시송품-상품", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }
        }
        DocumentTemplateCode.OLEASE_PRICE_DIFFERENCE -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("1428010", "렌탈자산", AccountSide.DEBIT, FieldRequirement.OPTIONAL),
                    AccountInfo("5203050", "가격차이(상품)", AccountSide.CREDIT, FieldRequirement.OPTIONAL)
                )
            }else {
                listOf(
                    AccountInfo("1428010", "렌탈자산", AccountSide.DEBIT, FieldRequirement.OPTIONAL),
                    AccountInfo("5199030", "가격차이-상품", AccountSide.CREDIT, FieldRequirement.OPTIONAL)
                )
            }
        }
        DocumentTemplateCode.OLEASE_PAYMENT_BILLING -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("1117010", "렌탈미수금-렌탈료", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("4113010", "렌탈료매출-재화", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2111040", "선수금-운용(비리스)", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                )
            }else{
                listOf(
                    AccountInfo("1117030", "렌탈미수금-렌탈료", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("4103020", "상품매출-운용리스", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2111010", "선수금", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                )
            }

        }
        DocumentTemplateCode.OLEASE_PAYMENT_RECEIVED -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("1126020", "카드미수금", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1117010", "렌탈미수금-렌탈료", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2117011", "예수금-판매세-State", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2117012", "예수금-판매세-County", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2117013", "예수금-판매세-City", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2117014", "예수금-판매세-Special", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }else {
                listOf(
                    AccountInfo("1136010", "카드미수금", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1117030", "렌탈미수금-렌탈료", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2115020", "예수금-판매세-State", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2115030", "예수금-판매세-County", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2115040", "예수금-판매세-City", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2115050", "예수금-판매세-Special", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }

        }
        DocumentTemplateCode.OLEASE_PAYMENT_DEPOSIT -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("1105001", "보통예금(1)", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("5423010", "지급수수료-금융거래수수료", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1126020", "카드미수금", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }else {
                listOf(
                    AccountInfo("1101010", "현금-보통예금", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("5423020", "지급수수료-카드수수료", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1136010", "카드미수금", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }

        }
        DocumentTemplateCode.OLEASE_RENTAL_ASSET_DEPRECIATION -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("5113012", "렌탈매출원가-감가상각비", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1431010", "감가상각누계액(렌탈자산)", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }else {
                listOf(
                    AccountInfo("5113010", "운용리스매출원가-감가", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1431010", "감가상각누계액-렌탈자산", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }

        }
        DocumentTemplateCode.OLEASE_FILTER_SHIPPED -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("2111040", "선수금-운용(비리스)", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("4113011", "렌탈료매출-서비스", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("2111010", "선수금", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("4116020", "용역매출-운용리스", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }

        }
        DocumentTemplateCode.OLEASE_REPLACEMENT_PRODUCT_SHIPPED -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("1201011", "시송품-상품", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1201010", "상품", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }else {
                listOf(
                    AccountInfo("1230010", "시송품-상품", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1201010", "상품", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }

        }
        DocumentTemplateCode.OLEASE_REPLACEMENT_INSTALLATION_COMPLETED -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("1428010", "렌탈자산", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1201011", "시송품-상품", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("1428010", "렌탈자산", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1230010", "시송품-상품", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }

        }
        DocumentTemplateCode.OLEASE_RENTAL_ASSET_DISPOSED -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("1431010", "감가상각누계액(렌탈자산)", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("5471011", "렌탈자산폐기대체", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("1428010", "렌탈자산", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("1431010", "감가상각누계액-렌탈자산", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("5471030", "렌탈자산폐기대체", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("1428010", "렌탈자산", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }

        }
        DocumentTemplateCode.OLEASE_PRODUCT_RECEIVED_GRADE_B -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("1201010", "상품", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("9113820", "임시-반환입고", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("1201020", "상품-B등급", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1900010", "임시-반환입고", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }
        }
        DocumentTemplateCode.OLEASE_INVENTORY_DISPOSED_GRADE_B -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("9113820", "임시-반환입고", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("5471010", "렌탈자산폐기손실", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1201010", "상품", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("5471011", "렌탈자산폐기대체", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                )
            } else {
                listOf(
                    AccountInfo("1900010", "임시-반환입고", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1201020", "상품-B등급", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }

        }
        DocumentTemplateCode.OLEASE_RENTAL_ASSET_LOSS_DISPOSED -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("1431010", "감가상각누계액(렌탈자산)", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("5599011", "잡손실-고객분실화재", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("1428010", "렌탈자산", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("5471011", "렌탈자산폐기대체", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("1431010", "감가상각누계액-렌탈자산", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("5599010", "잡손실-고객분실화재", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("1428010", "렌탈자산", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("5471030", "렌탈자산폐기대체", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }
        }
        DocumentTemplateCode.OLEASE_RECEIVABLE_BREACHED_REGISTERED -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("5471045", "렌탈자산폐기손실-가해약", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1431019", "손상차손누계액(렌탈자산)", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("5471010", "렌탈자산폐기손실-가해약", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1431020", "손상차손누계액-렌탈자산", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }
        }
        DocumentTemplateCode.FLEASE_PRODUCT_SHIPPED -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("1201011", "시송품-상품", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1201010", "상품", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("1230010", "시송품-상품", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1201010", "상품", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }

        }
        DocumentTemplateCode.FLEASE_SALES_RECOGNITION -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("1117043", "금융리스채권-할부발생(리스렌탈료 일부상계)", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("4103050", "상품매출액-금융리스", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("1314011", "현재가치할인차금(장기성매출채권-금융리스)", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("1117060", "금융리스채권-할부발생", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("4103030", "상품매출-금융리스", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2215010", "현할차금-금융리스", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }
        }
        DocumentTemplateCode.FLEASE_COGS_RECOGNITION -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("5103050", "상품국내매출원가-상품출고(금융리스)", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1201011", "시송품-상품", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("5103020", "상품매출원가-금융리스", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1230010", "시송품-상품", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }
        }
        DocumentTemplateCode.FLEASE_PRICE_DIFFERENCE -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("5103050", "상품국내매출원가-상품출고(금융리스)", AccountSide.DEBIT, FieldRequirement.OPTIONAL),
                    AccountInfo("5203050", "가격차이(상품)", AccountSide.CREDIT, FieldRequirement.OPTIONAL)
                )
            } else {
                listOf(
                    AccountInfo("5103020", "상품매출원가-금융리스", AccountSide.DEBIT, FieldRequirement.OPTIONAL),
                    AccountInfo("5199030", "가격차이-상품", AccountSide.CREDIT, FieldRequirement.OPTIONAL)
                )
            }
        }
        DocumentTemplateCode.FLEASE_PAYMENT_BILLING -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("1117041", "금융리스채권-월 렌탈료", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1117043", "금융리스채권-할부발생(리스렌탈료 일부상계)", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2111050", "선수금-금융(비리스)", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                )
            } else {
                listOf(
                    AccountInfo("1117040", "금융리스채권-월렌탈료", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1117060", "금융리스채권-할부발생", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2111010", "선수금", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                )
            }
        }
        DocumentTemplateCode.FLEASE_PAYMENT_RECEIVED -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("1126020", "카드미수금", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1117041", "금융리스채권-월 렌탈료", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2117011", "예수금-판매세-State", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2117012", "예수금-판매세-County", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2117013", "예수금-판매세-City", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2117014", "예수금-판매세-Special", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("1136010", "카드미수금", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1117040", "금융리스채권-월렌탈료", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2115020", "예수금-판매세-State", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2115030", "예수금-판매세-County", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2115040", "예수금-판매세-City", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("2115050", "예수금-판매세-Special", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }

        }
        DocumentTemplateCode.FLEASE_PAYMENT_DEPOSIT -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("1105001", "보통예금(1)", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("5423010", "지급수수료-금융거래수수료", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1126020", "카드미수금", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("1101010", "현금-보통예금", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("5423020", "지급수수료-카드수수료", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1136010", "카드미수금", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }

        }
        DocumentTemplateCode.FLEASE_FINANCIAL_ASSET_INTEREST_INCOME -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("1314011", "현재가치할인차금(장기성매출채권-금융리스)", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("4201011", "이자수익-금융리스", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("2215010", "현할차금-금융리스", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("4201010", "이자수익-금융리스", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }

        }
        DocumentTemplateCode.FLEASE_FILTER_SHIPPED -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("2111050", "선수금-금융(비리스)", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("4116070", "용역수입(금융리스)", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("2111010", "선수금", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("4116030", "용역매출-금융리스", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }

        }
        DocumentTemplateCode.FLEASE_REPLACEMENT_PRODUCT_SHIPPED -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("1201011", "시송품-상품", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1201010", "상품", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("1230010", "시송품-상품", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1201010", "상품", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }

        }
        DocumentTemplateCode.FLEASE_REPLACEMENT_INSTALLATION_COMPLETED -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("5103050", "상품국내매출원가-상품출고(금융리스)", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1201011", "시송품-상품", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("5103020", "상품매출원가-금융리스", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1230010", "시송품-상품", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }

        }
        DocumentTemplateCode.FLEASE_PRODUCT_RECEIVED_GRADE_B -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("1201010", "상품", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("9113820", "임시-반환입고", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("1201020", "상품-B등급", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1900010", "임시-반환입고", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }

        }
        DocumentTemplateCode.FLEASE_INVENTORY_DISPOSED_GRADE_B -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("9113820", "임시-반환입고", AccountSide.DEBIT, FieldRequirement.OPTIONAL),
                    AccountInfo("1201010", "상품", AccountSide.CREDIT, FieldRequirement.OPTIONAL)
                )
            } else {
                listOf(
                    AccountInfo("1900010", "임시-반환입고", AccountSide.DEBIT, FieldRequirement.OPTIONAL),
                    AccountInfo("1201020", "상품-B등급", AccountSide.CREDIT, FieldRequirement.OPTIONAL)
                )
            }

        }
        DocumentTemplateCode.FLEASE_FINANCIAL_ASSET_DISPOSED -> {
            if (companyCode.isNewAccountCode())  {
                listOf(
                    AccountInfo("5471072", "계약해지계약해지손실(위약금)-금융리스", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("1314011", "현재가치할인차금(장기성매출채권-금융리스)", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("1117043", "금융리스채권-할부발생(리스렌탈료 일부상계)", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            } else {
                listOf(
                    AccountInfo("5471050", "계약해지손실-위약금-금융리스", AccountSide.DEBIT, FieldRequirement.REQUIRED),
                    AccountInfo("2215010", "현할차금-금융리스", AccountSide.CREDIT, FieldRequirement.REQUIRED),
                    AccountInfo("1117060", "금융리스채권-할부발생", AccountSide.CREDIT, FieldRequirement.REQUIRED)
                )
            }

        }
        else -> emptyList()
    }
}

interface BasicTestDocumentTemplateMapping {
    fun getAccountInfos(companyCode: CompanyCode):List<AccountInfo>
}


enum class TestDocumentTemplateMapping(
    val templateCode: DocumentTemplateCode,
    val description: String
): BasicTestDocumentTemplateMapping {
    ONETIME_PAYMENT_RECEIVED(
        DocumentTemplateCode.ONETIME_PAYMENT_RECEIVED,
        "[일시불] 주문접수",
    ),

    ONETIME_PAYMENT_DEPOSIT(
        DocumentTemplateCode.ONETIME_PAYMENT_DEPOSIT,
        "[일시불] 대금입금"
    ){
        override fun generateDocItemRequests(
            companyCode: CompanyCode,
            txCurrency: CurrencyCode,
            accountAmountsMap: Map<String, BigDecimal>,
            customerId: String?,
            vendorId: String?
        ): List<DocumentItemRequest> {
            require(accountAmountsMap.size == 1) { "amounts size must be 1" }
            val amount = accountAmountsMap.values.first()
            val accounts = getAccountInfos(companyCode)
            return generateDocItemsForDeposit(companyCode, txCurrency, amount, accounts, customerId, vendorId)
        }
    },

    ONETIME_PRODUCT_SHIPPED(
        DocumentTemplateCode.ONETIME_PRODUCT_SHIPPED,
        "[일시불] 제품출고"
    ),

    ONETIME_SALES_RECOGNITION(
        DocumentTemplateCode.ONETIME_SALES_RECOGNITION,
        "[일시불] 설치완료-매출인식"
    ){
        override fun generateDocItemRequests(
            companyCode: CompanyCode,
            txCurrency: CurrencyCode,
            accountAmountsMap: Map<String, BigDecimal>,
            customerId: String?,
            vendorId: String?
        ): List<DocumentItemRequest> {
            require(accountAmountsMap.size == 1) { "amounts size must be 1" }
            val accounts = getAccountInfos(companyCode)

            val amount = accountAmountsMap.values.first()
            val debitAmount = amount + SalesTaxFixture.getSalesTaxAmount(amount)
            val debitItem = DocumentItemRequest(
                companyCode = companyCode,
                accountCode = accounts[0].code,
                accountSide = accounts[0].accountSide,
                txCurrency = txCurrency.name,
                txAmount = debitAmount,
                text = accounts[0].description,
                costCenter = "cost_center",
                customerId = customerId,
                vendorId = vendorId
            )
            val creditItem = DocumentItemRequest(
                companyCode = companyCode,
                accountCode = accounts[1].code,
                accountSide = accounts[1].accountSide,
                txCurrency = txCurrency.name,
                txAmount = amount,
                text = accounts[1].description,
                costCenter = "cost_center",
                customerId = customerId,
                vendorId = vendorId
            )
            val salesTaxItems = SalesTaxFixture.generateDocItems(companyCode, txCurrency, amount, accounts.subList(2, accounts.size), customerId = customerId, vendorId = vendorId)

            val list = mutableListOf(debitItem,creditItem)
            list.addAll(salesTaxItems)
            return list.filter { it.txAmount > BigDecimal.ZERO }
        }
    },

    ONETIME_COGS_RECOGNITION(
        DocumentTemplateCode.ONETIME_COGS_RECOGNITION,
        "[일시불] 설치완료-매출원가 인식"
    ),

    ONETIME_ADVANCE_PAYMENT_OFFSET(
        DocumentTemplateCode.ONETIME_ADVANCE_PAYMENT_OFFSET,
        "[일시불] 설치완료-선수금 대체"
    ),

    ONETIME_INVENTORY_PRICE_DIFFERENCE(
        DocumentTemplateCode.ONETIME_PRICE_DIFFERENCE,
        "[일시불] 설치완료-재고가액 확정"
    ),

//    // ONE_TIME_SALES(일시불) 취소 관련
//    ONETIME_PAYMENT_AUTH_CANCELLED(
//        DocumentTemplateCode.ONETIME_PAYMENT_VOID,
//        "[일시불:취소] 승인취소"
//    ),

    ONETIME_PAYMENT_CAPTURE_REVERSAL(
        DocumentTemplateCode.ONETIME_PAYMENT_REFUND,
        "[일시불:취소] 환불"
    ){
        override fun generateDocItemRequests(
            companyCode: CompanyCode,
            txCurrency: CurrencyCode,
            accountAmountsMap: Map<String, BigDecimal>,
            customerId: String?,
            vendorId: String?
        ): List<DocumentItemRequest> {
            require(accountAmountsMap.size == 1) { "amounts size must be 1" }

            val accounts = getAccountInfos(companyCode)
            val amount = accountAmountsMap.values.first()

            val feeAmount = amount.multiply(TRANSACTION_FEE_RATE)
            val refundAmount = amount.subtract(feeAmount)

            val depositItem = DocumentItemRequest(
                companyCode = companyCode,
                accountCode = accounts[0].code,
                accountSide = accounts[0].accountSide,
                txCurrency = txCurrency.name,
                txAmount = amount,
                text = accounts[0].description,
                costCenter = "cost_center",
                customerId = customerId,
                vendorId = vendorId
            )
            val feeItem = DocumentItemRequest(
                companyCode = companyCode,
                accountCode = accounts[1].code,
                accountSide = accounts[1].accountSide,
                txCurrency = txCurrency.name,
                txAmount = refundAmount,
                text = accounts[1].description,
                costCenter = "cost_center",
                customerId = customerId,
                vendorId = vendorId
            )
            val creditItem = DocumentItemRequest(
                companyCode = companyCode,
                accountCode = accounts[2].code,
                accountSide = accounts[2].accountSide,
                txCurrency = txCurrency.name,
                txAmount = feeAmount,
                text = accounts[2].description,
                costCenter = "cost_center",
                customerId = customerId,
                vendorId = vendorId
            )

            return listOf(depositItem, feeItem, creditItem)
        }
    },

    // ONE_TIME_SALES(일시불) 반품 관련
    ONETIME_RETURN_SALES_CANCELLED(
        DocumentTemplateCode.ONETIME_RETURN_SALES_CANCELLED,
        "[일시불:반품] 매출취소"
    ){
        override fun generateDocItemRequests(
            companyCode: CompanyCode,
            txCurrency: CurrencyCode,
            accountAmountsMap: Map<String, BigDecimal>,
            customerId: String?,
            vendorId: String?
        ): List<DocumentItemRequest> {
            require(accountAmountsMap.size == 1) { "amounts size must be 1" }
            val amount = accountAmountsMap.values.first()
            val accounts = getAccountInfos(companyCode)

            val creditAmount = amount + SalesTaxFixture.getSalesTaxAmount(amount)

            val debitItem = DocumentItemRequest(
                companyCode = companyCode,
                accountCode = accounts[0].code,
                accountSide = accounts[0].accountSide,
                txCurrency = txCurrency.name,
                txAmount = amount,
                text = accounts[0].description,
                costCenter = "cost_center",
                customerId = customerId,
                vendorId = vendorId
            )
            val salesTaxItems = SalesTaxFixture.generateDocItems(companyCode, txCurrency, amount, accounts.subList(2, accounts.size))
            val creditItem = DocumentItemRequest(
                companyCode = companyCode,
                accountCode = accounts[1].code,
                accountSide = accounts[1].accountSide,
                txCurrency = txCurrency.name,
                txAmount = creditAmount,
                text = accounts[1].description,
                costCenter = "cost_center",
                customerId = customerId,
                vendorId = vendorId
            )

            val list = mutableListOf(debitItem)
            list.addAll(salesTaxItems)
            list.add(creditItem)

            return list.filter { it.txAmount > BigDecimal.ZERO }
        }
    },

    ONETIME_RETURN_PAYMENT_REFUND(
        DocumentTemplateCode.ONETIME_RETURN_PAYMENT_REFUND,
        "[일시불:반품] 환불"
    ),

    ONETIME_RETURN_PRODUCT_RECEIPT(
        DocumentTemplateCode.ONETIME_PRODUCT_RECEIVED_GRADE_B,
        "[일시불:반품] 반품입고",
    ),

    ONETIME_RETURN_PAYMENT_RECEIVED(
        DocumentTemplateCode.ONETIME_RETURN_PAYMENT_RECEIVED,
        "[일시불:반품] 현장수납",
    ){
        override fun generateDocItemRequests(
            companyCode: CompanyCode,
            txCurrency: CurrencyCode,
            accountAmountsMap: Map<String, BigDecimal>,
            customerId: String?,
            vendorId: String?
        ): List<DocumentItemRequest> {
            require(accountAmountsMap.size == 1) { "amounts size must be 1" }
            val amount = accountAmountsMap.values.first()
            val accounts = getAccountInfos(companyCode)

            val debitAmount = amount + SalesTaxFixture.getSalesTaxAmount(amount)
            val debitItem = DocumentItemRequest(
                companyCode = companyCode,
                accountCode = accounts[0].code,
                accountSide = accounts[0].accountSide,
                txCurrency = txCurrency.name,
                txAmount = debitAmount,
                text = accounts[0].description,
                costCenter = "cost_center",
                customerId = customerId,
                vendorId = vendorId
            )
            val creditItem = DocumentItemRequest(
                companyCode = companyCode,
                accountCode = accounts[1].code,
                accountSide = accounts[1].accountSide,
                txCurrency = txCurrency.name,
                txAmount = amount,
                text = accounts[1].description,
                costCenter = "cost_center",
                customerId = customerId,
                vendorId = vendorId
            )
            val salesTaxItems = SalesTaxFixture.generateDocItems(companyCode, txCurrency, amount, accounts.subList(2, accounts.size), customerId = customerId, vendorId = vendorId)

            val list = mutableListOf(debitItem,creditItem)
            list.addAll(salesTaxItems)
            return list.filter { it.txAmount > BigDecimal.ZERO }
        }
    },

    ONETIME_RETURN_GRADE_B_INVENTORY_DISPOSED(
        DocumentTemplateCode.ONETIME_INVENTORY_DISPOSED_GRADE_B,
        "[일시불:반품] B급자산 폐기",
    ),

    // ONE_TIME_SALES(일시불) 교환 관련
    ONETIME_REPLACEMENT_PRODUCT_SHIPPED(
        DocumentTemplateCode.ONETIME_REPLACEMENT_PRODUCT_SHIPPED,
        "[일시불:교환] 제품출고",
    ),

    ONETIME_REPLACEMENT_INSTALLATION_COMPLETED(
        DocumentTemplateCode.ONETIME_REPLACEMENT_INSTALLATION_COMPLETED,
        "[일시불:교환] 설치완료",
    ),

    ONETIME_REPLACEMENT_PRODUCT_RECEIPT(
        DocumentTemplateCode.ONETIME_PRODUCT_RECEIVED_GRADE_B,
        "[일시불:교환] 교환입고",
    ),

    ONETIME_REPLACEMENT_INVENTORY_PRICE_DIFFERENCE(
        DocumentTemplateCode.ONETIME_PRICE_DIFFERENCE,
        "[일시불:교환] 재고가액 확정",
    ),

    ONETIME_REPLACEMENT_GRADE_B_INVENTORY_DISPOSED(
        DocumentTemplateCode.ONETIME_INVENTORY_DISPOSED_GRADE_B,
        "[일시불:반품] B급자산 폐기",
    ),

    // 운용리스 관련
    OPERATING_LEASE_PRODUCT_SHIPPED(
        DocumentTemplateCode.OLEASE_PRODUCT_SHIPPED,
        "[운용리스] 제품출고",
    ),

    OPERATING_LEASE_RENTAL_ASSET_ACQUISITION(
        DocumentTemplateCode.OLEASE_RENTAL_ASSET_ACQUISITION,
        "[운용리스] 설치완료-렌탈자산 인식",
    ),

    OPERATING_LEASE_INVENTORY_PRICE_DIFFERENCE(
        DocumentTemplateCode.OLEASE_PRICE_DIFFERENCE,
        "[운용리스] 설치완료-재고가액 확정",
    ),

    OPERATING_LEASE_CUSTOMER_BILLING(
        DocumentTemplateCode.OLEASE_PAYMENT_BILLING,
        "[운용리스] 청구",
    ){
        override fun generateDocItemRequests(
            companyCode: CompanyCode,
            txCurrency: CurrencyCode,
            accountAmountsMap: Map<String, BigDecimal>,
            customerId: String?,
            vendorId: String?
        ): List<DocumentItemRequest> {
            require(accountAmountsMap.size == 2) { "amounts size must be 2" }
            val accounts = getAccountInfos(companyCode)

            val oleaseAmount = accountAmountsMap[TestAccountCode.SALES_RENTAL.getAccountCode(companyCode)]!!        // 렌탈료매출-재화
            val advancedAmount = accountAmountsMap[TestAccountCode.UNEARNED_SERVICE_INCOME_OLEASE.getAccountCode(companyCode)]!! // 선수금-운용(비리스)

            val amount = oleaseAmount.add(advancedAmount)
            val debitAmount = amount

            val debitItem = DocumentItemRequest(
                companyCode = companyCode,
                accountCode = accounts[0].code,
                accountSide = accounts[0].accountSide,
                txCurrency = txCurrency.name,
                txAmount = debitAmount,
                text = accounts[0].description,
                costCenter = "cost_center",
                customerId = customerId,
                vendorId = vendorId
            )
            val oleaseItem = DocumentItemRequest(
                companyCode = companyCode,
                accountCode = accounts[1].code,
                accountSide = accounts[1].accountSide,
                txCurrency = txCurrency.name,
                txAmount = oleaseAmount,
                text = accounts[1].description,
                costCenter = "cost_center",
                customerId = customerId,
                vendorId = vendorId
            )
            val advanceItem = DocumentItemRequest(
                companyCode = companyCode,
                accountCode = accounts[2].code,
                accountSide = accounts[2].accountSide,
                txCurrency = txCurrency.name,
                txAmount = advancedAmount,
                text = accounts[2].description,
                costCenter = "cost_center",
                customerId = customerId,
                vendorId = vendorId
            )

            val list = mutableListOf(debitItem,oleaseItem, advanceItem)
            return list.filter { it.txAmount > BigDecimal.ZERO }
        }
    },

    OPERATING_LEASE_PAYMENT_RECEIVED(
        DocumentTemplateCode.OLEASE_PAYMENT_RECEIVED,
        "[운용리스] 수납",
    ){
        override fun generateDocItemRequests(
            companyCode: CompanyCode,
            txCurrency: CurrencyCode,
            accountAmountsMap: Map<String, BigDecimal>,
            customerId: String?,
            vendorId: String?
        ): List<DocumentItemRequest> {
            require(accountAmountsMap.size == 1) { "amounts size must be 1, but accountAmountsMap:${accountAmountsMap}" }
            val accounts = getAccountInfos(companyCode)
            val accountCode = TestAccountCode.ACCOUNT_RECEIVABLE_RENTAL.getAccountCode(companyCode)
            val creditAmount = accountAmountsMap[accountCode]!!      // 렌탈미수금-렌탈료
            val debitAmount = creditAmount + SalesTaxFixture.getSalesTaxAmount(creditAmount)

            val debitItem = DocumentItemRequest(
                companyCode = companyCode,
                accountCode = accounts[0].code,
                accountSide = accounts[0].accountSide,
                txCurrency = txCurrency.name,
                txAmount = debitAmount,
                text = accounts[0].description,
                costCenter = "cost_center",
                customerId = customerId,
                vendorId = vendorId
            )
            val creditItem = DocumentItemRequest(
                companyCode = companyCode,
                accountCode = accounts[1].code,
                accountSide = accounts[1].accountSide,
                txCurrency = txCurrency.name,
                txAmount = creditAmount,
                text = accounts[1].description,
                costCenter = "cost_center",
                customerId = customerId,
                vendorId = vendorId
            )

            val salesTaxesAccounts = accounts.filter { SalesTaxFixture.find(companyCode, it.code) != null }
            val salesTaxItems = SalesTaxFixture.generateDocItems(companyCode, txCurrency, creditAmount, salesTaxesAccounts, customerId = customerId, vendorId = vendorId)
            require(salesTaxItems.size == 4) {"salesTaxItems must be 4, but salesTaxItems:${salesTaxItems}"}

            val list = mutableListOf(debitItem, creditItem)
            list.addAll(salesTaxItems)
            return list.filter { it.txAmount > BigDecimal.ZERO }
        }
    },


    OPERATING_LEASE_DEPOSIT_RECEIVED(
        DocumentTemplateCode.OLEASE_PAYMENT_DEPOSIT,
        "[운용리스] 입금",
    ){
        override fun generateDocItemRequests(
            companyCode: CompanyCode,
            txCurrency: CurrencyCode,
            accountAmountsMap: Map<String, BigDecimal>,
            customerId: String?,
            vendorId: String?
        ): List<DocumentItemRequest> {
            require(accountAmountsMap.size == 1) { "amounts size must be 1" }
            val amount = accountAmountsMap.values.first()
            val accounts = getAccountInfos(companyCode)
            return generateDocItemsForDeposit(companyCode, txCurrency, amount, accounts, customerId = customerId, vendorId = vendorId)
        }
    },

    OPERATING_LEASE_RENTAL_ASSET_DEPRECIATION(
        DocumentTemplateCode.OLEASE_RENTAL_ASSET_DEPRECIATION,
        "[운용리스:상각] 렌탈자산 감가상각",
    ),

    OPERATING_LEASE_FILTER_SHIPPED(
        DocumentTemplateCode.OLEASE_FILTER_SHIPPED,
        "[운용리스:서비스매출] 필터배송",
    ),

    // 운용리스 교환 관련
    OPERATING_LEASE_REPLACEMENT_PRODUCT_SHIPPED(
        DocumentTemplateCode.OLEASE_REPLACEMENT_PRODUCT_SHIPPED,
        "[운용리스:교환] 교환출고",
    ),

    OPERATING_LEASE_REPLACEMENT_INSTALLATION_COMPLETED(
        DocumentTemplateCode.OLEASE_REPLACEMENT_INSTALLATION_COMPLETED,
        "[운용리스:교환] 설치완료",
    ),

    OPERATING_LEASE_REPLACEMENT_RENTAL_ASSET_DISPOSED(
        DocumentTemplateCode.OLEASE_RENTAL_ASSET_DISPOSED,
        "[운용리스:교환] 교환완료- 렌탈자산 폐기",
    ),

    OPERATING_LEASE_REPLACEMENT_PRODUCT_RECEIPT(
        DocumentTemplateCode.OLEASE_PRODUCT_RECEIVED_GRADE_B,
        "[운용리스:교환] 반환입고",
    ),

    OPERATING_LEASE_REPLACEMENT_PRICE_DIFFERENCE(
        DocumentTemplateCode.OLEASE_PRICE_DIFFERENCE,
        "[운용리스:교환] 교환완료- 재고가액 확정",
    ),

    OPERATING_LEASE_REPLACEMENT_GRADE_B_INVENTORY_DISPOSED(
        DocumentTemplateCode.OLEASE_INVENTORY_DISPOSED_GRADE_B,
        "[운용리스:교환] B급 재고자산 폐기",
    ),

    // 운용리스 해지 관련
    OPERATING_LEASE_CONTRACT_TERMINATION_RENTAL_ASSET_DISPOSED(
        DocumentTemplateCode.OLEASE_RENTAL_ASSET_DISPOSED,
        "[운용리스:해지] 렌탈자산 폐기",
    ),

    OPERATING_LEASE_CONTRACT_TERMINATION_RENTAL_ASSET_LOSS_DISPOSED(
        DocumentTemplateCode.OLEASE_RENTAL_ASSET_LOSS_DISPOSED,
        "[운용리스:해지] 렌탈자산 폐기(분실)",
    ),

    OPERATING_LEASE_CONTRACT_TERMINATION_PRODUCT_RECEIPT(
        DocumentTemplateCode.OLEASE_PRODUCT_RECEIVED_GRADE_B,
        "[운용리스:해지] 반환입고",
    ),

    OPERATING_LEASE_CONTRACT_TERMINATION_GRADE_B_INVENTORY_DISPOSED(
        DocumentTemplateCode.OLEASE_INVENTORY_DISPOSED_GRADE_B,
        "[운용리스:해지] B급 재고자산 폐기",
    ),

    OPERATING_LEASE_RECEIVABLE_RENTAL_ASSET_IMPAIRMENT(
        DocumentTemplateCode.OLEASE_RECEIVABLE_BREACHED_REGISTERED,
        "[운용리스:채권] 가해약 등록 - 렌탈자산 손상처리",
    ),

    // 금융리스 관련
    FINANCIAL_LEASE_PRODUCT_SHIPPED(
        DocumentTemplateCode.FLEASE_PRODUCT_SHIPPED,
        "[금융리스] 제품출고",
    ),

    FINANCIAL_LEASE_GOODS_SALES_RECOGNITION(
        DocumentTemplateCode.FLEASE_SALES_RECOGNITION,
        "[금융리스] 설치완료-재화매출 인식",
    ),

    FINANCIAL_LEASE_COGS_RECOGNITION(
        DocumentTemplateCode.FLEASE_COGS_RECOGNITION,
        "[금융리스] 설치완료-매출원가 인식",
    ),

    FINANCIAL_LEASE_INVENTORY_PRICE_DIFFERENCE(
        DocumentTemplateCode.FLEASE_PRICE_DIFFERENCE,
        "[금융리스] 설치완료-재고가액 확정",
    ),

    FINANCIAL_LEASE_CUSTOMER_BILLING(
        DocumentTemplateCode.FLEASE_PAYMENT_BILLING,
        "[금융리스] 청구",
    ){
        override fun generateDocItemRequests(
            companyCode: CompanyCode,
            txCurrency: CurrencyCode,
            accountAmountsMap: Map<String, BigDecimal>,
            customerId: String?,
            vendorId: String?
        ): List<DocumentItemRequest> {
            require(accountAmountsMap.size == 2) { "amounts size must be 2" }
            val accounts = getAccountInfos(companyCode)

            val fleaseAmount = accountAmountsMap[TestAccountCode.FINANCE_LEASE_RECEIVABLE_EQUIPMENT_CURRENT.getAccountCode(companyCode)]!!  // 금융리스채권-할부발생
            val advancedAmount = accountAmountsMap[TestAccountCode.UNEARNED_SERVICE_INCOME_FLEASE.getAccountCode(companyCode)]!!     // 선수금-금융(비리스)

            val amount = fleaseAmount.add(advancedAmount)
            val debitAmount = amount

            val debitItem = DocumentItemRequest(
                companyCode = companyCode,
                accountCode = accounts[0].code,
                accountSide = accounts[0].accountSide,
                txCurrency = txCurrency.name,
                txAmount = debitAmount,
                text = accounts[0].description,
                costCenter = "cost_center",
                customerId = customerId,
                vendorId = vendorId
            )
            val oleaseItem = DocumentItemRequest(
                companyCode = companyCode,
                accountCode = accounts[1].code,
                accountSide = accounts[1].accountSide,
                txCurrency = txCurrency.name,
                txAmount = fleaseAmount,
                text = accounts[1].description,
                costCenter = "cost_center",
                customerId = customerId,
                vendorId = vendorId
            )
            val advanceItem = DocumentItemRequest(
                companyCode = companyCode,
                accountCode = accounts[2].code,
                accountSide = accounts[2].accountSide,
                txCurrency = txCurrency.name,
                txAmount = advancedAmount,
                text = accounts[2].description,
                costCenter = "cost_center",
                customerId = customerId,
                vendorId = vendorId
            )
            val list = mutableListOf(debitItem,oleaseItem, advanceItem)
            return list.filter { it.txAmount > BigDecimal.ZERO }
        }

    },

    FINANCIAL_LEASE_PAYMENT_RECEIVED(
        DocumentTemplateCode.FLEASE_PAYMENT_RECEIVED,
        "[금융리스] 수납",
    ){
        override fun generateDocItemRequests(
            companyCode: CompanyCode,
            txCurrency: CurrencyCode,
            accountAmountsMap: Map<String, BigDecimal>,
            customerId: String?,
            vendorId: String?
        ): List<DocumentItemRequest> {
            require(accountAmountsMap.size == 1) { "amounts size must be 2, but accountAmountsMap: $accountAmountsMap" }
            val accounts = getAccountInfos(companyCode)
            val creditAmount = accountAmountsMap[TestAccountCode.FINANCE_LEASE_RECEIVABLE_BILLING.getAccountCode(companyCode)]!!      // 금융리스채권-월렌탈료
            val debitAmount = creditAmount + SalesTaxFixture.getSalesTaxAmount(creditAmount)

            val debitItem = DocumentItemRequest(
                companyCode = companyCode,
                accountCode = accounts[0].code,
                accountSide = accounts[0].accountSide,
                txCurrency = txCurrency.name,
                txAmount = debitAmount,
                text = accounts[0].description,
                costCenter = "cost_center",
                customerId = customerId,
                vendorId = vendorId
            )
            val creditItem = DocumentItemRequest(
                companyCode = companyCode,
                accountCode = accounts[1].code,
                accountSide = accounts[1].accountSide,
                txCurrency = txCurrency.name,
                txAmount = creditAmount,
                text = accounts[1].description,
                costCenter = "cost_center",
                customerId = customerId,
                vendorId = vendorId
            )
            val salesTaxItems = SalesTaxFixture.generateDocItems(companyCode, txCurrency, creditAmount, accounts.subList(2, accounts.size), customerId = customerId, vendorId = vendorId)
            require(salesTaxItems.size == 4) {"salesTaxItems must be 4, but salesTaxItems:${salesTaxItems}"}

            val list = mutableListOf(debitItem,creditItem)
            list.addAll(salesTaxItems)
            return list.filter { it.txAmount > BigDecimal.ZERO }
        }

    },

    FINANCIAL_LEASE_DEPOSIT_RECEIVED(
        DocumentTemplateCode.FLEASE_PAYMENT_DEPOSIT,
        "[금융리스] 입금",
    ){
        override fun generateDocItemRequests(
            companyCode: CompanyCode,
            txCurrency: CurrencyCode,
            accountAmountsMap: Map<String, BigDecimal>,
            customerId: String?,
            vendorId: String?
        ): List<DocumentItemRequest> {
            require(accountAmountsMap.size == 1) { "amounts size must be 1" }
            val accounts = getAccountInfos(companyCode)
            val amount = accountAmountsMap.values.first()
            return generateDocItemsForDeposit(companyCode, txCurrency, amount, accounts, customerId, vendorId)
        }
    },

    FINANCIAL_LEASE_DEPRECIATION(
        DocumentTemplateCode.FLEASE_FINANCIAL_ASSET_INTEREST_INCOME,
        "[금융리스:상각] 금융리스상각",
    ),

    FINANCIAL_LEASE_FILTER_SHIPPED(
        DocumentTemplateCode.FLEASE_FILTER_SHIPPED,
        "[금융리스:서비스매출] 필터배송",
    ),

    // 금융리스 교환 관련
    FINANCIAL_LEASE_REPLACEMENT_PRODUCT_SHIPPED(
        DocumentTemplateCode.FLEASE_REPLACEMENT_PRODUCT_SHIPPED,
        "[금융리스:교환] 교환출고",
    ),

    FINANCIAL_LEASE_REPLACEMENT_INSTALLATION_COMPLETED(
        DocumentTemplateCode.FLEASE_REPLACEMENT_INSTALLATION_COMPLETED,
        "[금융리스:교환] 설치완료",
    ),

    FINANCIAL_LEASE_REPLACEMENT_PRODUCT_RECEIPT(
        DocumentTemplateCode.FLEASE_PRODUCT_RECEIVED_GRADE_B,
        "[금융리스:교환] 반환입고",
    ),

    FINANCIAL_LEASE_REPLACEMENT_INVENTORY_PRICE_DIFFERENCE(
        DocumentTemplateCode.FLEASE_PRICE_DIFFERENCE,
        "[금융리스:교환] 교환완료- 재고가액 확정",
    ),

    FINANCIAL_LEASE_REPLACEMENT_GRADE_B_INVENTORY_DISPOSED(
        DocumentTemplateCode.FLEASE_INVENTORY_DISPOSED_GRADE_B,
        "[금융리스:교환] B급 재고자산 폐기",
    ),

//    // 금융리스 해지 관련
//    FINANCIAL_LEASE_CONTRACT_TERMINATION_DISMANTLING(
//        DocumentTemplateCode.FLEASE_TERMINATION_DISMANTLING,
//        "[금융리스:해지] 해체확정",
//    ),

    FINANCIAL_LEASE_CONTRACT_TERMINATION_PRODUCT_RECEIPT(
        DocumentTemplateCode.FLEASE_PRODUCT_RECEIVED_GRADE_B,
        "[금융리스:해지] 반환입고",
    ),

    FINANCIAL_LEASE_CONTRACT_TERMINATION_GRADE_B_INVENTORY_DISPOSED(
        DocumentTemplateCode.FLEASE_INVENTORY_DISPOSED_GRADE_B,
        "[금융리스:해지] B급 재고자산 폐기",
    );

    override fun getAccountInfos(companyCode: CompanyCode): List<AccountInfo> {
        return makeAccountInfos(templateCode,companyCode)
    }


    override fun toString(): String {
        return "DocumentTemplateMapping(templateId='$templateCode', description='$description')"
    }

    open fun generateDocItemRequests(companyCode: CompanyCode,
                                     txCurrency: CurrencyCode,
                                     accountAmountsMap: Map<String, BigDecimal>,
                                     customerId:String? = null,
                                     vendorId:String? = null): List<DocumentItemRequest> {

        require( accountAmountsMap.values.distinct().size == 1 ) { "amounts must be same, but accounts:${accountAmountsMap}" }
        val amount = accountAmountsMap.values.first()
        val accounts = getAccountInfos(companyCode)

        return accounts.map {
            val accountEntity = accountService.getAccount(companyCode, it.code)
            val adjustedCustomerId = if ( accountEntity.accountType.customerIdRequired() ) {
                customerId ?: UUID.randomUUID().toString()
            } else {
                customerId
            }
            val adjustedVendorId = if ( accountEntity.accountType.vendorIdRequired() ) {
                vendorId ?: UUID.randomUUID().toString()
            } else {
                vendorId
            }

            DocumentItemRequest(
                companyCode = companyCode,
                accountCode = it.code,
                accountSide = it.accountSide,
                txCurrency = txCurrency.name,
                txAmount = amount,
                text = it.description,
                costCenter = "cost_center",
                customerId = adjustedCustomerId,
                vendorId = adjustedVendorId
            )
        }
    }

    companion object {
        val TRANSACTION_FEE_RATE: BigDecimal = BigDecimal(0.029) .toScale(Constants.ACCOUNTING_SCALE)       // 수수료율: 2.9% 가정

        private var accountService: AccountServiceable
        init {
            // AccountRepository 빈을 직접 주입
            SpringContext.getBean(AccountServiceable::class.java).let {
                accountService = it
            }
        }

        fun generateDocItemsForDeposit(companyCode: CompanyCode, txCurrency: CurrencyCode, amount: BigDecimal, accounts: List<AccountInfo>, customerId: String? = null, vendorId: String? = null): List<DocumentItemRequest> {
            val feeAmount = amount.multiply(TRANSACTION_FEE_RATE)
            val depositAmount = amount.subtract(feeAmount)

            val depositItem = DocumentItemRequest(
                companyCode = companyCode,
                accountCode = accounts[0].code,
                accountSide = accounts[0].accountSide,
                txCurrency = txCurrency.name,
                txAmount = depositAmount,
                text = accounts[0].description,
                costCenter = "cost_center",
                customerId = customerId,
                vendorId = vendorId
            )
            val feeItem = DocumentItemRequest(
                companyCode = companyCode,
                accountCode = accounts[1].code,
                accountSide = accounts[1].accountSide,
                txCurrency = txCurrency.name,
                txAmount = feeAmount,
                text = accounts[1].description,
                costCenter = "cost_center",
                customerId = customerId,
                vendorId = vendorId
            )
            val creditItem = DocumentItemRequest(
                companyCode = companyCode,
                accountCode = accounts[2].code,
                accountSide = accounts[2].accountSide,
                txCurrency = txCurrency.name,
                txAmount = amount,
                text = accounts[2].description,
                costCenter = "cost_center",
                customerId = customerId,
                vendorId = vendorId
            )

            return listOf(depositItem, feeItem, creditItem)
        }



        // templateId로 계정 코드 찾기
        fun findAccountInfos(docTemplateCode: DocumentTemplateCode, companyCode: CompanyCode): List<AccountInfo> {
            return findAccountInfos(DocumentTemplateKey(companyCode, docTemplateCode))
        }

        fun findAccountInfos(docTemplateKey:DocumentTemplateKey): List<AccountInfo> {
            return makeAccountInfos(docTemplateKey.docTemplateCode, docTemplateKey.companyCode)
        }


        // templateId로 매핑 정보 찾기
        fun findByTemplateCode(templateCode: DocumentTemplateCode, companyCode: CompanyCode): TestDocumentTemplateMapping {
            return findByTemplateKey(DocumentTemplateKey(companyCode, templateCode))
        }

        fun findByTemplateKey(docTemplateKey: DocumentTemplateKey): TestDocumentTemplateMapping {
            return entries.find { it.templateCode == docTemplateKey.docTemplateCode }
                ?: throw IllegalArgumentException("해당 templateCode에 대한 매핑 정보가 없습니다.")
        }
    }




}
