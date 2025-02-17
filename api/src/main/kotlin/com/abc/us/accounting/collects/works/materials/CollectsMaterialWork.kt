package com.abc.us.accounting.collects.works.materials

//import com.abc.us.accounting.supports.entity.SaveDistinct
import com.abc.us.accounting.collects.domain.entity.collect.CollectMaterial
import com.abc.us.accounting.collects.domain.repository.CollectMaterialRepository
import com.abc.us.accounting.collects.domain.type.*
import com.abc.us.accounting.commons.domain.type.TimeZoneCode
import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.client.OmsClient
import com.abc.us.accounting.supports.entity.BulkDistinctInserter
import com.abc.us.generated.models.MaterialAttributeType
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Lazy
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.LocalDateTime
import com.abc.us.generated.models.Material as OmsMaterial

@Service
class CollectsMaterialWork (
    @Value("\${abc-sdk.api-key}")
    private val xAbcSdkApikey: String,
    @Value("\${collects.read.page.sort-by:createTime}")
    private val sortProperty: String,
    @Value("\${collects.read.page.max-size:100}")
    private val pageSize: Int,
    @Lazy
    private val omsClient : OmsClient,
    private val materialRepository : CollectMaterialRepository,
    private val eventPublisher : ApplicationEventPublisher,
    private val bulkInserter : BulkDistinctInserter
) : MaterialCollectable(xAbcSdkApikey, sortProperty, pageSize, omsClient){
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private fun getMaterialById(materialId : String) : OmsMaterial? {
        val response = getMaterialByIdWithApiKey(materialId)
        if (response.statusCode == HttpStatus.OK) {
            return response.body!!.data
        } else {
            throw ResponseStatusException(response.statusCode, response.body?.code)
        }

        return null
    }

    fun generateEntity(targetId : String?) : CollectMaterial? {
        return getMaterialById(targetId!!)?.let {
            material ->
                val featureCode = material.materialAttributes?.firstOrNull{attribute -> attribute.attributeType == MaterialAttributeType.KEY_FEATURE}?.attributeCode?.value
                val filterType = material.materialAttributes?.firstOrNull{attribute -> attribute.attributeType == MaterialAttributeType.FILTER_TYPE}?.attributeCode?.value
                val installationType = material.materialAttributes?.firstOrNull{attribute -> attribute.attributeType == MaterialAttributeType.INSTALLATION_TYPE}?.attributeCode?.value
                CollectMaterial(
                    materialId = material.materialId!!,
                    materialName = material.materialName,
                    materialBrandName = material.materialBrandName.name,
                    materialType = MaterialType.valueOf(material.materialType.value),
                    materialCategoryCode = MaterialCategoryCode.valueOf(material.materialCategory.categoryCode.value),
                    productType = ProductType.valueOf(material.materialProductType.value),
                    featureCode = if ( featureCode != null) FeatureCode.valueOf(featureCode) else null,
                    filterType = if ( filterType != null) FilterType.valueOf(filterType) else null,
                    installationType = if ( installationType != null) InstallationType.valueOf(installationType) else null
                )
                    .apply {
                        materialSeriesCode = material.materialSeries?.let { it.seriesCode }
                        materialModelName = material.materialModelName
                        createTime = material.materialCreateTime
                        updateTime = material.materialUpdateTime
                        description = material.description
                    }

        }
    }
    fun collectMaterials(startDate : LocalDate,
                         endDate: LocalDate) : MutableList<CollectMaterial>{
        var collectedCollectMaterials = mutableListOf<CollectMaterial>()
        collects(startDate,endDate){ materials ->
            materials.forEach { material ->
                val newMateiral = generateEntity(material.materialId)
                newMateiral?.let { collectedCollectMaterials.add(it)}
            }
            true
        }
        return collectedCollectMaterials
    }

    @Transactional
    fun bulkInsert(collectedMaterials : MutableList<CollectMaterial>) {
        bulkInserter.execute(materialRepository,collectedMaterials)
    }

    fun collect(trailer: AsyncEventTrailer){
        val from = trailer.queries().get("fromDateTime") as LocalDateTime
        val to = trailer.queries().get("toDateTime") as LocalDateTime
        val timezone = trailer.queries().get("timezone") as TimeZoneCode

        logger.info { "COLLECT-START[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }
        var collectedMaterials = collectMaterials(from.toLocalDate(),to.toLocalDate())
        bulkInsert(collectedMaterials)
        logger.info { "COLLECT-END[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }
    }
}