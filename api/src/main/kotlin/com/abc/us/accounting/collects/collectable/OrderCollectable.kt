package com.abc.us.accounting.collects.collectable

import com.abc.us.accounting.supports.client.OmsClient
import com.abc.us.generated.models.GetOrdersOrderSearchParameter
import com.abc.us.generated.models.Order
import com.abc.us.generated.models.OrderView
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

open class OrderCollectable(
    private val xAbcSdkApikey: String,
    private val sortProperty: String,
    private val pageSize: Int,
    private val omsClient : OmsClient
) : Collectable(sortProperty, pageSize){

    @Throws(Throwable::class)
    fun collects(parameter : GetOrdersOrderSearchParameter,
                 results: (List<OrderView>) -> Boolean) {
        super.visit(
            // 요청 로직 정의
            { pageNumber, pageSize, sortDirection ->
                omsClient.getOrders(
                    xAbcSdkApikey = xAbcSdkApikey,
                    current = pageNumber,
                    size = pageSize,
                    direction = sortDirection,
                    sortBy = sortProperty,
                    orderSearch = parameter
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

    @Throws(Throwable::class)
    fun getOrderById(orderId : String,result: (Order) -> Boolean) {
        val responseEntity = omsClient.getOrderById(
            xAbcSdkApikey = xAbcSdkApikey,
            orderId = orderId,
            includeServiceFlow = true)
        if (responseEntity.statusCode == HttpStatus.OK) {
            responseEntity.body?.let { it.data?.let { order -> result(order)} }
        } else {
            throw ResponseStatusException(responseEntity.statusCode, responseEntity.statusCode.toString())
        }
    }
}