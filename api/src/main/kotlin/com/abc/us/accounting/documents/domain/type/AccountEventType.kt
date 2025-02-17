package com.abc.us.accounting.documents.domain.type

enum class AccountEventType(val code: String, val engName: String, val korName: String, val category: AccountEventCategory) {
    PAYMENT_BILLING("PBI", "Billing Created", "청구", AccountEventCategory.ACCOUNT_RECEIVABLE),
    PAYMENT_BILLING_CANCELLED("PBC", "Billing Cancelled", "청구 취소", AccountEventCategory.ACCOUNT_RECEIVABLE),
    PAYMENT_RECEIVED("PRE", "Payment Received", "수납", AccountEventCategory.ACCOUNT_RECEIVABLE),
    PAYMENT_DEPOSIT("PDE", "Deposit Received", "입금", AccountEventCategory.ACCOUNT_RECEIVABLE),
    PAYMENT_CANCELLED("PCA", "Authorization Cancelled", "승인취소", AccountEventCategory.ACCOUNT_RECEIVABLE),
    PAYMENT_REFUNDED("PRE", "Capture Cancelled", "환불(매입취소)", AccountEventCategory.ACCOUNT_RECEIVABLE),

    REVENUE_RECOGNITION("RVR", "Revenue Recognition", "매출인식", AccountEventCategory.REVENUE),
    SERVICE_RECOGNITION_RECEIVED("SRR", "Service Recognition Received", "서비스매출인식", AccountEventCategory.REVENUE),
    COGS_RECOGNITION("CGR", "COGS Recognition", "원가인식", AccountEventCategory.COGS),
    REVENUE_CANCELLED("RCD", "Revenue Cancelled", "매출취소", AccountEventCategory.REVENUE),
    COGS_CANCELLED("CCA", "COGS Cancelled", "매출원가취소", AccountEventCategory.COGS),

    ADVANCE_PAYMENT_OFFSET("APO", "Advance Payment Offset", "선급금대체", AccountEventCategory.ACCOUNT_RECEIVABLE),

    GOODS_ISSUE("GII", "Goods Issue", "출고", AccountEventCategory.LOGISTICS),
    GOODS_RECEIPT("GRI", "Goods Receipt", "입고", AccountEventCategory.LOGISTICS),
    INVENTORY_DISPOSED("IDP", "Inventory Disposed", "재고자산폐기", AccountEventCategory.LOGISTICS),
    PRICE_DIFFERENCE("PRD", "Price Difference", "가격차이", AccountEventCategory.INVENTORY_VALUATION),


    RENTAL_ASSET_ACQUISITION("RAA", "Rental Asset Acquisition", "렌탈자산취득", AccountEventCategory.RENTAL_ASSET),
    RENTAL_ASSET_DEPRECIATION("RAD", "Rental Asset Depreciation", "렌탈자산상각", AccountEventCategory.RENTAL_ASSET),
    RENTAL_ASSET_DISPOSED("RAD", "Rental Asset Disposed", "렌탈자산폐기", AccountEventCategory.RENTAL_ASSET),
    RENTAL_ASSET_IMPAIRMENT("RAI", "Rental Asset Impairment", "렌탈자산손상", AccountEventCategory.RENTAL_ASSET),

    FINANCIAL_ASSET_DISPOSED("FAD", "Financial Asset Disposed", "잔여채권 정리", AccountEventCategory.FINANCIAL_ASSET),

    TRANSFER_TO_COLLECTION("TTC", "Collection Transfer Registered", "추심전환등록", AccountEventCategory.ACCOUNT_RECEIVABLE),
    RECEIVABLES_COLLECTION("CRC", "Collection Recovered", "추심회수", AccountEventCategory.ACCOUNT_RECEIVABLE),

    CUSTOMER_INTEREST_INCOME("CII", "Interest Income", "이자수익", AccountEventCategory.REVENUE),
    CONTRACT_TERMINATION_LOSS("CTL", "Contract Termination Loss", "계약해지손실", AccountEventCategory.EXPENSE),

    ;

    override fun toString(): String {
        return this.name
    }
}

enum class AccountEventCategory{
    ACCOUNT_RECEIVABLE,
    ACCOUNT_PAYABLE,
    LOGISTICS,
    INVENTORY_VALUATION,
    REVENUE,
    COGS,
    RENTAL_ASSET,
    FINANCIAL_ASSET,
    EXPENSE,
    GR_IR,
    ;
}