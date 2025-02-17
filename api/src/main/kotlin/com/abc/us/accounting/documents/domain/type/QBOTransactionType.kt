package com.abc.us.accounting.documents.domain.type

/**
 * QBO Transaction Type: ABC의 전표유형과 동일한 역할을 하는 QuickBooks Online의 전표 유형
 * @param code: 전표유형 코드
 * @param engName: 전표유형 영문명
 * @param korName: 전표유형 한글명
 * @param description: 전표유형 설명
 * @param category: 전표유형 카테고리
 */
enum class QBOTransactionType(val code:String, val engName: String, val korName: String,
                              val description: String, val category: QBOTransactionCategory) {

    SALES_RECEIPT("CS","Sales Receipt", "판매영수증", "즉시 결제된 판매 거래", QBOTransactionCategory.SALES),
    INVOICE("CI", "Customer Invoice", "매출전표/청구서", "고객에게 발행하는 청구서", QBOTransactionCategory.SALES),
    PAYMENT("CP", "Customer Payment", "매출대금수금", "고객으로부터 받은 결제", QBOTransactionCategory.SALES),
    ESTIMATE("CE", "Estimate", "견적서", "고객에게 보내는 견적서", QBOTransactionCategory.SALES),
    CREDIT_MEMO("CM" , "Credit Memo", "환불", "고객에게 지급하는 환불", QBOTransactionCategory.SALES),
    REFUND_RECEIPT("CR", "Refund Receipt", "환불영수증", "환불 영수증", QBOTransactionCategory.SALES),
    REIMBURSE_CHARGE("CE", "Reimburse Charge", "비용상환", "직원 등에 대한 비용 상환", QBOTransactionCategory.OTHERS),

    PURCHASE_ORDER("VO", "Purchase Order", "구매주문", "구매 주문", QBOTransactionCategory.PURCHASE),
    BILL("VI", "Bill", "매입전표/매입청구서", "공급업체로부터 받은 청구서 기록", QBOTransactionCategory.PURCHASE),
    BILL_PAYMENT("VP", "Bill Payment", "매입대금지급", "매입전표에 대한 지불", QBOTransactionCategory.PURCHASE),
    VENDOR_CREDIT("VC", "Vendor Credit", "매입처크레딧", "공급업체가 발행한 크레딧 메모", QBOTransactionCategory.PURCHASE),
    CREDIT_CARD_CREDIT("VR", "Credit Card Credit", "신용카드크레딧", "신용카드에 대한 크레딧(환불)", QBOTransactionCategory.PURCHASE),
    CHECK("VK", "Check", "수표", "수표를 통한 지출", QBOTransactionCategory.PURCHASE),
    EXPENSE("VE", "Expense", "비용", "일반 비용 지출", QBOTransactionCategory.PURCHASE),

    DEPOSIT("DP", "Deposit", "입금", "은행 계좌로의 입금", QBOTransactionCategory.OTHERS),
    TRANSFER("TF", "Transfer", "이체", "은행 계좌 간 이체", QBOTransactionCategory.OTHERS),
    INVENTORY_QUANTITY_ADJUSTMENT("IA", "Inventory Quantity Adjustment", "재고수량조정", "재고 수량 조정", QBOTransactionCategory.OTHERS),
    JOURNAL_ENTRY("JE", "Journal Entry", "분개", "수동으로 입력하는 회계 분개", QBOTransactionCategory.OTHERS),
    ;

    companion object {
        fun fromCode(code: String): QBOTransactionType {
            return entries.first { it.code == code }
        }

        fun fromCategory(category: QBOTransactionCategory): List<QBOTransactionType> {
            return entries.filter { it.category == category }
        }
    }

}

// TODO: 세부 케이스는 진행하면서 고민 필요
//enum class QboTransactionType(val value: String, val description: String) {
//    CREDIT_CARD_CHARGE("CreditCardCharge", "신용카드 청구"),
//    CHECK("Check", "수표"),
//    INVOICE("Invoice", "매출전표/청구서"),
//    RECEIVE_PAYMENT("ReceivePayment", "수금"),
//    JOURNAL_ENTRY("JournalEntry", "분개"),
//    BILL("Bill", "매입전표"),
//    CREDIT_CARD_CREDIT("CreditCardCredit", "신용카드 크레딧"),
//    VENDOR_CREDIT("VendorCredit", "매입처 크레딧"),
//    CREDIT("Credit", "크레딧"),
//    BILL_PAYMENT_CHECK("BillPaymentCheck", "수표를 통한 매입대금 지급"),
//    BILL_PAYMENT_CREDIT_CARD("BillPaymentCreditCard", "신용카드를 통한 매입대금 지급"),
//    CHARGE("Charge", "청구"),
//    TRANSFER("Transfer", "이체"),
//    DEPOSIT("Deposit", "입금"),
//    STATEMENT("Statement", "거래명세서"),
//    BILLABLE_CHARGE("BillableCharge", "청구가능 비용"),
//    TIME_ACTIVITY("TimeActivity", "시간활동"),
//    CASH_PURCHASE("CashPurchase", "현금구매"),
//    SALES_RECEIPT("SalesReceipt", "판매영수증"),
//    CREDIT_MEMO("CreditMemo", "대변메모"),
//    CREDIT_REFUND("CreditRefund", "크레딧 환불"),
//    ESTIMATE("Estimate", "견적서"),
//    INVENTORY_QUANTITY_ADJUSTMENT("InventoryQuantityAdjustment", "재고수량조정"),
//    PURCHASE_ORDER("PurchaseOrder", "구매주문서"),
//    GLOBAL_TAX_PAYMENT("GlobalTaxPayment", "글로벌 세금 납부"),
//    GLOBAL_TAX_ADJUSTMENT("GlobalTaxAdjustment", "글로벌 세금 조정"),
//    SERVICE_TAX_REFUND("ServiceTaxRefund", "서비스 세금 환급"),
//    SERVICE_TAX_GROSS_ADJUSTMENT("ServiceTaxGrossAdjustment", "서비스 세금 총액 조정"),
//    SERVICE_TAX_REVERSAL("ServiceTaxReversal", "서비스 세금 취소"),
//    SERVICE_TAX_DEFER("ServiceTaxDefer", "서비스 세금 이연"),
//    SERVICE_TAX_PARTIAL_UTILISATION("ServiceTaxPartialUtilisation", "서비스 세금 부분 사용");
//
//    companion object {
//        fun fromValue(value: String): QboTransactionType = values().find { it.value == value }
//            ?: throw IllegalArgumentException("Unknown QBO Transaction Type: $value")
//    }
//}

enum class QBOTransactionCategory {
    SALES,
    PURCHASE,
    OTHERS
}