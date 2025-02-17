package com.abc.us.accounting.collects.works.helper.builder

import com.abc.us.accounting.collects.helper.builder.ChannelBuilder
import com.abc.us.accounting.collects.works.JsonHelper
import com.abc.us.accounting.supports.converter.JsonConverter
import com.abc.us.generated.models.OrderView
import com.abc.us.generated.models.OrdersViewResponse
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.nulls.shouldNotBeNull

class ChannelBuilderTest : AnnotationSpec() {

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
    fun `collected channel use order`() {


        val jsonResponse = JsonHelper.readFromFile("orders.json", ChannelBuilderTest::class)
        val ordersView = jsonToOrdersView(jsonResponse)

        val orders = mutableMapOf<String,OrderView>()
        ordersView.forEach { view ->orders[view.orderId] = view}

        val makeChannels = ChannelBuilder.build(ordersView.toMutableList())

        makeChannels.shouldNotBeNull()
//        orders.forEach { (orderId, order) ->
//
//            val channel = makeChannels[orderId]
//            channel.shouldNotBeNull()
//            channel.relation!!.value!!.shouldBeEqual(orderId)
//        }
    }
}
