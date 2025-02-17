package com.abc.us.accounting.collects.domain.type

/**
 * - [BillingTypeEnum] - [서비스 비용 정산 방식]
 * - `ON_SITE_PAYMENT`     - [On Site Payment]
 * - [현장 결제]   - `INVOICE_BILLING`
 * - [Invoice Billing]     - [청구서 기반 결제]
 * Values: ON_SITE_PAYMENT,INVOICE_BILLING
 */
enum class BillingTypeEnum(val value: kotlin.String) {

    ON_SITE_PAYMENT("ON_SITE_PAYMENT"),
    INVOICE_BILLING("INVOICE_BILLING"),
    NONE("NONE")
}
