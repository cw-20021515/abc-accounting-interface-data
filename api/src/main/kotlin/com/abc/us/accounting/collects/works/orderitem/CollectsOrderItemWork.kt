package com.abc.us.accounting.collects.works.orderitem

import com.abc.us.accounting.collects.collectable.ResourceHistoryCollectable
import com.abc.us.accounting.collects.domain.entity.collect.*
import com.abc.us.accounting.collects.domain.repository.*
import com.abc.us.accounting.collects.helper.OmsEntityOrderItemMutableList
import com.abc.us.accounting.collects.helper.builder.OrderItemBuilder
import com.abc.us.accounting.commons.domain.type.TimeZoneCode
import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.client.OmsClient
import com.abc.us.accounting.supports.converter.JsonConverter
import com.abc.us.accounting.supports.entity.BulkDistinctInserter
import com.abc.us.generated.models.GetResourceHistoryResourceHistorySearchParameter
import com.abc.us.generated.models.ResourceHistoryOperation
import com.abc.us.generated.models.ResourceName
import com.abc.us.oms.domain.order.entity.OrderItem
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.OffsetDateTime

@Service
class CollectsOrderItemWork(
    @Value("\${abc-sdk.api-key}")
    private val xAbcSdkApikey: String,
    @Value("\${collects.read.page.sort-by:createTime}")
    private val sortProperty: String,
    @Value("\${collects.read.page.max-size:100}")
    private val pageSize: Int,
    @Lazy
    private val omsClient : OmsClient,
    private val orderRepository : CollectOrderRepository,
    private val orderItemRepository : CollectOrderItemRepository,
    private val contractRepository : CollectContractRepository,
    private val installationRepository: CollectInstallationRepository,
    private val taxLineRepository : CollectTaxLineRepository,
    private val receiptRepository : CollectReceiptRepository,
    private val channelRepository : CollectChannelRepository,
    private val customerRepository : CollectCustomerRepository,
    private val shippingRepository : CollectShippingRepository,
    private val materialRepository : CollectMaterialRepository,
    private val promotionRepository : CollectPromotionRepository,
    private val bulkInserter : BulkDistinctInserter
)   : ResourceHistoryCollectable(xAbcSdkApikey, sortProperty, pageSize, omsClient){

    companion object {
        private val logger = KotlinLogging.logger {}
        private val converter = JsonConverter()
    }
    fun collectOrderItems(start : OffsetDateTime,
                          end : OffsetDateTime,
                          op : ResourceHistoryOperation ,
                          responses: (MutableList<OrderItem>) -> Boolean) {
        val searchParameter = GetResourceHistoryResourceHistorySearchParameter(
            resourceName = ResourceName.ORDER_ITEM,
            operation = op,
            startDate = start,
            endDate = end
        )
        collects(searchParameter){ histories ->
            var collects = mutableListOf<OrderItem>()
            histories.forEach { history ->
                history.newValue?.let { valueMap ->
                    converter.toObj(valueMap, OrderItem::class.java)?.let {
                        collects.add(it)
                    }
                }
            }
            responses(collects)
            true
        }
    }
    data class Builder(
        var orders : MutableList<CollectOrder> = mutableListOf(),
        var orderItems : MutableList<CollectOrderItem> = mutableListOf(),
        var customerIds : MutableSet<String> = mutableSetOf(),
        var customers : MutableList<CollectCustomer> = mutableListOf(),
        var materials : MutableList<CollectMaterial> = mutableListOf(),
        var shippings : MutableList<CollectShipping> = mutableListOf(),
        var installations : MutableList<CollectInstallation> = mutableListOf(),
        var contracts : MutableList<CollectContract> = mutableListOf(),
        var taxLines : MutableList<CollectTaxLine> = mutableListOf(),
        var promotions : MutableList<CollectPromotion> = mutableListOf(),
        var receipts : MutableList<CollectReceipt> = mutableListOf(),
        var channels : MutableList<CollectChannel> = mutableListOf()
    ) {
        private fun separateOrders(orderItems : MutableList<CollectOrderItem>) : MutableList<CollectOrder> {
            val separated = mutableListOf<CollectOrder>()
            orderItems.forEach { item ->
                item.order?.let { separated.add(it) }
            }
            return separated
        }
        private fun separateMaterials(orderItems : MutableList<CollectOrderItem>) : MutableList<CollectMaterial> {
            val separated = mutableListOf<CollectMaterial>()
            orderItems.forEach { item ->
                item.material?.let { separated.add(it) }
            }
            return separated
        }

        private fun separateContracts(orderItems : MutableList<CollectOrderItem>) : MutableList<CollectContract> {
            val separated = mutableListOf<CollectContract>()
            orderItems.forEach { item ->
                separated.addAll(item.contracts)
            }
            return separated
        }


        private fun separateInstallation(orderItems : MutableList<CollectOrderItem>) : MutableList<CollectInstallation> {
            val separated = mutableListOf<CollectInstallation>()
            orderItems.forEach { item ->
                item.installation?.let {separated.add(it)}
            }
            return separated
        }
        private fun separateShipping(orderItems : MutableList<CollectOrderItem>) : MutableList<CollectShipping> {
            val separated = mutableListOf<CollectShipping>()
            orderItems.forEach { item ->
                item.shipping?.let { separated.add(it) }
            }
            return separated
        }

        private fun separateTaxLine(orderItems : MutableList<CollectOrderItem>) : MutableList<CollectTaxLine> {
            val separated = mutableListOf<CollectTaxLine>()
            orderItems.forEach { item ->
                separated.addAll(item.taxLines)
            }
            return separated
        }

        private fun separatePromotion(orderItems : MutableList<CollectOrderItem>) : MutableList<CollectPromotion> {
            val separated = mutableListOf<CollectPromotion>()
            orderItems.forEach { item ->
                separated.addAll(item.promotions)
            }
            return separated
        }

        private fun separateReceipt(orders : MutableList<CollectOrder>) : MutableList<CollectReceipt> {
            val separated = mutableListOf<CollectReceipt>()
            orders.forEach { order -> order.receipt?.let { separated.add(it) }}
            return separated
        }
        private fun separateChannel(orders : MutableList<CollectOrder>) : MutableList<CollectChannel> {
            val separated = mutableListOf<CollectChannel>()
            orders.forEach { order ->
                order.channel?.let { separated.add(it) }
            }
            return separated
        }
        private fun separateCustomersFromContract(contracts : MutableList<CollectContract>) : MutableList<CollectCustomer> {
            val separated = mutableListOf<CollectCustomer>()
            contracts.forEach { contract ->
                contract.customer?.let { customer ->
                    separated.add(customer)
                }
            }
            return separated
        }
        private fun separateCustomersFromOrderItem(orderItems : MutableList<CollectOrderItem>) : MutableList<CollectCustomer> {
            val separated = mutableListOf<CollectCustomer>()
            orderItems.forEach { item ->
                item.customers.forEach { customer ->
                    separated.add(customer)
                }
            }
            return separated
        }
        private fun separateCustomerId(orders : MutableList<CollectOrder>) : MutableSet<String>{
            val separated = mutableSetOf<String>()
            orders.forEach { order ->
                separated.add(order.customerId)
            }
            return separated
        }
        private fun separateCustomersFromOrder(orders : MutableList<CollectOrder>) : MutableList<CollectCustomer> {
            val separated = mutableListOf<CollectCustomer>()
            orders.forEach { order ->
                order.customer?.let { customer ->
                    separated.add(customer)
                }
            }
            return separated
        }
        fun execute(origin : OmsEntityOrderItemMutableList) : Builder{
            orderItems = OrderItemBuilder.builds( origin )
            orders = separateOrders(orderItems)
            contracts = separateContracts(orderItems)
            materials = separateMaterials(orderItems)
            customers = separateCustomersFromOrderItem(orderItems)
            customers.addAll(separateCustomersFromContract(contracts))
            customers.addAll(separateCustomersFromOrder(orders))

            customerIds.addAll(separateCustomerId(orders))

            shippings = separateShipping(orderItems)
            installations = separateInstallation(orderItems)
            taxLines = separateTaxLine(orderItems)
            receipts = separateReceipt(orders)
            channels = separateChannel(orders)
            promotions = separatePromotion(orderItems)
            return this
        }
    }
    @Transactional
    fun bulkInsert(op : ResourceHistoryOperation , results :Builder) {
        /*
        val saveCharge = separateCharge(saveOrderItems)
        SaveDistinct(chargeRepository).execute(saveCharge)
         */


        bulkInserter.execute(orderRepository,results.orders)
        bulkInserter.execute(contractRepository,results.contracts)
        bulkInserter.execute(customerRepository,results.customers)
        bulkInserter.execute(shippingRepository,results.shippings)
        bulkInserter.execute(materialRepository,results.materials)
        bulkInserter.execute(installationRepository,results.installations)
        bulkInserter.execute(taxLineRepository,results.taxLines)
        bulkInserter.execute(receiptRepository,results.receipts)
        bulkInserter.execute(channelRepository,results.channels)
        bulkInserter.execute(promotionRepository,results.promotions)
        bulkInserter.execute(orderItemRepository,results.orderItems)


        logger.info {
            "COLLECT-ORDER-ITEM-BULK_INSERT[${op.name}].[" +
                    "orders=${results.orders.size}, " +
                    "contracts=${results.contracts.size}, " +
                    "installations=${results.installations.size}," +
                    "taxLines=${results.taxLines.size}," +
                    "receipts=${results.receipts.size}," +
                    "channels=${results.channels.size}," +
                    "contracts=${results.contracts.size}," +
                    "customerIds=${results.customerIds.size}," +
                    "customers=${results.customers.size}," +
                    "shippings=${results.shippings.size}," +
                    "promotions=${results.promotions.size}," +
                    "materials=${results.materials.size}," +
                    "orderItems=${results.orderItems.size}" +
                    "]"
        }
    }

    fun bulkDelete(op : ResourceHistoryOperation,
                   results :Builder) {

        //val makeOrderItems = OrderItemBuilder.builds(orderItems)
        //SaveDistinct(orderItemRepository).execute(makeOrderItems)
    }


    fun collects(trailer: AsyncEventTrailer) {
        val from = trailer.queries().get("fromDateTime") as LocalDateTime
        val to = trailer.queries().get("toDateTime") as LocalDateTime
        val timezone = trailer.queries().get("timezone") as TimeZoneCode

        logger.info { "COLLECT-START[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }

        collectOrderItems(timezone.convertTime(from,TimeZoneCode.UTC),
            timezone.convertTime(to,TimeZoneCode.UTC),
            ResourceHistoryOperation.INSERT ){orderItems ->
            bulkInsert(ResourceHistoryOperation.INSERT,
                Builder().execute( OmsEntityOrderItemMutableList(orderItems)))
            true
        }
        collectOrderItems(timezone.convertTime(from,TimeZoneCode.UTC),
            timezone.convertTime(to,TimeZoneCode.UTC),
            ResourceHistoryOperation.UPDATE ){ orderItems ->
            bulkInsert(ResourceHistoryOperation.UPDATE,
                Builder().execute( OmsEntityOrderItemMutableList(orderItems)))
            true
        }
        collectOrderItems(timezone.convertTime(from,TimeZoneCode.UTC),
            timezone.convertTime(to,TimeZoneCode.UTC),
            ResourceHistoryOperation.DELETE ) {orderItems ->
            bulkDelete(ResourceHistoryOperation.UPDATE,
                Builder().execute( OmsEntityOrderItemMutableList(orderItems)))
            true
        }
        logger.info { "COLLECT-END[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }
    }
}