package com.abc.us.accounting.collects.collectable

import com.abc.us.accounting.supports.client.OmsClient
import com.abc.us.generated.models.GetResourceHistoryResourceHistorySearchParameter
import com.abc.us.generated.models.ResourceHistory

open class ResourceHistoryCollectable(
    private val xAbcSdkApikey: String,
    private val sortProperty: String,
    private val pageSize: Int,
    private val omsClient : OmsClient
) : Collectable(sortProperty, pageSize){

    @Throws(Throwable::class)
    fun collects(parameter : GetResourceHistoryResourceHistorySearchParameter,
                 result: (List<ResourceHistory>) -> Boolean) {
        super.visit(
            // 요청 로직 정의
            { pageNumber, pageSize, sortDirection ->
                omsClient.getResourceHistory(
                    xAbcSdkApikey = xAbcSdkApikey,
                    current = pageNumber,
                    size = pageSize,
                    direction = sortDirection,
                    sortBy = sortProperty,
                    resourceHistorySearch = parameter
                )
            },
            // 응답 처리 로직 정의
            { response ->
                response?.let { body ->
                    val page = body.data?.let {
                        //results(it.items ?: emptyList())
                        val omsHistories = it.items ?: emptyList()
                        result(omsHistories)
                        //omsHistories.forEach {result(it)}
                        val page = body.data!!.page
                        Paging(page!!.current, page.total, page.propertySize, page.totalItems!!)
                    }
                    page
                } ?: throw IllegalStateException("Response body is null")
            }
        )
    }
}