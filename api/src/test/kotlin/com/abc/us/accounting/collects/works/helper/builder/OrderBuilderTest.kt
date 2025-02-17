package com.abc.us.accounting.collects.works.helper.builder

import com.abc.us.accounting.collects.helper.OmsApiOrderMutableList
import com.abc.us.accounting.collects.helper.builder.OrderBuilder
import com.abc.us.accounting.collects.works.JsonHelper
import com.abc.us.accounting.supports.converter.JsonConverter
import com.abc.us.generated.models.OrderView
import com.abc.us.generated.models.OrdersViewResponse
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull


class OrderBuilderTest : AnnotationSpec() {


    fun jsonToOrdersView(jsonData: String): List<OrderView> {
        val converter = JsonConverter()
        try {
            val response = converter.toObj(jsonData, OrdersViewResponse::class.java)
            return response?.data?.items?: emptyList()
            //return response?.items ?: emptyList()
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse JSON: ${e.message}", e)
        }
    }

    @Test
    fun `collected order use order json TEST`() {

        val jsonResponse = JsonHelper.readFromFile("orders.json", ChannelBuilderTest::class)
        val ordersView = jsonToOrdersView(jsonResponse)

        val orders = mutableMapOf<String, OrderView>()
        ordersView.forEach { view ->orders[view.orderId] = view}


        val makeOrders = OrderBuilder.build( OmsApiOrderMutableList(ordersView.toMutableList()) )

        makeOrders.shouldNotBeNull()
        makeOrders.forEach { collectOrder ->
            val orderView = orders[collectOrder.orderId]
            orderView.shouldNotBeNull()
            orderView.orderId.shouldBeEqual(collectOrder.orderId!!)
        }
    }
}