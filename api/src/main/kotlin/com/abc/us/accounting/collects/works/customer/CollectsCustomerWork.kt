package com.abc.us.accounting.collects.works.customer

import com.abc.us.accounting.collects.collectable.ResourceHistoryCollectable
import com.abc.us.accounting.collects.domain.entity.collect.CollectCustomer
import com.abc.us.accounting.collects.domain.repository.CollectCustomerRepository
import com.abc.us.accounting.collects.helper.OmsEntityCustomerMutableList
import com.abc.us.accounting.collects.helper.builder.CustomerBuilder
import com.abc.us.accounting.commons.domain.type.TimeZoneCode
import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.client.OmsClient
import com.abc.us.accounting.supports.converter.JsonConverter
import com.abc.us.accounting.supports.entity.BulkDistinctInserter
import com.abc.us.generated.models.GetResourceHistoryResourceHistorySearchParameter
import com.abc.us.generated.models.ResourceHistoryOperation
import com.abc.us.generated.models.ResourceName
import com.abc.us.oms.domain.customer.entity.Customer
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.OffsetDateTime

@Service
class CollectsCustomerWork(
    @Value("\${abc-sdk.api-key}")
    private val xAbcSdkApikey: String,
    @Value("\${collects.read.page.sort-by:createTime}")
    private val sortProperty: String,
    @Value("\${collects.read.page.max-size:100}")
    private val pageSize: Int,
    @Lazy
    private val omsClient : OmsClient,
    private val customerRepository : CollectCustomerRepository,
    private val bulkInserter : BulkDistinctInserter,
    private val eventPublisher : ApplicationEventPublisher,
) : ResourceHistoryCollectable(xAbcSdkApikey, sortProperty, pageSize, omsClient){

    companion object {
        private val logger = KotlinLogging.logger {}
        private val converter = JsonConverter()
    }
    fun collectCustomers(start : OffsetDateTime,
                         end : OffsetDateTime,
                         op : ResourceHistoryOperation,
                         responses: (OmsEntityCustomerMutableList) -> Boolean) {
        val searchParameter = GetResourceHistoryResourceHistorySearchParameter(
            resourceName = ResourceName.CUSTOMER,
            operation = op,
            startDate = start,
            endDate = end
        )

        collects(searchParameter) { histories ->
            var collects = mutableListOf<Customer>()
            histories.forEach { history ->
                history.newValue?.let { valueMap ->
                    converter.toObj(valueMap, Customer::class.java)?.let {
                        collects.add(it)
                    }
                }
            }
            responses(OmsEntityCustomerMutableList(collects))
            true
        }
    }
    data class Builder(
        var customers : MutableList<CollectCustomer> = mutableListOf()
    ) {
        fun execute(origin : OmsEntityCustomerMutableList) : Builder {
            customers = CustomerBuilder.build(origin)
            return this
        }

    }
    @Transactional
    fun bulkInsert(results : Builder) {

        bulkInserter.execute(customerRepository,results.customers)
        logger.info {
            "COLLECT-CUSTOMER-BULK_INSERT[customers=${results.customers.size}]"
        }
    }
    @Transactional
    fun bulkDelete(results : Builder) {

        //SaveDistinct(customerRepository).execute(makeCustomer)
        logger.info {
            "COLLECT-CUSTOMER-BULK_DELETE[customers=${results.customers.size}]"
        }
    }

    @Transactional
    fun collects(trailer: AsyncEventTrailer) {

        val from = trailer.queries().get("fromDateTime") as LocalDateTime
        val to = trailer.queries().get("toDateTime") as LocalDateTime
        val timezone = trailer.queries().get("timezone") as TimeZoneCode


        logger.info { "COLLECT-START[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }
        collectCustomers(timezone.convertTime(from,TimeZoneCode.UTC),
            timezone.convertTime(to,TimeZoneCode.UTC),
            ResourceHistoryOperation.INSERT ) {
            bulkInsert( Builder().execute(it))
            true
        }
        collectCustomers(timezone.convertTime(from,TimeZoneCode.UTC),
            timezone.convertTime(to,TimeZoneCode.UTC),
            ResourceHistoryOperation.UPDATE ){
            bulkInsert( Builder().execute(it))
            true
        }
        collectCustomers(timezone.convertTime(from,TimeZoneCode.UTC),
            timezone.convertTime(to,TimeZoneCode.UTC),
            ResourceHistoryOperation.DELETE ){
            bulkDelete( Builder().execute(it))
            true
        }
        logger.info { "COLLECT-END[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }
    }
}