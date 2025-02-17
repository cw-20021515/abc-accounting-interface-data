package com.abc.us.accounting.iface.domain.type.oms

/**
 * - [MaterialShippingMethodType] - [자재 출고 유형]   - `INSTALL`     - [Install]     - [설치형]   - `COURIER`     - [Courier]     - [배송형]
 * Values: INSTALL,COURIER
 */
enum class IfMaterialShippingMethodType(val value: kotlin.String) {

    INSTALL("INSTALL"),
    COURIER("COURIER")
}