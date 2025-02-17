package com.abc.us.accounting.collects.helper.builder

import com.abc.us.accounting.collects.domain.entity.collect.CollectLocation
import com.abc.us.accounting.collects.domain.entity.collect.CollectOrder
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableLocation
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableName
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableRelation
import com.abc.us.generated.models.DeliveryAddress
import com.abc.us.generated.models.OrderView

class LocationBuilder {
    companion object {
        fun makeLocation(order: OrderView, address: DeliveryAddress): CollectLocation {
            return CollectLocation().apply {
                relation = EmbeddableRelation().apply {
                    entity = CollectOrder::class.simpleName
                    field = "order_id"
                    value = order.orderId
                }
                name = EmbeddableName().apply {
                    //nameTitle = address.title
                    firstName = address.firstName
                    //nameMiddle = address.middleName
                    lastName = address.lastName
                    primaryEmail = address.email
                    primaryPhone = address.phone
                    mobile = address.mobile
                }
                location = EmbeddableLocation().apply {
                    state = address.state
                    city = address.city
                    address1 = address.address1
                    address2 = address.address2
                    zipCode = address.zipcode
                    locationRemark = address.remark
                }
            }
        }

        fun build(originOrders: MutableList<OrderView>): MutableList<CollectLocation> {

            val contracts = mutableListOf<CollectLocation>()
            originOrders.forEach { order ->
                order.deliveryAddress?.let { addr ->
                    contracts.add(makeLocation(order, addr))
                }
            }
            return contracts
        }
    }

}