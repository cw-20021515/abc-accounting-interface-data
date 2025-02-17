package com.abc.us.accounting.collects.works

import com.abc.us.accounting.supports.client.OmsClient
import com.abc.us.accounting.supports.converter.JsonConverter
import com.abc.us.generated.models.*
import org.springframework.http.ResponseEntity

class OmsClientStub : OmsClient{

    override fun getOrders(
        xAbcSdkApikey: String,
        current: Int,
        size: Int,
        sortBy: String,
        direction: String,
        orderSearch: GetOrdersOrderSearchParameter?
    ): ResponseEntity<OrdersViewResponse> {
        val converter = JsonConverter()
        val jsonData = JsonHelper.readFromFile("orders.json", OmsClientStub::class)
        return ResponseEntity.ok(converter.toObj(jsonData, OrdersViewResponse::class.java)!!)
    }
    override fun getCustomers(
        xAbcSdkApikey: String,
        current: Int,
        size: Int,
        sortBy: String,
        direction: String,
        customerSearch: GetCustomersCustomerSearchParameter?
    ): ResponseEntity<CustomerListResponse> {
        val converter = JsonConverter()
        val jsonData = JsonHelper.readFromFile("customer.json", OmsClientStub::class)
        return ResponseEntity.ok(converter.toObj(jsonData, CustomerListResponse::class.java)!!)
    }


    override fun getOrderById(
        xAbcSdkApikey: String,
        orderId: String,
        includeServiceFlow: Boolean
    ): ResponseEntity<OrderWithItemResponse> {
        TODO("Not yet implemented")
    }

    override fun getOrderItems(
        xAbcSdkApikey: String,
        orderItemSearch: GetOrderItemsOrderItemSearchParameter,
        current: Int,
        size: Int,
        sortBy: String,
        direction: String,
        isMasked: Boolean
    ): ResponseEntity<OrderItemsViewResponse> {
        val converter = JsonConverter()
        val jsonData = JsonHelper.readFromFile("order_items.json", OmsClientStub::class)
        return ResponseEntity.ok(converter.toObj(jsonData, OrderItemsViewResponse::class.java)!!)
    }

    override fun getCustomerById(
        xAbcSdkApikey: String,
        customerId: String
    ): ResponseEntity<CustomerWithDetailResponse> {
        TODO("Not yet implemented")
    }

    override fun getContractById(
        xAbcSdkApikey: String,
        contractId: String
    ): ResponseEntity<ContractWithDetailResponse> {
        TODO("Not yet implemented")
    }

    override fun getMaterials(
        xAbcSdkApikey: String,
        current: Int,
        size: Int,
        direction: String,
        sortBy: String
//        startDate: LocalDate?,
//        endDate: LocalDate?
    ): ResponseEntity<MaterialListResponse> {
        val converter = JsonConverter()
        val jsonData = JsonHelper.readFromFile("materials.json", OmsClientStub::class)
        return ResponseEntity.ok(converter.toObj(jsonData, MaterialListResponse::class.java)!!)
    }

    override fun getMaterialById(xAbcSdkApikey: String, materialId: String): ResponseEntity<MaterialResponse> {
        TODO("Not yet implemented")
    }

    override fun getServiceFlowList(
        xAbcSdkApikey: String,
        serviceFlowSearch: GetServiceFlowListServiceFlowSearchParameter,
        current: Int,
        size: Int,
        sortBy: String,
        direction: String
    ): ResponseEntity<ServiceFlowPageListResponse> {
        val converter = JsonConverter()
        val jsonData = JsonHelper.readFromFile("service-flows.json", OmsClientStub::class)
        return ResponseEntity.ok(converter.toObj(jsonData, ServiceFlowPageListResponse::class.java)!!)
    }

    override fun getResourceHistory(
        xAbcSdkApikey: String,
        resourceHistorySearch: GetResourceHistoryResourceHistorySearchParameter,
        current: Int,
        size: Int,
        sortBy: String,
        direction: String
    ): ResponseEntity<ResourceHistoryListResponse> {
        val converter = JsonConverter()

        val fileName = when(resourceHistorySearch.resourceName) {
            ResourceName.ORDER_ITEM -> {
                when(resourceHistorySearch.operation) {
                    ResourceHistoryOperation.INSERT -> "resource-history-insert-order-item.json"
                    ResourceHistoryOperation.UPDATE -> "resource-history-update-order-item.json"
                    ResourceHistoryOperation.DELETE -> "resource-history-delete-order-item.json"
                    null -> TODO()
                }
            }
            ResourceName.CUSTOMER -> "resource-history-customer.json"
            ResourceName.CUSTOMER_SERVICE_TICKET ->"resource-history-customer-service_ticker.json"
            ResourceName.CUSTOMER_INQUIRY ->"resource-history-customer-inquiry.json"
            ResourceName.SERVICE_FLOW -> "resource-history-service-flow.json"
            ResourceName.MATERIAL ->"resource-history-material.json"
            ResourceName.UNKNOWN -> TODO()
            null -> TODO()
        }

        val jsonData = JsonHelper.readFromFile(fileName, OmsClientStub::class)
        return ResponseEntity.ok(converter.toObj(jsonData, ResourceHistoryListResponse::class.java)!!)
    }

    override fun getBillingCharges(
        xAbcSdkApikey: String,
        chargeSearch: GetBillingChargesChargeSearchParameter,
        current: Int,
        size: Int,
        sortBy: String,
        direction: String
    ): ResponseEntity<OmsChargesResponse> {
        val converter = JsonConverter()
        val jsonData = JsonHelper.readFromFile("charges.json", OmsClientStub::class)
        return ResponseEntity.ok(converter.toObj(jsonData, OmsChargesResponse::class.java)!!)
    }
}