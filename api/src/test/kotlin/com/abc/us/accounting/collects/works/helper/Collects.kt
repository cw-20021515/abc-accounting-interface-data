package com.abc.us.accounting.collects.works.helper

import com.abc.us.accounting.supports.converter.JsonConverter
import com.abc.us.generated.models.OrderItem
import com.abc.us.generated.models.ResourceHistory

class Collects {

    companion object {
        fun orderItems( resourceHistories : List<ResourceHistory>) : MutableMap<String, OrderItem> {
            val converter = JsonConverter()
            var orderItems = mutableMapOf<String, OrderItem>()
            resourceHistories.forEach { history ->
                history.newValue?.let { omsValue ->
                    val omsOrderItem = converter.toObj(omsValue, OrderItem::class.java)
                    omsOrderItem?.let { item -> orderItems[item.orderItemId] = item}
                }
            }
            return orderItems
        }
    }

}