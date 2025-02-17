package com.abc.us.accounting.supports.client

import com.abc.us.generated.models.*
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.cloud.openfeign.SpringQueryMap
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@FeignClient(
    name = "oms"
    ,configuration = [IgnoreSslFeignClientConfig::class]
)
interface OmsClient {

    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/oms/v1/orders"],
        produces = ["application/json"]
    )
    @Throws(Throwable::class)
    fun getOrders(
        @Parameter(description = "서비스 인증 API KEY", `in` = ParameterIn.HEADER, required = true)
        @RequestHeader(value = "x-abc-sdk-apikey", required = true)
        xAbcSdkApikey: String,

        @Valid
        @Min(1)
        @Parameter(description = "The number of results page",schema = Schema(defaultValue = "1"))
        @RequestParam(value = "current", required = false, defaultValue = "1")
        current: Int,

        @Valid
        @Parameter(description = "The number of results in a single page",schema = Schema(defaultValue = "10"))
        @RequestParam(value = "size", required = false, defaultValue = "10")
        size: Int,

        @Valid
        @Parameter(description = "",schema = Schema(allowableValues = ["updateTime", "createTime"], defaultValue = "createTime"))
        @RequestParam(value = "sortBy",required = false,defaultValue = "createTime")
        sortBy: String,

        @Valid
        @Parameter(description = "",schema = Schema(allowableValues = ["ASC", "DESC"], defaultValue = "DESC"))
        @RequestParam(value = "direction",required = false,defaultValue = "DESC")
        direction: String,

        @Valid
        @SpringQueryMap
        @Parameter(description = "주문검색조건")
        orderSearch: GetOrdersOrderSearchParameter?
    ): ResponseEntity<OrdersViewResponse>

    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/oms/v1/orders/{orderId}"],
        produces = ["application/json"]
    )
    @Throws(Throwable::class)
    fun getOrderById(
        @Parameter(description = "서비스 인증 API KEY", `in` = ParameterIn.HEADER, required = true)
        @RequestHeader(value = "x-abc-sdk-apikey", required = true)
        xAbcSdkApikey: String,

        @Parameter(description = "oms 주문아이디", required = true)
        @PathVariable("orderId")
        orderId: String,

        @Valid
        @Parameter(description = "Boolean flag to include service flow details", schema = Schema(defaultValue = "false"))
        @RequestParam(value = "includeServiceFlow", required = false, defaultValue = "false")
        includeServiceFlow: Boolean
    ): ResponseEntity<OrderWithItemResponse>

    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/oms/v1/orders/order-items"],
        produces = ["application/json"]
    )
    fun getOrderItems(
        @Parameter(description = "서비스 인증 API KEY", `in` = ParameterIn.HEADER, required = true)
        @RequestHeader(value = "x-abc-sdk-apikey", required = true)
        xAbcSdkApikey: String,

        @NotNull
        @Parameter(description = "주문상세검색조건", required = true)
        @Valid
        @SpringQueryMap
        orderItemSearch: GetOrderItemsOrderItemSearchParameter,

        @Min(1)
        @Parameter(description = "The number of results page", schema = Schema(defaultValue = "1"))
        @Valid
        @RequestParam(value = "current", required = false, defaultValue = "1")
        current: Int,

        @Parameter(description = "The number of results in a single page", schema = Schema(defaultValue = "10"))
        @Valid
        @RequestParam(value = "size", required = false, defaultValue = "10")
        size: Int,

        @Parameter(description = "", schema = Schema(allowableValues = ["updateTime", "createTime"], defaultValue = "createTime"))
        @Valid
        @RequestParam(value = "sortBy", required = false, defaultValue = "createTime")
        sortBy: String,

        @Parameter(description = "", schema = Schema(allowableValues = ["ASC", "DESC"], defaultValue = "DESC"))
        @Valid
        @RequestParam(value = "direction", required = false, defaultValue = "DESC")
        direction: String,

        @Parameter(description = "개인정보 마스킹 여부 <br/> `true`: 개인정보 마스킹 <br/> `false`: 개인정보 마스킹 해제", schema = Schema(defaultValue = "true"))
        @Valid
        @RequestParam(value = "isMasked", required = false, defaultValue = "true")
        isMasked: Boolean): ResponseEntity<OrderItemsViewResponse>


    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/oms/v1/customers"],
        produces = ["application/json"]
    )
    fun getCustomers(
        @Parameter(description = "서비스 인증 API KEY", `in` = ParameterIn.HEADER, required = true)
        @RequestHeader(value = "x-abc-sdk-apikey", required = true)
        xAbcSdkApikey: String,

        @Valid
        @Min(1)
        @Parameter(description = "The number of results page", schema = Schema(defaultValue = "1"))
        @RequestParam(value = "current", required = false, defaultValue = "1")
        current: Int,

        @Valid
        @Parameter(description = "The number of results in a single page", schema = Schema(defaultValue = "10"))
        @RequestParam(value = "size", required = false, defaultValue = "10")
        size: Int,

        @Valid
        @Parameter(description = "", schema = Schema(allowableValues = ["updateTime", "createTime"], defaultValue = "createTime"))
        @RequestParam(value = "sortBy", required = false, defaultValue = "createTime")
        sortBy: String,

        @Valid
        @Parameter(description = "", schema = Schema(allowableValues = ["ASC", "DESC"], defaultValue = "DESC"))
        @RequestParam(value = "direction", required = false, defaultValue = "DESC")
        direction: String,

        @Parameter(description = "고객 검색조건")
        @Valid
        @SpringQueryMap
        customerSearch: GetCustomersCustomerSearchParameter?
    ): ResponseEntity<CustomerListResponse>

    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/oms/v1/customers/{customerId}"],
        produces = ["application/json"]
    )
    @Throws(Throwable::class)
    fun getCustomerById(
        @Parameter(description = "서비스 인증 API KEY", `in` = ParameterIn.HEADER, required = true)
        @RequestHeader(value = "x-abc-sdk-apikey", required = true)
        xAbcSdkApikey: String,

        @Parameter(description = "고객 ID", required = true)
        @PathVariable("customerId")
        customerId: String
    ): ResponseEntity<CustomerWithDetailResponse>


    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/oms/v1/contracts/{contractId}"],
        produces = ["application/json"]
    )
    @Throws(Throwable::class)
    fun getContractById(
        @Parameter(description = "서비스 인증 API KEY", `in` = ParameterIn.HEADER, required = true)
        @RequestHeader(value = "x-abc-sdk-apikey", required = true)
        xAbcSdkApikey: String,

        @Parameter(description = "oms 계약", required = true)
        @PathVariable("contractId")
        contractId: String
    ): ResponseEntity<ContractWithDetailResponse>

    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/oms/v1/materials"],
        produces = ["application/json"]
    )
    @Throws(Throwable::class)
    fun getMaterials(
        @Parameter(description = "서비스 인증 API KEY", `in` = ParameterIn.HEADER, required = true)
        @RequestHeader(value = "x-abc-sdk-apikey", required = true)
        xAbcSdkApikey: String,

        @Valid
        @Min(1)
        @Parameter(description = "The number of results page",schema = Schema(defaultValue = "1"))
        @RequestParam(value = "current", required = false, defaultValue = "1")
        current: Int,

        @Valid
        @Parameter(description = "The number of results in a single page",schema = Schema(defaultValue = "10"))
        @RequestParam(value = "size", required = false, defaultValue = "10")
        size: Int,

        @Valid
        @Parameter(description = "",schema = Schema(allowableValues = ["ASC", "DESC"], defaultValue = "DESC"))
        @RequestParam(value = "direction", required = false, defaultValue = "DESC")
        direction: String,

        @Valid
        @Parameter(description = "",schema = Schema(allowableValues = ["updateTime", "createTime"], defaultValue = "createTime"))
        @RequestParam(value = "sortBy", required = false, defaultValue = "createTime")
        sortBy: String,

//        @Valid
//        @Parameter(description = "시작 날짜")
//        @RequestParam(value = "startDate", required = false)
//        @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
//        startDate: java.time.LocalDate?,
//
//        @Valid
//        @Parameter(description = "종료 날짜")
//        @RequestParam(value = "endDate", required = false)
//        @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
//        endDate: java.time.LocalDate?
    ): ResponseEntity<MaterialListResponse>

    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/oms/v1/materials/{materialId}"],
        produces = ["application/json"]
    )
    @Throws(Throwable::class)
    fun getMaterialById(
        @Parameter(description = "서비스 인증 API KEY", `in` = ParameterIn.HEADER, required = true)
        @RequestHeader(value = "x-abc-sdk-apikey", required = true)
        xAbcSdkApikey: String,
        @Parameter(description = "자재 ID", required = true)
        @PathVariable("materialId")
        materialId: String): ResponseEntity<MaterialResponse>

    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/oms/v1/service-flows"],
        produces = ["application/json"]
    )
    @Throws(Throwable::class)
    fun getServiceFlowList(
        @Parameter(description = "서비스 인증 API KEY", `in` = ParameterIn.HEADER, required = true)
        @RequestHeader(value = "x-abc-sdk-apikey", required = true)
        xAbcSdkApikey: String,

        @Valid
        @SpringQueryMap
        @NotNull
        @Parameter(description = "서비스플로우 검색조건",required = true)
        serviceFlowSearch: GetServiceFlowListServiceFlowSearchParameter,

        @Valid
        @Min(1)
        @Parameter(description = "The number of results page",schema = Schema(defaultValue = "1"))
        @RequestParam(value = "current",required = false,defaultValue = "1")
        current: Int,

        @Valid
        @Parameter(description = "The number of results in a single page",schema = Schema(defaultValue = "10"))
        @RequestParam(value = "size",required = false,defaultValue = "10")
        size: Int,

        @Valid
        @Parameter(description = "",schema = Schema(allowableValues = ["updateTime", "createTime"],defaultValue = "createTime"))
        @RequestParam(value = "sortBy",required = false,defaultValue = "createTime")
        sortBy: String,

        @Valid
        @Parameter(description = "",schema = Schema(allowableValues = ["ASC", "DESC"], defaultValue = "DESC"))
        @RequestParam(value = "direction",required = false,defaultValue = "DESC")
        direction: String
    ): ResponseEntity<ServiceFlowPageListResponse>

    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/oms/v1/resource-histories"],
        produces = ["application/json"]
    )
    fun getResourceHistory(
        @Parameter(description = "서비스 인증 API KEY", `in` = ParameterIn.HEADER, required = true)
        @RequestHeader(value = "x-abc-sdk-apikey", required = true)
        xAbcSdkApikey: String,

        @Valid
        @NotNull
        @SpringQueryMap
        @Parameter(description = "리소스 변경이력 조회", required = true)
        resourceHistorySearch: GetResourceHistoryResourceHistorySearchParameter,

        @Min(1)
        @Parameter(description = "The number of results page", schema = Schema(defaultValue = "1"))
        @Valid
        @RequestParam(value = "current", required = false, defaultValue = "1")
        current: Int,

        @Valid
        @Parameter(description = "The number of results in a single page", schema = Schema(defaultValue = "10"))
        @RequestParam(value = "size", required = false, defaultValue = "10") size: Int,

        @Valid
        @Parameter(description = "", schema = Schema(allowableValues = ["updateTime", "createTime"], defaultValue = "createTime"))
        @RequestParam(value = "sortBy", required = false, defaultValue = "createTime") sortBy: String,

        @Valid
        @Parameter(description = "", schema = Schema(allowableValues = ["ASC", "DESC"], defaultValue = "DESC"))
        @RequestParam(value = "direction", required = false, defaultValue = "DESC")
        direction: String)
    : ResponseEntity<ResourceHistoryListResponse>

    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/oms/v1/billing/charges"],
        produces = ["application/json"]
    )
    fun getBillingCharges(
        @Parameter(description = "서비스 인증 API KEY", `in` = ParameterIn.HEADER, required = true)
        @RequestHeader(value = "x-abc-sdk-apikey", required = true)
        xAbcSdkApikey: String,

        @NotNull
        @Parameter(description = "청구항목 계약조건", required = true)
        @Valid
        @SpringQueryMap
        chargeSearch: GetBillingChargesChargeSearchParameter,

        @Min(1)@Parameter(description = "The number of results page", schema = Schema(defaultValue = "1"))
        @Valid
        @RequestParam(value = "current", required = false, defaultValue = "1")
        current: Int,

        @Parameter(description = "The number of results in a single page", schema = Schema(defaultValue = "10"))
        @Valid @RequestParam(value = "size", required = false, defaultValue = "10")
        size: Int,

        @Parameter(description = "", schema = Schema(allowableValues = ["updateTime", "createTime"], defaultValue = "createTime"))
        @Valid
        @RequestParam(value = "sortBy", required = false, defaultValue = "billingCycle")
        sortBy: String,

        @Parameter(description = "", schema = Schema(allowableValues = ["ASC", "DESC"], defaultValue = "DESC"))
        @Valid
        @RequestParam(value = "direction", required = false, defaultValue = "DESC")
        direction: String): ResponseEntity<OmsChargesResponse>
}