package com.abc.us.accounting.rentals.onetime.utils

import com.abc.us.accounting.documents.domain.type.CompanyCode

enum class OnetimeAccountCode(val newAccountCode: String, val oldAccountCode: String, val description:String) {
    INVENTORY_IN_TRANSIT("1201011", "1230010", "시송품-상품"),
    INVENTORIES("1201010", "1201010", "상품"),

    ACCOUNT_RECEIVABLE_SALES("1117050","1117010",  "외상매출금-일시불 판매"),
    SALES_WHOLESALES("4101010","4103010",  "상품매출액-일시불"),
    COGS_PRODUCT("5103010","5103010", "상품국내매출원가-상품출고(일시불)"),

    DEPOSITS_ON_DEMAND_1("1105001","1101010",  "보통예금"),
    BANK_SERVICE_CHARGE("5423010","5423010",  "지급수수료-금융거래수수료"),
    CREDIT_CARD_RECEIVABLES("1126020","1136010",  "카드미수금"),

    OTHER_ACCOUNTS_PAYABLE("2107010", "2107010", "미지급금"),
    ADVANCED_PAYMENTS("2111010","2111010",  "선수금"),

    TAX_PAYABLE_STATE("2117011","2115020",  "예수금-판매세-State"),
    TAX_PAYABLE_COUNTY("2117012","2115030",  "예수금-판매세-County"),
    TAX_PAYABLE_CITY("2117013","2115040",  "예수금-판매세-City"),
    TAX_PAYABLE_SPECIAL("2117014","2115050",  "예수금-판매세-Special"),
;
    fun getAccountCode(companyCode: CompanyCode):String {
        return if (companyCode.isNewAccountCode()) newAccountCode else oldAccountCode
    }
}