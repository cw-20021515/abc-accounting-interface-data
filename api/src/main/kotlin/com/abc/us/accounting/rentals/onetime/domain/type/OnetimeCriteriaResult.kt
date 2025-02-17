package com.abc.us.accounting.rentals.onetime.domain.type

enum class OnetimeCriteriaResult {
    SUCCEEDED,
    FAILED,

    ORDER_ITEM_TYPE_IS_NOT_ONETIME,
    ORDER_ITEM_MATERIAL_IS_NULL,

    MATERIAL_IS_NULL,
    MATERIAL_TYPE_IS_NOT_PRODUCT,

    ORDER_STATUS_IS_NOT_RECEIVED,
    ORDER_STATUS_IS_NOT_BOOKING_CONFIRMED,
    ORDER_STATUS_IS_NOT_INSTALL_COMPLETED,

    SERVICE_FLOW_IS_NULL,
    SERVICE_TYPE_IS_NOT_INSTALL,
    SERVICE_FLOW_STATUS_IS_NOT_SERVICE_SCHEDULED,
    SERVICE_FLOW_STATUS_IS_NOT_SERVICE_COMPLETED,

    INSTALL_ITEM_IS_NULL,
    INSTALL_TIME_IS_NULL,

    INVENTORY_VALUATION_IS_NULL,
    ;

    fun isSucceeded() = this == SUCCEEDED
    fun isFailed() = !isSucceeded()

    companion object {
        fun failedResults(vararg items: OnetimeCriteriaResult): List<OnetimeCriteriaResult> {
            val results = mutableListOf<OnetimeCriteriaResult>()
            items.forEach { item ->
                if ( item.isFailed()) {
                    results.add(item)
                }
            }
            return results
        }
    }

}