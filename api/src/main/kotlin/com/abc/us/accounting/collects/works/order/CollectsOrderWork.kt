package com.abc.us.accounting.collects.works.order

import com.abc.us.accounting.collects.collectable.OrderCollectable
import com.abc.us.accounting.collects.domain.entity.collect.*
import com.abc.us.accounting.collects.domain.repository.CollectCustomerRepository
import com.abc.us.accounting.collects.domain.repository.CollectOrderRepository
import com.abc.us.accounting.collects.domain.repository.CollectReceiptRepository
import com.abc.us.accounting.collects.domain.repository.CollectTaxLineRepository
import com.abc.us.accounting.collects.helper.OmsApiOrderMutableList
import com.abc.us.accounting.collects.helper.builder.OrderBuilder
import com.abc.us.accounting.collects.helper.builder.TaxLineBuilder
import com.abc.us.accounting.commons.domain.type.TimeZoneCode
import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.client.OmsClient
import com.abc.us.accounting.supports.converter.JsonConverter
import com.abc.us.accounting.supports.entity.BulkDistinctInserter
import com.abc.us.generated.models.GetOrdersOrderSearchParameter
import com.abc.us.generated.models.OrderItemStatus
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.OffsetDateTime

@Service
class CollectsOrderWork(
    @Value("\${abc-sdk.api-key}")
    private val xAbcSdkApikey: String,
    @Value("\${collects.read.page.sort-by:createTime}")
    private val sortProperty: String,
    @Value("\${collects.read.page.max-size:100}")
    private val pageSize: Int,
    @Lazy
    private val omsClient : OmsClient,
    private val orderRepository : CollectOrderRepository,
    private val taxLineRepository: CollectTaxLineRepository,
    private val receiptRepository: CollectReceiptRepository,
    private val customerRepository: CollectCustomerRepository,
    private val bulkInserter : BulkDistinctInserter

) : OrderCollectable(xAbcSdkApikey, sortProperty, pageSize, omsClient){

    companion object {
        private val logger = KotlinLogging.logger {}
        private val converter = JsonConverter()
    }
    data class Builder(
        var orders : MutableList<CollectOrder> = mutableListOf(),
        var orderItems : MutableList<CollectOrderItem> = mutableListOf(),
        var customers : MutableList<CollectCustomer> = mutableListOf(),
        var materials : MutableList<CollectMaterial> = mutableListOf(),
        var receipts : MutableList<CollectReceipt> = mutableListOf(),
        var taxLines :MutableList<CollectTaxLine> = mutableListOf()
    ) {
//        fun separateOrderItem(orders : MutableList<CollectOrder>) : MutableList<CollectOrderItem> {
//            val collects = mutableListOf<CollectOrderItem>()
//            orders.forEach { order ->
//                collects.addAll(order.orderItems)
//            }
//            return collects
//        }
//        fun separateMaterial(orders : MutableList<CollectOrder>) : MutableList<CollectMaterial> {
//            val collects = mutableListOf<CollectMaterial>()
//            orders.forEach { order ->
//                order.material?.let { collects.add(it) }
//            }
//            return collects
//        }
        fun separateCustomer(orders : MutableList<CollectOrder>) : MutableList<CollectCustomer> {
            val collects = mutableListOf<CollectCustomer>()
            orders.forEach { order ->
                order.customer?.let { collects.add(it) }
            }
            return collects
        }
        fun separateReceipt( orders : MutableList<CollectOrder>) : MutableList<CollectReceipt> {
            val collects = mutableListOf<CollectReceipt>()
            orders.forEach { order ->
                order.receipt?.let { collects.add(it) }
            }
            return collects
        }
        fun execute(origin : OmsApiOrderMutableList) : Builder{
            orders = OrderBuilder.build(origin)
            //orderItems = separateOrderItem(orders)
            customers = separateCustomer(orders)
            receipts = separateReceipt(orders)
            //materials = separateMaterial(orders)
            taxLines = TaxLineBuilder.build(origin)
            return this
        }
    }

    fun collectOrders(start : OffsetDateTime,
                      end : OffsetDateTime,
                      results : (OmsApiOrderMutableList)->Boolean)  {
        val searchParameter = GetOrdersOrderSearchParameter(
            startDate = start,
            endDate = end,
            orderItemStatuses = mutableListOf(
                //OrderItemStatus.ORDER_RECEIVED,
                //OrderItemStatus.ORDER_CONFIRMED,
                OrderItemStatus.ORDER_COMPLETED,
                OrderItemStatus.INSTALL_COMPLETED,
                OrderItemStatus.CANCELLATION_COMPLETED,
                OrderItemStatus.REFUND_COMPLETED,
                OrderItemStatus.RETURN_COMPLETED,
                OrderItemStatus.MATERIAL_RETURNED,
            )
        )
        collects(searchParameter){ omsOrders ->
            results( OmsApiOrderMutableList(omsOrders.toMutableList()) )
            true
        }
    }
    @Transactional
    fun bulkInsert(results : Builder) {
        bulkInserter.execute(orderRepository,results.orders)
        bulkInserter.execute(taxLineRepository,results.taxLines)
        bulkInserter.execute(receiptRepository,results.receipts)
        bulkInserter.execute(customerRepository,results.customers)
        logger.info {
            "COLLECT-ORDER-BULK_INSERT[orders=${results.orders.size}, " +
                    "receipts=${results.taxLines.size}, " +
                    "customers=${results.customers.size}, " +
                    "taxLines=${results.receipts.size}]"
        }
    }
    fun collects(trailer: AsyncEventTrailer) {

        val from = trailer.queries().get("fromDateTime") as LocalDateTime
        val to = trailer.queries().get("toDateTime") as LocalDateTime
        val timezone = trailer.queries().get("timezone") as TimeZoneCode


        logger.info { "COLLECT-START[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }
        collectOrders(timezone.convertTime(from,TimeZoneCode.UTC),
                timezone.convertTime(to,TimeZoneCode.UTC)){ orders ->
            bulkInsert(Builder().execute(orders))
            true
        }
        logger.info { "COLLECT-END[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }
    }
}