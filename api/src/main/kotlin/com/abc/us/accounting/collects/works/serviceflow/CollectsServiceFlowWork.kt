package com.abc.us.accounting.collects.works.serviceflow

import com.abc.us.accounting.collects.collectable.ResourceHistoryCollectable
import com.abc.us.accounting.collects.domain.entity.collect.CollectLocation
import com.abc.us.accounting.collects.domain.entity.collect.CollectReceipt
import com.abc.us.accounting.collects.domain.entity.collect.CollectServiceFlow
import com.abc.us.accounting.collects.domain.entity.collect.CollectTaxLine
import com.abc.us.accounting.collects.domain.repository.CollectLocationRepository
import com.abc.us.accounting.collects.domain.repository.CollectReceiptRepository
import com.abc.us.accounting.collects.domain.repository.CollectServiceFlowRepository
import com.abc.us.accounting.collects.domain.repository.CollectTaxLineRepository
import com.abc.us.accounting.collects.helper.builder.ServiceFlowBuilder
import com.abc.us.accounting.commons.domain.type.TimeZoneCode
import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.client.OmsClient
import com.abc.us.accounting.supports.converter.JsonConverter
import com.abc.us.accounting.supports.entity.BulkDistinctInserter
import com.abc.us.generated.models.GetResourceHistoryResourceHistorySearchParameter
import com.abc.us.generated.models.ResourceHistoryOperation
import com.abc.us.generated.models.ResourceName
import com.abc.us.oms.domain.serviceflow.entity.ServiceFlow
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.OffsetDateTime

@Service
class CollectsServiceFlowWork(
    @Value("\${abc-sdk.api-key}")
    private val xAbcSdkApikey: String,
    @Value("\${collects.read.page.sort-by:createTime}")
    private val sortProperty: String,
    @Value("\${collects.read.page.max-size:100}")
    private val pageSize: Int,
    @Lazy
    private val omsClient : OmsClient,
    private val serviceFlowRepository : CollectServiceFlowRepository,
    private val locationRepository: CollectLocationRepository,
    private val receiptRepository : CollectReceiptRepository,
    private val taxLineRepository: CollectTaxLineRepository,
    private val bulkInserter : BulkDistinctInserter
) : ResourceHistoryCollectable(xAbcSdkApikey, sortProperty, pageSize, omsClient){
    companion object {
        private val logger = KotlinLogging.logger {}
        private val converter = JsonConverter()
    }
    fun collectServiceFlow(start : OffsetDateTime,
                           end : OffsetDateTime,
                           op : ResourceHistoryOperation ,
                           responses: (MutableList<ServiceFlow>) -> Boolean) {
        val searchParameter = GetResourceHistoryResourceHistorySearchParameter(
            resourceName = ResourceName.SERVICE_FLOW,
            operation = op,
            startDate = start,
            endDate = end
        )

        collects(searchParameter) { histories ->
            var collects = mutableListOf<ServiceFlow>()
            histories.forEach { history ->
                history.newValue?.let { valueMap ->
                    converter.toObj(valueMap, ServiceFlow::class.java)?.let {
                        collects.add(it)
                    }
                }
            }
            responses(collects)
            true
        }
    }

    data class Builder(
        var serviceFlows : MutableList<CollectServiceFlow> = mutableListOf(),
        var locations : MutableList<CollectLocation> = mutableListOf(),
        var receipts : MutableList<CollectReceipt> = mutableListOf(),
        var taxLines : MutableList<CollectTaxLine> = mutableListOf()

    ) {
        fun separateReceipt(serviceFlows : MutableList<CollectServiceFlow>) : MutableList<CollectReceipt> {
            val collects = mutableListOf<CollectReceipt>()
            serviceFlows.forEach { flow ->
                collects.addAll(flow.receipts)
            }
            return collects
        }
        fun separateTaxLine(receipts : MutableList<CollectReceipt>) : MutableList<CollectTaxLine> {
            val collects = mutableListOf<CollectTaxLine>()
            receipts.forEach { receipt ->
                collects.addAll(receipt.taxLines)
            }
            return collects
        }
        fun separateLocation(serviceFlows : MutableList<CollectServiceFlow>) : MutableList<CollectLocation> {
            val collects = mutableListOf<CollectLocation>()
            serviceFlows.forEach { flow ->
                flow.location?.let {  collects.add(it)}
            }
            return collects
        }
        fun execute(origin : MutableList<ServiceFlow>) : Builder  {
            serviceFlows = ServiceFlowBuilder.build(origin)
            locations = separateLocation(serviceFlows)
            receipts = separateReceipt(serviceFlows)
            taxLines = separateTaxLine(receipts)
            return this
        }
    }
    @Transactional
    fun bulkInsert(results : Builder) {
        bulkInserter.execute(locationRepository,results.locations)
        bulkInserter.execute(receiptRepository,results.receipts)
        bulkInserter.execute(taxLineRepository,results.taxLines)
        bulkInserter.execute(serviceFlowRepository,results.serviceFlows)

//        SaveDistinct(locationRepository).execute(saveLocation)
//        SaveDistinct(receiptRepository).execute(saveReceipt)
//        SaveDistinct(taxLineRepository).execute(taxLines)
//        SaveDistinct(serviceFlowRepository).execute(makeServiceFlows)
        logger.info {
            "COLLECT-SERVICEFLOW-BULK_INSERT[locations=${results.locations.size}, " +
                "receipts=${results.receipts.size}, " +
                "taxLines=${results.taxLines.size}," +
                "receipts=${results.receipts.size}," +
                "serviceFlows=${results.serviceFlows.size}" }
    }
    @Transactional
    fun bulkDelete(results : Builder) {

        //TODO : hschoi --> DELETE 어떤 방식으로 처리해야 할지 고민 필요
        //SaveDistinct(serviceFlowRepository).execute(makeServiceFlows)
    }

    fun collect(trailer: AsyncEventTrailer) {
        val from = trailer.queries().get("fromDateTime") as LocalDateTime
        val to = trailer.queries().get("toDateTime") as LocalDateTime
        val timezone = trailer.queries().get("timezone") as TimeZoneCode

        logger.info { "COLLECT-START[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }
        collectServiceFlow(timezone.convertTime(from,TimeZoneCode.UTC),
            timezone.convertTime(to,TimeZoneCode.UTC),
            ResourceHistoryOperation.INSERT) {
            bulkInsert(Builder().execute(it))
            true
        }
        collectServiceFlow(timezone.convertTime(from,TimeZoneCode.UTC),
            timezone.convertTime(to,TimeZoneCode.UTC),
            ResourceHistoryOperation.UPDATE) {
            bulkInsert(Builder().execute(it))
            true
        }
        collectServiceFlow(timezone.convertTime(from,TimeZoneCode.UTC),
            timezone.convertTime(to,TimeZoneCode.UTC),
            ResourceHistoryOperation.DELETE) {
            bulkDelete(Builder().execute(it))
            true
        }
        logger.info { "COLLECT-END[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }
    }
}