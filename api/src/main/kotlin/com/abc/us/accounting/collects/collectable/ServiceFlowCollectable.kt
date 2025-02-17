//package com.abc.us.accounting.collects.collectable
//
//import com.abc.us.accounting.supports.client.OmsClient
//import com.abc.us.accounting.supports.converter.ISO8856ToLocalDate
//import com.abc.us.generated.models.GetServiceFlowListServiceFlowSearchParameter
//import com.abc.us.generated.models.ServiceFlow
//import com.abc.us.generated.models.ServiceFlowType
//
//open class ServiceFlowCollectable(
//    private val xAbcSdkApikey: String,
//    private val sortProperty: String,
//    private val pageSize: Int,
//    private val omsClient : OmsClient
//) : Collectable(sortProperty, pageSize){
//
//    @Throws(Throwable::class)
//    fun collects(from : String,
//                 to : String,
//                 type : ServiceFlowType,
//                 results: (List<ServiceFlow>) -> Boolean) {
//
//        super.visit(
//            // 요청 로직 정의
//            { pageNumber, pageSize, sortDirection ->
//                omsClient.getServiceFlowList(
//                    xAbcSdkApikey = xAbcSdkApikey,
//                    current = pageNumber,
//                    size = pageSize,
//                    direction = sortDirection,
//                    sortBy = sortProperty,
//                    serviceFlowSearch = GetServiceFlowListServiceFlowSearchParameter(
//                        startDate = ISO8856ToLocalDate.convert(from),
//                        endDate = ISO8856ToLocalDate.convert(to),
//                        serviceFlowType = type
//                    )
//                )
//            },
//            // 응답 처리 로직 정의
//            { response ->
//                response?.let { body ->
//                    val page = body.data?.let {
//                        results(it.items ?: emptyList())
//                        val page = body.data!!.page
//                        Paging(page!!.current, page.total, page.propertySize, page.totalItems!!)
//                    }
//                    page
//                } ?: throw IllegalStateException("Response body is null")
//            }
//        )
//    }
//}