package com.abc.us.accounting.collects.works.helper.builder

import com.abc.us.accounting.collects.helper.OmsApiContractMutableList
import com.abc.us.accounting.collects.helper.builder.ContractBuilder
import com.abc.us.accounting.collects.works.JsonHelper
import com.abc.us.accounting.supports.converter.JsonConverter
import com.abc.us.generated.models.ContractView
import com.abc.us.generated.models.ContractsViewResponse
import com.abc.us.generated.models.Order
import com.abc.us.generated.models.OrderView
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.nulls.shouldNotBeNull

class ContractBuilderTest : AnnotationSpec() {

    fun jsonToContractView(jsonData: String): List<ContractView> {
        val converter = JsonConverter()
        try {
            val response = converter.toObj(jsonData, ContractsViewResponse::class.java)
            return response?.data?.items?: emptyList()
            //return response?.items ?: emptyList()
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse JSON: ${e.message}", e)
        }
    }

    fun viewToOrder(view : OrderView) : Order {
        return Order(
            channelOrderId = view.channelOrderId,
            orderProductType = view.orderProductType,
            orderId = view.orderId,
            orderItems = view.orderItems,
            channel = view.channel,
            customerInformation = view.customerInformation,
            createTime = view.createTime,
            referrerCode = view.referrerCode,
            orderCreateTime = view.orderCreateTime,
            orderUpdateTime = view.orderUpdateTime,
            deliveryAddress = view.deliveryAddress,
            payment = view.payment,
            updateTime = view.updateTime
        )
    }

//    fun collectOrderItems(histories : List<ResourceHistory>) : MutableMap<String,OrderItem> {
//        var orderItems = mutableMapOf<String,OrderItem>()
//        val converter = JsonConverter()
//        histories.forEach { history ->
//            history.newValue?.let { omsValue ->
//                val entityClass = when(history.resourceName) {
//                    ResourceName.ORDER_ITEM -> converter.toObj(omsValue, OrderItem::class.java)
//                    ResourceName.CUSTOMER -> converter.toObj(omsValue, Customer::class.java)
////                    ResourceName.SERVICE_FLOW -> converter.toObj(omsValue, OmsServiceFlow::class.java)
////                    ResourceName.MATERIAL -> converter.toObj(omsValue, OmsMaterial::class.java)
//                    else -> null
//                }
//                if(entityClass!=null && entityClass is OrderItem) {
//                    orderItems[entityClass.orderItemId!!] = entityClass
//                }
//            }
//        }
//        return orderItems
//    }


    @Test
    fun `collected contract use order`() {

        val jsonResponse = JsonHelper.readFromFile("contract.json", ContractBuilderTest::class)
        val contractViews = jsonToContractView(jsonResponse)
        //val histories = JsonHelper.jsonToResourceHistory(jsonResponse)
        //histories.shouldNotBeNull()

//        val orderItems = collectOrderItems(histories)
//        orderItems.shouldNotBeNull()
//        orderItems.isEmpty() shouldBeEqual false


        val makeContracts = ContractBuilder.builds(OmsApiContractMutableList(contractViews.toMutableList()))
        makeContracts.shouldNotBeNull()

//        makeContracts.forEach { contractId, contract ->
//            contract.rentalCode
//        }
    }
}
