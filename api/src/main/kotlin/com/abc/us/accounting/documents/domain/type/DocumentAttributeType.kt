package com.abc.us.accounting.documents.domain.type

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

enum class DocumentAttributeType(val symbol: String, val engName: String, val korName: String, val category: DocumentAttributeCategory) {
    COST_CENTER("cost_center", "Cost Center", "코스트센터", DocumentAttributeCategory.ASSIGNMENT),
    PROFIT_CENTER("profit_center", "Profit Center", "프로핏센터", DocumentAttributeCategory.ASSIGNMENT),
    BUSINESS_AREA("business_area", "Business Area", "사업영역", DocumentAttributeCategory.ASSIGNMENT),
    SEGMENT("segment", "Segment", "세그먼트", DocumentAttributeCategory.ASSIGNMENT),
    PROJECT("project", "Project", "프로젝트", DocumentAttributeCategory.ASSIGNMENT),

    CUSTOMER_ID("customer_id", "Customer ID", "고객 ID", DocumentAttributeCategory.ATTRIBUTE),
    ORDER_ID("order_id", "Order ID", "주문ID", DocumentAttributeCategory.ATTRIBUTE),
    ORDER_ITEM_ID("order_item_id", "Order Item ID", "주문아이템ID", DocumentAttributeCategory.ATTRIBUTE),
    CONTRACT_ID("contract_id", "Contract ID", "계약ID", DocumentAttributeCategory.ATTRIBUTE),
    SERIAL_NUMBER("serial_number", "Serial Number", "일련번호", DocumentAttributeCategory.ATTRIBUTE),

    INSTALL_ID("install_id", "Install Id", "설치ID", DocumentAttributeCategory.ATTRIBUTE),
    BRANCH_ID("branch_id", "Branch Id", "지점ID", DocumentAttributeCategory.ATTRIBUTE),
    WAREHOUSE_ID("warehouse_id", "Warehouse Id", "창고ID", DocumentAttributeCategory.ATTRIBUTE),
    TECHNICIAN_ID("technician_id", "Technician Id", "CT ID", DocumentAttributeCategory.ATTRIBUTE),

    SALES_TYPE("sales_type", "Sales Type", "판매유형", DocumentAttributeCategory.ATTRIBUTE),
    SALES_ITEM("sales_item", "Sales Item", "판매항목", DocumentAttributeCategory.ATTRIBUTE),
    RENTAL_CODE("rental_code", "Rental Code", "렌탈코드", DocumentAttributeCategory.ATTRIBUTE),
    LEASE_TYPE("lease_type", "Lease Type", "리스유형", DocumentAttributeCategory.ATTRIBUTE),
    CONTRACT_DURATION("contract_duration", "Contract Duration", "계약기간", DocumentAttributeCategory.ATTRIBUTE),
    COMMITMENT_DURATION("commitment_duration", "Commitment Duration", "약정기간", DocumentAttributeCategory.ATTRIBUTE),
    CURRENT_TERM("current_term", "Current Term", "계약회차", DocumentAttributeCategory.ATTRIBUTE),

    CHANNEL_ID("channel_id", "Channel Id", "채널ID", DocumentAttributeCategory.ATTRIBUTE),
    CHANNEL_TYPE("channel_type", "Channel Type", "채널유형", DocumentAttributeCategory.ATTRIBUTE),
    CHANNEL_NAME("channel_name", "Channel Name", "채널명", DocumentAttributeCategory.ATTRIBUTE),
    CHANNEL_DETAIL("channel_detail", "Channel Detail", "채널상세", DocumentAttributeCategory.ATTRIBUTE),
    REFERRAL_CODE("referral_code", "Referral Code", "추천인코드", DocumentAttributeCategory.ATTRIBUTE),

    PAMENT_ID("payment_id", "Customer Payment Id", "고객 결제 ID", DocumentAttributeCategory.ATTRIBUTE),

    CHARGE_ID("charge_id", "Customer Charge Id", "고객 청구 ID", DocumentAttributeCategory.ATTRIBUTE),
    INVOICE_ID("invoice_id", "Customer Invoice Id", "고객 청구서 ID", DocumentAttributeCategory.ATTRIBUTE),

    VENDOR_ID("vendor_id", "Vendor ID", "공급업체 ID", DocumentAttributeCategory.ATTRIBUTE),
    PAYOUT_ID("payout_id", "Payout ID", "지급 ID", DocumentAttributeCategory.ATTRIBUTE),
    VENDOR_INVOICE_ID("vendor_invoice_id", "Vendor Invoice ID", "공급업체 송장 ID", DocumentAttributeCategory.ATTRIBUTE),
    PURCHASE_ORDER("purchase_order", "Purchase Order", "구매주문", DocumentAttributeCategory.ATTRIBUTE),
    MATERIAL_ID("material_id", "Material ID", "자재 ID", DocumentAttributeCategory.ATTRIBUTE),
    MATERIAL_TYPE("material_type", "Material Type", "자재유형", DocumentAttributeCategory.ATTRIBUTE),
    MATERIAL_CATEGORY_CODE("material_category_code", "Material Category Code", "자재분류코드", DocumentAttributeCategory.ATTRIBUTE),
    PRODUCT_CATEGORY("product_category", "Product Category", "제품분류", DocumentAttributeCategory.ATTRIBUTE),
    MATERIAL_SERIES_CODE("material_series_code", "Material Series Code", "품목코드", DocumentAttributeCategory.ATTRIBUTE),
    INSTALLATION_TYPE("installation_type", "Installation Type", "설치유형", DocumentAttributeCategory.ATTRIBUTE),
    FILTER_TYPE("filter_type", "Filter Type", "필터유형", DocumentAttributeCategory.ATTRIBUTE),
    FEATURE_TYPE("feature_type", "Feature Type", "특징유형", DocumentAttributeCategory.ATTRIBUTE),
}




@Converter
class DocumentAttributeTypeConverter : AttributeConverter<DocumentAttributeType, String> {
    override fun convertToDatabaseColumn(attribute: DocumentAttributeType?): String? {
        return attribute?.name
    }

    override fun convertToEntityAttribute(dbData: String?): DocumentAttributeType? {
        return dbData?.let { name ->
            DocumentAttributeType.entries.find { it.name == name }
        }
    }
}
