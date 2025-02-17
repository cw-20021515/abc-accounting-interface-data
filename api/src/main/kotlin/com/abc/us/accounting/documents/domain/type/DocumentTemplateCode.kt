package com.abc.us.accounting.documents.domain.type

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * Represents document template codes with their corresponding symbols and descriptions
 * in both Korean and English.
 */
enum class DocumentTemplateCode(
    val symbol: String,
    val koreanText: String,
    val englishText: String
) {
    // One-time Order & Installation Related
    ONETIME_ORDER_RECEIVED("CTOR010", "[일시불] 주문접수", "[ONETIME] Order Received"),
    ONETIME_SALES_RECOGNITION("CTOR030", "[일시불] 설치완료-매출인식", "[ONETIME] Installation Complete - Sales Recognition"),
    ONETIME_COGS_RECOGNITION("CTOR040", "[일시불] 설치완료-매출원가 인식", "[ONETIME] Installation Complete - Cost Recognition"),
    ONETIME_ADVANCE_PAYMENT_OFFSET("CTOR050", "[일시불] 설치완료-선수금 대체", "[ONETIME] Installation Complete - Advance Payment Transfer"),
    ONETIME_PRICE_DIFFERENCE("CTOR060", "[일시불] 설치완료-재고가액 확정", "[ONETIME] Installation Complete - Inventory Value Confirmation"),

    // One-time Logistics Related
    ONETIME_PRODUCT_SHIPPED("CTLO010", "[일시불:출고] 제품출고", "[ONETIME] Product Shipment"),
    ONETIME_PRODUCT_RECEIVED_GRADE_A("CTLO020", "[일시불:반품] 반품입고-A급", "[ONETIME:Return] Return Received - Grade A"),
    ONETIME_PRODUCT_RECEIVED_GRADE_B("CTLO030", "[일시불:반품] 반품입고-B급", "[ONETIME:Return] Return Received - Grade B"),
    ONETIME_INVENTORY_DISPOSED_GRADE_B("CTLO040", "[일시불:폐기] B급자산 폐기", "[ONETIME:Return] Grade B Asset Disposal"),

    // One-time Payment Related
    ONETIME_PAYMENT_RECEIVED("CTCP010", "[일시불:결제] 수납", "[ONETIME:Payment] Payment Received"),
    ONETIME_PAYMENT_VOID("CTCP020", "[일시불:결제] 취소(승인취소)", "[ONETIME:Payment] Cancellation"),
    ONETIME_PAYMENT_REFUND("CTCP030", "[일시불:결제] 환불(매입취소)", "[ONETIME:Payment] Refund"),
    ONETIME_PAYMENT_DEPOSIT("CTCP040", "[일시불] 대금입금", "[ONETIME:Payment] Deposit Received"),

    ONETIME_IN_PERSON_PAYMENT_RECEIVED("CTCP050", "[일시불:현장수납] 결제", "[ONETIME:In-Person Payment] In-Person Payment Received"),
    ONETIME_IN_PERSON_PAYMENT_VOID("CTCP060", "[일시불:현장수납] 취소(승인취소)", "[ONETIME:In-Person Payment] In-Person Payment Cancellation"),
    ONETIME_IN_PERSON_PAYMENT_REFUND("CTCP070", "[일시불:현장수납] 취소(매입취소)", "[ONETIME:In-Person Payment] In-Person Payment Refund"),
    ONETIME_IN_PERSON_PAYMENT_DEPOSIT("CTCP080", "[일시불:현장수납] 입금", "[ONETIME:In-Person Payment] In-Person Payment Deposit Received"),

    // One-time Cancel Related
    ONETIME_CANCEL_RECEIVED("CTCA010", "[일시불:취소] 취소접수", "[ONETIME:Cancel] Cancellation Request"),

    // One-time Return Related
    ONETIME_RETURN_RECEIVED("CTRT010", "[일시불:반품] 반품접수", "[ONETIME:Return] Return Request"),
    ONETIME_RETURN_SALES_CANCELLED("CTRT020", "[일시불:반품] 매출취소", "[ONETIME:Return] Sales Cancellation"),
    ONETIME_RETURN_COGS_CANCELLED("CTRT030", "[일시불:반품] 매출원가취소", "[ONETIME:Return] Sales Cancellation"),
    ONETIME_RETURN_PAYMENT_REFUND("CTRT040", "[일시불:반품] 환불", "[ONETIME:Return] Refund"),
    ONETIME_RETURN_PAYMENT_RECEIVED("CTRT050", "[일시불:반품] 현장수납", "[ONETIME:Return] On-site Collection"),

    // One-time Replacement Related
    ONETIME_REPLACEMENT_RECEIVED("CTRP010", "[일시불:교환] 접수완료", "[ONETIME:Replacement] Request Received"),
    ONETIME_REPLACEMENT_PRODUCT_SHIPPED("CTRP020", "[일시불:교환] 제품출고", "[ONETIME:Replacement] Product Shipment"),
    ONETIME_REPLACEMENT_INSTALLATION_COMPLETED("CTRP030", "[일시불:교환] 설치완료", "[ONETIME:Replacement] Installation Completed"),
    ONETIME_REPLACEMENT_COMPLETED("CTRP040", "[일시불:교환] 교환입고", "[ONETIME:Replacement] Replacement Completed"),

    // One-time After Service Related
    ONETIME_AS_REPAIR("CTAS010", "[일시불:AS] 수리", "[ONETIME:AS] Repair"),
    ONETIME_AS_RELOCATION("CTAS020", "[일시불:AS] 이사", "[ONETIME:AS] Moving"),
    ONETIME_AS_REINSTALL("CTAS030", "[일시불:AS] 이전설치", "[ONETIME:AS] Relocation Installation"),
    ONETIME_AS_DISMANTLING("CTAS040", "[일시불:AS] 해체", "[ONETIME:AS] Dismantling"),

    // Operating Lease Order Related
    OLEASE_ORDER_RECEIVED("COOR010", "[운용리스] 주문접수", "[Operating Lease] Order Received"),
    OLEASE_PRICE_DIFFERENCE("COOR060", "[운용리스] 설치완료-재고가액 확정", "[Operating Lease] Installation Complete - Inventory Value Confirmation"),

    // Operating Lease Payment Related
    OLEASE_PAYMENT_BILLING("COCP010", "[운용리스] 청구", "[Operating Lease] Billing"),
    OLEASE_PAYMENT_BILLING_CANCELLED("COCP020", "[운용리스] 청구 취소", "[Operating Lease] Billing Cancelled"),
    OLEASE_PAYMENT_RECEIVED("COCP030", "[운용리스] 수납", "[Operating Lease] Collection"),
    OLEASE_PAYMENT_DEPOSIT("COCP040", "[운용리스] 입금", "[Operating Lease] Payment Received"),

    // Operating Lease Service Related
    OLEASE_FILTER_SHIPPED("COSS010", "[운용리스:서비스매출] 필터배송", "[Operating Lease:Service] Filter Delivery"),

    // Operating Lease Logistics Related
    OLEASE_PRODUCT_SHIPPED("COLO010", "[운용리스] 제품출고", "[Operating Lease] Product Shipment"),
    OLEASE_PRODUCT_RECEIVED_GRADE_A("COLO020", "[운용리스:반환] 반환입고 A급", "[Operating Lease:Return] Product Received - Grade A"),
    OLEASE_PRODUCT_RECEIVED_GRADE_B("COLO030", "[운용리스:반환] 반환입고 B급", "[Operating Lease:Return] Product Received - Grade B"),
    OLEASE_INVENTORY_DISPOSED_GRADE_B("COLO040", "[운용리스:반환] 반환입고 B급", "[Operating Lease:Return] Grade B Inventory Disposal"),

    // Operating Lease Rental Asset Related
    OLEASE_RENTAL_ASSET_ACQUISITION("CORA010", "[운용리스:렌탈자산] 렌탈자산 인식", "[Operating Lease:Rental Asset] Rental Asset Recognition"),
    OLEASE_RENTAL_ASSET_DEPRECIATION("CORA020", "[운용리스:렌탈자산] 렌탈자산 감가상각", "[Operating Lease:Rental Asset] Rental Asset Depreciation"),
    OLEASE_RENTAL_ASSET_BREACHED_REGISTERED("CORA030", "[운용리스:렌탈자산] 렌탈자산 손상처리", "[Operating Lease:Rental Asset] Rental Asset Impairment"),
    OLEASE_RENTAL_ASSET_BREACHED_CANCELLED("CORA040", "[운용리스:렌탈자산] 렌탈자산 손상처리 취소", "[Operating Lease:Rental Asset] Rental Asset Impairment Cancelled"),
    OLEASE_RENTAL_ASSET_DISPOSED("CORA050", "[운용리스:렌탈자산] 렌탈자산 폐기", "[Operating Lease:Rental Asset] Rental Asset Disposed"),
    OLEASE_RENTAL_ASSET_LOSS_DISPOSED("CORA060", "[운용리스:렌탈자산] 렌탈자산 분실폐기", "[Operating Lease:Rental Asset] Rental Asset Loss Disposed"),

    // Operating Lease Cancel & Withdrawal Related
    OLEASE_CANCEL_RECEIVED("COCA010", "[운용리스:취소] 주문취소 접수", "[Operating Lease:Cancel] Order Cancel Received"),
    OLEASE_CANCEL_COMPLETED("COCA020", "[운용리스:취소] 주문취소 완료", "[Operating Lease:Cancel] Order Cancel Completed"),
    OLEASE_WITHDRAWAL_RECEIVED("COWD010", "[운용리스:철회] 계약철회 접수", "[Operating Lease:Withdrawal] Contract Withdrawal Received"),
    OLEASE_WITHDRAWAL_DISMANTLING("COWD020", "[운용리스:철회] 제품해체 완료", "[Operating Lease:Withdrawal] Contract Withdrawal Dismantling Completed"),
    OLEASE_WITHDRAWAL_COMPLETED("COWD050", "[운용리스:철회] 계약철회 완료", "[Operating Lease:Withdrawal] Contract Withdrawal Completed"),

    // Operating Lease Termination Related
    OLEASE_TERMINATION_RECEIVED("COCT010", "[운용리스:해지] 해지접수", "[Operating Lease:Termination] Termination Request"),
    OLEASE_TERMINATION_DISMANTLING("COCT020", "[운용리스:해지] 해체확정", "[Operating Lease:Termination] Dismantling Confirmation"),
    OLEASE_TERMINATION_COMPLETED("COCT030", "[운용리스:해지] 해지완료", "[Operating Lease:Termination] Termination Completed"),

    // Operating Lease Replacement Related
    OLEASE_REPLACEMENT_RECEIVED("CORP010", "[운용리스:교환] 교환접수", "[Operating Lease:Replacement] Replacement Request"),
    OLEASE_REPLACEMENT_PRODUCT_SHIPPED("CORP020", "[운용리스:교환] 교환출고", "[Operating Lease:Replacement] Replacement Shipment"),
    OLEASE_REPLACEMENT_INSTALLATION_COMPLETED("CORP030", "[운용리스:교환] 설치완료", "[Operating Lease:Replacement] Replacement Complete"),
    OLEASE_REPLACEMENT_COMPLETED("CORP040", "[운용리스:교환] 설치완료", "[Operating Lease:Replacement] Replacement Complete"),

    // Operating Lease Receivables Related
    OLEASE_RECEIVABLE_OVERDUE("CORV010", "[운용리스:채권] 연체", "[Operating Lease:Receivables] Overdue"),
    OLEASE_RECEIVABLE_BREACHED_REGISTERED("CORV020", "[운용리스:채권] 가해약 등록 - 렌탈자산 손상처리", "[Operating Lease:Receivables] Provisional Termination Registration - Rental Asset Damage Processing"),
    OLEASE_RECEIVABLE_BREACHED_CANCELLED("CORV030", "[운용리스:채권] 가해약 취소", "[Operating Lease:Receivables] Provisional Termination Cancellation"),
    OLEASE_RECEIVABLE_TRANSFER_TO_COLLECTION("CORV040", "[운용리스:채권] 추심전환", "[Operating Lease:Receivables] Collection Transfer"),
    OLEASE_RECEIVABLE_COLLECTION_RECOVERED("CORV050", "[운용리스:채권] 추심회수", "[Operating Lease:Receivables] Collection Recovery"),

    // Operating Lease After Service Related
    OLEASE_AS_REPAIR("COAS010", "[운용리스:AS] 수리", "[Operating Lease:AS] Repair"),
    OLEASE_AS_RELOCATION("COAS020", "[운용리스:AS] 이사", "[Operating Lease:AS] Moving"),
    OLEASE_AS_REINSTALL("COAS030", "[운용리스:AS] 이전설치", "[Operating Lease:AS] Relocation Installation"),
    OLEASE_AS_DISMANTLING("COAS040", "[운용리스:AS] 해체", "[Operating Lease:AS] Dismantling"),

    // Finance Lease Order Related
    FLEASE_ORDER_RECEIVED("CFOR010", "[금융리스] 주문접수", "[Finance Lease] Order Received"),
    FLEASE_SALES_RECOGNITION("CFOR020", "[금융리스] 설치완료-재화매출 인식", "[Finance Lease] Installation Complete - Product Sales Recognition"),
    FLEASE_COGS_RECOGNITION("CFOR030", "[금융리스] 설치완료-매출원가 인식", "[Finance Lease] Installation Complete - Cost Recognition"),
    FLEASE_PRICE_DIFFERENCE("CFOR040", "[금융리스] 설치완료-재고가액 확정", "[Finance Lease] Installation Complete - Inventory Value Confirmation"),

    // Finance Lease Payment Related
    FLEASE_PAYMENT_BILLING("CFCP010", "[금융리스] 청구", "[Finance Lease] Billing"),
    FLEASE_PAYMENT_BILLING_CANCELLED("CFCP020", "[금융리스] 청구 취소", "[Finance Lease] Billing Cancelled"),
    FLEASE_PAYMENT_RECEIVED("CFCP030", "[금융리스] 수납", "[Finance Lease] Collection"),
    FLEASE_PAYMENT_DEPOSIT("CFCP040", "[금융리스] 입금", "[Finance Lease] Payment Received"),

    // Finance Lease Service Related
    FLEASE_FILTER_SHIPPED("CFSS010", "[금융리스:서비스매출] 필터배송", "[Finance Lease:Revenue] Service Revenue - Filter Delivery"),

    // Finance Lease Logistics Related
    FLEASE_PRODUCT_SHIPPED("CFLO010", "[금융리스] 제품출고", "[Finance Lease] Product Shipment"),
    FLEASE_PRODUCT_RECEIVED_GRADE_A("CFLO020", "[금융리스:해지] 반환입고-A급", "[Finance Lease:Termination] Return Receipt - Grade A"),
    FLEASE_PRODUCT_RECEIVED_GRADE_B("CFLO030", "[금융리스:해지] 반환입고-B급", "[Finance Lease:Termination] Return Receipt - Grade B"),
    FLEASE_INVENTORY_DISPOSED_GRADE_B("CFLO040", "[금융리스:해지] B급 재고자산 폐기", "[Finance Lease:Termination] Grade B Inventory Disposal"),

    // Finance Lease Financial Asset Related
    FLEASE_FINANCIAL_ASSET_INTEREST_INCOME("CFFA010", "[금융리스:금융자산] 이자수익", "[Finance Lease:Financial Asset] Interest Income"),
    FLEASE_FINANCIAL_ASSET_DISPOSED("CFFA020", "[금융리스:금융자산] 잔여채권 정리", "[Finance Lease:Financial Asset] Dispose of Remaining Receivables"),

    // Finance Lease Cancel Related
    FLEASE_CANCEL_RECEIVED("CFCA010", "[금용리스:취소] 주문취소 접수", "[Finance Lease:Cancel] Order Cancel Received"),
    FLEASE_CANCEL_COMPLETED("CFCA020", "[금용리스:취소] 주문취소 완료", "[Finance Lease:Cancel] Order Cancel Completed"),

    // Finance Lease Withdrawal Related
    FLEASE_WITHDRAWAL_RECEIVED("CFWD010", "[금용리스:철회] 계약철회 접수", "[Finance Lease:Withdrawal] Contract Withdrawal Received"),
    FLEASE_WITHDRAWAL_DISMANTLING("CFWD020", "[금용리스:철회] 제품해체 완료", "[Finance Lease:Withdrawal] Contract Withdrawal Dismantling Completed"),
    FLEASE_WITHDRAWAL_SALES_CANCELLED("CFWD030", "[금용리스:철회] 매출취소 완료", "[Finance Lease:Withdrawal] Sales Cancelled"),
    FLEASE_WITHDRAWAL_COGS_CANCELLED("CFWD040", "[금용리스:철회] 매출원가취소 완료", "[Finance Lease:Withdrawal] COGS Cancelled"),
    FLEASE_WITHDRAWAL_COMPLETED("CFWD050", "[금용리스:철회] 계약철회 완료", "[Finance Lease:Withdrawal] Contract Withdrawal Completed"),

    // Finance Lease Termination Related
    FLEASE_TERMINATION_RECEIVED("CFCT010", "[금용리스:해지] 해지접수", "[Finance Lease:Termination] Termination Request"),
    FLEASE_TERMINATION_DISMANTLING("CFCT020", "[금용리스:해지] 해체확정", "[Finance Lease:Termination] Dismantling Confirmation"),
    FLEASE_TERMINATION_COMPLETED("CFCT030", "[금용리스:해지] 해지완료", "[Finance Lease:Termination] Termination Completed"),

    // Finance Lease Replacement Related
    FLEASE_REPLACEMENT_RECEIVED("CFRP010", "[금융리스:교환] 교환접수", "[Finance Lease:Exchange] Exchange Request"),
    FLEASE_REPLACEMENT_PRODUCT_SHIPPED("CFRP020", "[금융리스:교환] 교환출고", "[Finance Lease:Exchange] Exchange Shipment"),
    FLEASE_REPLACEMENT_INSTALLATION_COMPLETED("CFRP030", "[금융리스:교환] 설치완료", "[Finance Lease:Exchange] Installation Complete"),
    FLEASE_REPLACEMENT_COMPLETED("CFRP040", "[금융리스:교환] 설치완료", "[Finance Lease:Exchange] Installation Complete"),

    // Finance Lease Receivables Related
    FLEASE_RECEIVABLE_OVERDUE("CFRV010", "[금융리스:채권] 연체", "[Finance Lease:Receivables] Overdue"),
    FLEASE_RECEIVABLE_BREACHED_REGISTERED("CFRV020", "[금융리스:채권] 가해약 등록", "[Finance Lease:Receivables] Provisional Termination Registration"),
    FLEASE_RECEIVABLE_BREACHED_CANCELLED("CFRV030", "[금융리스:채권] 가해약 취소", "[Finance Lease:Receivables] Provisional Termination Cancellation"),
    FLEASE_RECEIVABLE_TRANSFER_TO_COLLECTION("CFRV040", "[금융리스:채권] 추심전환", "[Finance Lease:Receivables] Collection Transfer"),
    FLEASE_RECEIVABLE_COLLECTION_RECOVERED("CFRV050", "[금융리스:채권] 추심회수", "[Finance Lease:Receivables] Collection Recovery"),

    // Finance Lease After Service Related
    FLEASE_AS_REPAIR("CFAS010", "[금융리스:AS] 수리", "[Finance Lease:AS] Repair"),
    FLEASE_AS_RELOCATION("CFAS020", "[금융리스:AS] 이사", "[Finance Lease:AS] Moving"),
    FLEASE_AS_REINSTALL("CFAS030", "[금융리스:AS] 이전설치", "[Finance Lease:AS] Relocation Installation"),
    FLEASE_AS_DISMANTLING("CFAS040", "[금융리스:AS] 해체", "[Finance Lease:AS] Dismantling"),

    // Logistics Related
    LOGISTICS_GOODS_INVOICE_RECEIVED("VLGI010", "[물류] 상품 비용", "[Logistics] Goods Cost"),
    LOGISTICS_FORWARDER_INVOICE_RECEIVED("VLGI020", "[물류] 상품 부대비용(관세/운임)", "[Logistics] Goods Extra Cost"),
    LOGISTICS_GOODS_RECEIVED("VLGI030", "[물류] 제품 입고", "[Logistics] Goods Receipts"),

    // Account Payable Related
    ACCOUNT_PAYABLE_EMPLOYEE_EXPENSE("VPAP010", "[지급] 개인경비 지급요청", "[Payout] Employee Expense"),
    ACCOUNT_PAYABLE_VENDOR_EXPENSE("VPAP050", "[지급] 공급처 지급요청", "[Payout] Vendor Expense"),
    ACCOUNT_PAYABLE_VENDOR_INVOICE("VPAP060", "[지급] 공급처 지급요청", "[Payout] Vendor Invoice"),
    ACCOUNT_PAYABLE_VENDOR_PAYMENT("VPAP070", "[지급] 공급처 지급요청", "[Payout] Vendor Payment"),
    ;

    override fun toString(): String {
        return this.name
    }

    companion object {
        /**
         * Find DocumentTemplateCode by symbol
         * @param symbol Document template symbol to search for
         * @return DocumentTemplateCode if found, null otherwise
         */
        fun findBySymbolOrNull(symbol: String): DocumentTemplateCode? {
            return entries.find { it.symbol == symbol }
        }

        fun findBySymbol(symbol: String): DocumentTemplateCode {
            val lookup = findBySymbolOrNull(symbol)
            return lookup ?: throw IllegalArgumentException("DocumentTemplateCode not found for symbol: $symbol")
        }

        /**
         * Check if symbol exists
         * @param symbol Document template symbol to check
         * @return true if symbol exists, false otherwise
         */
        fun symbolExists(symbol: String): Boolean =
            entries.any { it.symbol == symbol }

        /**
         * Get all codes for a specific category
         * @param category Category prefix to filter by (e.g., "ONETIME", "OLEASE", "FLEASE")
         * @return List of DocumentTemplateCode matching the category
         */
        fun getCodesByCategory(category: String): List<DocumentTemplateCode> =
            entries.filter { it.name.startsWith(category) }

        fun findAllBySalesType(salesType: SalesType): List<DocumentTemplateCode> {
            return when (salesType) {
                SalesType.ONETIME -> {
                    entries.filter { it.name.startsWith("ONETIME") }
                }

                SalesType.OPERATING_LEASE -> {
                    entries.filter { it.name.startsWith("OLEASE") }
                }

                SalesType.FINANCIAL_LEASE -> {
                    entries.filter { it.name.startsWith("FLEASE") }
                }

                SalesType.MEMBERSHIP -> TODO()
                SalesType.CUSTOMER_SERVICE -> TODO()
            }
        }
    }
}


@Converter
class DocumentTemplateCodeConverter : AttributeConverter<DocumentTemplateCode, String> {
    override fun convertToDatabaseColumn(attribute: DocumentTemplateCode?): String? {
        return attribute?.symbol
    }

    override fun convertToEntityAttribute(dbData: String?): DocumentTemplateCode? {
        return dbData?.let { symbol ->
            DocumentTemplateCode.entries.find { it.symbol == symbol }
        }
    }
}


@Converter
class DocumentTemplateCodeListConverter : AttributeConverter<List<DocumentTemplateCode>, String> {
    override fun convertToDatabaseColumn(attribute: List<DocumentTemplateCode>?): String? {
        return attribute?.joinToString(",")
    }

    override fun convertToEntityAttribute(dbData: String?): List<DocumentTemplateCode> {
        return dbData?.split(",")
            ?.filter { it.isNotBlank() }
            ?.map { DocumentTemplateCode.valueOf(it.trim()) }
            ?: emptyList()
    }
}
