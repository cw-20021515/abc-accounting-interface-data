package com.abc.us.accounting.collects.collectable

import com.abc.us.accounting.supports.client.OmsClient
import com.abc.us.generated.models.GetOrderItemsOrderItemSearchParameter
import com.abc.us.generated.models.OrderItemView

open class OrderItemCollectable(
    private val xAbcSdkApikey: String,
    private val sortProperty: String,
    private val pageSize: Int,
    private val omsClient : OmsClient
) : Collectable(sortProperty, pageSize){

    @Throws(Throwable::class)
    fun collects(parameter : GetOrderItemsOrderItemSearchParameter,
                 results: (List<OrderItemView>) -> Boolean) {
        super.visit(
            // 요청 로직 정의
            { pageNumber, pageSize, sortDirection ->
                omsClient.getOrderItems(
                    xAbcSdkApikey = xAbcSdkApikey,
                    current = pageNumber,
                    size = pageSize,
                    direction = sortDirection,
                    sortBy = sortProperty,
                    isMasked = false,
                    orderItemSearch = parameter,
                )
            },
            // 응답 처리 로직 정의
            { response ->
                response?.let { body ->
                    val page = body.data?.let {
                        results(it.items ?: emptyList())
                        val page = body.data!!.page
                        Paging(page!!.current, page.total, page.propertySize, page.totalItems!!)
                    }
                    page
                } ?: throw IllegalStateException("Response body is null")
            }
        )
    }
}