package com.abc.us.accounting.collects.works.materials

import com.abc.us.accounting.collects.collectable.Collectable
import com.abc.us.accounting.supports.client.OmsClient
import com.abc.us.generated.models.Material
import com.abc.us.generated.models.MaterialResponse
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import java.time.LocalDate

open class MaterialCollectable(
    private val xAbcSdkApikey: String,
    private val sortProperty: String,
    private val pageSize: Int,
    private val omsClient : OmsClient
) : Collectable(sortProperty, pageSize){

    companion object {
        private val logger = KotlinLogging.logger {}
    }
    @Throws(Throwable::class)
    fun collects(startDate : LocalDate,
                 endDate: LocalDate,
                 results: (List<Material>) -> Boolean) {

        super.visit(
            // 요청 로직 정의
            { pageNumber, pageSize, sortDirection ->
                omsClient.getMaterials(
                    xAbcSdkApikey = xAbcSdkApikey,
                    current = pageNumber,
                    size = pageSize,
                    direction = sortDirection,
                    sortBy = sortProperty
                    //startDate = startDate,
                    //endDate = endDate
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
    fun getMaterialByIdWithApiKey(materialId: String) : ResponseEntity<MaterialResponse> {
        return omsClient.getMaterialById(xAbcSdkApikey,materialId)
    }
}