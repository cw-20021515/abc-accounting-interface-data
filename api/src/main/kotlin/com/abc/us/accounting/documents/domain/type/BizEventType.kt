package com.abc.us.accounting.documents.domain.type

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

enum class BizEventType(val code: String, val engName: String, val korName: String, val processTypes: List<BizProcessType>) {
    // 주문/설치 관련 이벤트
    ORDER_RECEIVED("ORR", "Order Received", "주문접수", listOf(BizProcessType.ORDER)),
    INSTALLATION_COMPLETED("ISC", "Installation Completed", "설치완료", listOf(
        BizProcessType.ORDER,
        BizProcessType.REPLACEMENT
    )),

    // 청구/수납 관련 이벤트
    PAYMENT_BILLING("PBI", "Billing Created", "청구", listOf(BizProcessType.PAYMENT)),
    PAYMENT_BILLING_CANCELLED("PBC", "Payment Billing Cancelled", "청구취소", listOf(BizProcessType.PAYMENT)),
    PAYMENT_RECEIVED("PRE", "Payment Received", "수납", listOf(BizProcessType.PAYMENT)),
    PAYMENT_DEPOSIT("PDE", "Deposit Received", "입금", listOf(BizProcessType.PAYMENT)),
    PAYMENT_CANCELLED("PCA", "Authorization Cancelled", "승인취소", listOf(BizProcessType.CANCEL)),
    PAYMENT_REFUNDED("PRE", "Payment Refund", "환불", listOf(BizProcessType.RETURN)),

    // 취소 관련 이벤트
    ORDER_CANCEL_RECEIVED("OCR", "Cancel Requested", "취소접수", listOf(BizProcessType.CANCEL)),
    ORDER_CANCEL_COMPLETED("OCC", "Order Cancel Completed", "취소완료", listOf(BizProcessType.CANCEL)),

    SALES_CANCELLED("SCD", "Sales Cancelled", "매출취소", listOf(BizProcessType.RETURN)),
    COGS_CANCELLED("CCD", "COGS Cancelled", "매출원가취소", listOf(BizProcessType.RETURN)),

    // 반품/반환 관련 이벤트 (OMS의 OrderItemStatus와 이름이 달라서 주의 필요)
    RETURN_RECEIVED("RTR", "Return Requested", "반품/반환 접수", listOf(BizProcessType.RETURN)),

    // 계약 철회 관련
    WITHDRAWAL_RECEIVED("WRE", "Withdrawal Received", "계약철회 접수", listOf(BizProcessType.WITHDRAWAL)),
    WITHDRAWAL_COMPLETED("WCO", "Withdrawal Completed", "계약철회 완료", listOf(BizProcessType.WITHDRAWAL)),

    // 해지 관련
    TERMINATION_RECEIVED("CTR", "Contract Termination Requested", "해지접수", listOf(BizProcessType.TERMINATION)),
    TERMINATION_COMPLETED("CTC", "Contract Termination Completed", "해지완료", listOf(BizProcessType.TERMINATION)),

    // 교환 관련 이벤트
    REPLACEMENT_RECEIVED("PPR", "Replacement Requested", "교환접수", listOf(BizProcessType.REPLACEMENT)),
    REPLACEMENT_COMPLETED("RPC", "Replacement Completed", "교환완료", listOf(BizProcessType.REPLACEMENT)),

    // A/S 관련 이벤트
    AS_REPAIR("ARP", "Repair", "수리", listOf(BizProcessType.AFTER_SERVICE)),
    AS_RELOCATION("ARL", "Relocation", "이사", listOf(BizProcessType.AFTER_SERVICE)),
    AS_REINSTALL("ARI", "Reinstall", "이전설치", listOf(BizProcessType.AFTER_SERVICE)),
    AS_DISMANTLING("ADM", "Dismantling", "해체", listOf(BizProcessType.AFTER_SERVICE)),

    // 이자 수익 관련
    INTEREST_INCOME_PROCESSED("IIP", "Interest Income Processed", "이자수익", listOf(BizProcessType.FINANCIAL_ASSET, BizProcessType.DEPRECIATION)),
    // 상각 관련 (운용리스 상각)
    DEPRECIATION_PROCESSED("DPP", "Depreciation Processed", "상각", listOf(BizProcessType.RENTAL_ASSET, BizProcessType.DEPRECIATION)),
    // 렌탈자산 폐기
    DISPOSED_PROCESSED("DPR", "Disposed Processed", "폐기", listOf(BizProcessType.RENTAL_ASSET)),

    // 제품 출고/입고 관련 이벤트
    PRODUCT_SHIPPED("PDS", "Product Shipment", "제품출고", listOf(BizProcessType.ORDER, BizProcessType.REPLACEMENT)),
    PRODUCT_RECEIVED("PDR", "Product Receiving", "제품입고", listOf(BizProcessType.RETURN, BizProcessType.REPLACEMENT)),
    FILTER_SHIPPED("FLS", "Filter Shipped", "필터배송", listOf(BizProcessType.SERVICE_SALES)),

    // 재고 폐기
    INVENTORY_DISPOSED("IDP", "Inventory Disposed", "재고폐기", listOf(BizProcessType.DISPOSAL)),



    // 채권관련
    RECEIVABLES_OVERDUE("ROD", "Overdue", "연체", listOf(BizProcessType.RECEIVABLES)),
    BREACHED_CONTRACT_REGISTERED("BCR", "Overdue Temporary Termination Registered", "연체가해약등록",  listOf(BizProcessType.RECEIVABLES)),
    BREACHED_CONTRACT_CANCELLED("BCC", "Temporary Termination Cancelled", "가해약취소", listOf(BizProcessType.RECEIVABLES)),
    TRANSFER_TO_COLLECTION("TTC", "Collection Transfer Registered", "추심전환등록", listOf(BizProcessType.RECEIVABLES)),
    COLLECTION_RECOVERED("CRC", "Collection Recovered", "추심회수", listOf(BizProcessType.RECEIVABLES)),

    // TBD
    PROCURE_GOODS_RECEIVED("PGR", "Goods Receipt", "입고", listOf(BizProcessType.PROCURE, BizProcessType.LOGISTICS)),
    PROCURE_INVOICE_RECEIVED("PVI", "Vendor Invoice Receipt", "공급자청구", listOf(BizProcessType.PROCURE, BizProcessType.LOGISTICS)),
    PROCURE_GOODS_ISSUE("PGI", "Goods Issue", "출고", listOf(BizProcessType.PROCURE, BizProcessType.LOGISTICS)),

    AP_EMPLOYEE_EXPENSE("AEE", "Employee Expense", "비용", listOf(BizProcessType.PAYOUT)),
    AP_VENDOR_EXPENSE("AVE", "Vendor Expense", "거래처 비용", listOf(BizProcessType.PAYOUT)),
    AP_VENDOR_INVOICE("AVI", "Vendor Invoice", "거래처 송장", listOf(BizProcessType.PAYOUT)),
    AP_VENDOR_PAYMENT("AVP", "Vendor Payment", "거래처 지급", listOf(BizProcessType.PAYOUT)),
    ;

    override fun toString(): String {
        return this.name
    }

    companion object {
        fun findByCode(code: String): BizEventType? = entries.find { it.code == code }
        fun findByEngName(engName: String): BizEventType? = entries.find { it.engName.equals(engName, ignoreCase = true) }
        fun findByKorName(korName: String): BizEventType? = entries.find { it.korName == korName }
        fun findByProcessType(processType: BizProcessType): List<BizEventType> = entries.filter { it.processTypes.contains(processType) }
    }
}


@Converter
class BizEventTypeConverter : AttributeConverter<BizEventType, String> {
    override fun convertToDatabaseColumn(attribute: BizEventType?): String? {
        return attribute?.code
    }

    override fun convertToEntityAttribute(dbData: String?): BizEventType? {
        return dbData?.let { code ->
            BizEventType.entries.find { it.code == code }
        }
    }
}


enum class BizProcessType(val code: String, val engName: String, val korName: String, val description: String) {
    ORDER("OR", "Order/Installation", "주문/설치", "제품 주문 및 설치 관련 프로세스"),
    PAYMENT("CP", "Customer Billing/Payment", "청구/수납", "대금 청구 및 수납 관련 프로세스"),
    CANCEL("CA", "Order Cancellation", "취소", "주문 취소 관련 프로세스"),
    RETURN("RT", "Return", "반품/반환", "제품 반품/반환 관련 프로세스"),
    REPLACEMENT("RP", "Replacement", "교환", "제품 교환 관련 프로세스"),
    AFTER_SERVICE("AS", "After Service", "AS", "제품 사후 서비스 관련 프로세스"),
    RECEIVABLES("RV", "RECEIVABLES", "채권관리", "채권 관리 관련 프로세스"),
    WITHDRAWAL("WD", "Withdrawal", "철회", "계약 철회 관련 프로세스"),
    TERMINATION("TM", "Termination", "해지", "계약 해지 관련 프로세스"),
    DEPRECIATION("DP", "Depreciation", "상각", "상각 관련 프로세스"),
    SERVICE_SALES("SS", "Service Sales", "서비스판매", "서비스 판매 관련 프로세스"),
    DISPOSAL("DS", "Disposal", "폐기", "제품 폐기 관련 프로세스"),
    PROCURE("PC", "Procure", "조달", "자재 조달 관련 프로세스"),
    RENTAL_ASSET("RA", "Rental Asset", "렌탈자산", "렌탈 자산 관련 프로세스"),
    FINANCIAL_ASSET("FA", "Financial Asset", "금융자산", "금융자산 관련 프로세스"),
    LOGISTICS("LO", "Logistics", "물류", "물류 관련 프로세스"),
    PAYOUT("PO", "Payouts", "지급", "지급 프로세스"),
    ;

    override fun toString(): String {
        return this.name
    }

    companion object {
        fun findByCode(code: String): BizProcessType? = entries.find { it.code.equals(code, ignoreCase = true) }
        fun findByEngName(engName: String): BizProcessType? = entries.find { it.engName.equals(engName, ignoreCase = true) }
        fun findByKorName(korName: String): BizProcessType? = entries.find { it.korName == korName }
        fun getAllCodes(): List<String> = entries.map { it.code }
        fun getAllKoreanNames(): List<String> = entries.map { it.korName }
    }
}


@Converter
class BizProcessTypeConverter : AttributeConverter<BizProcessType, String> {
    override fun convertToDatabaseColumn(attribute: BizProcessType?): String? {
        return attribute?.code
    }

    override fun convertToEntityAttribute(dbData: String?): BizProcessType? {
        return dbData?.let { code ->
            BizProcessType.entries.find { it.code == code }
        }
    }
}
