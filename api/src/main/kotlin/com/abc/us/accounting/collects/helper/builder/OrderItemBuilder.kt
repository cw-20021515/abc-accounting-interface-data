package com.abc.us.accounting.collects.helper.builder

import com.abc.us.accounting.collects.domain.entity.collect.*
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableLocation
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableName
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddablePrice
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableRelation
import com.abc.us.accounting.collects.domain.type.*
import com.abc.us.accounting.collects.helper.*
import com.abc.us.accounting.rentals.master.domain.type.OrderItemStatus
import com.abc.us.accounting.rentals.master.domain.type.OrderItemType
import com.abc.us.accounting.supports.converter.JsonConverter
import com.abc.us.accounting.supports.converter.toOffset
import com.abc.us.oms.domain.order.entity.*
import mu.KotlinLogging
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.ZoneOffset

class OrderItemBuilder {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val converter = JsonConverter()

        fun convertChargeStatus(status : String) : ChargeStatusEnum {

            return when(status) {
                "CREATED" -> ChargeStatusEnum.CREATED
                "SCHEDULED" -> ChargeStatusEnum.SCHEDULED
                "PENDING" -> ChargeStatusEnum.PENDING
                "PAID" -> ChargeStatusEnum.PAID
                "UNPAID" -> ChargeStatusEnum.UNPAID
                "OVERDUE" -> ChargeStatusEnum.OVERDUE
                else -> TODO()
            }
        }
        fun convertReceiptMethod(method : String) : ReceiptMethodEnum {
            return when(method) {
                "CREDIT_CARD" ->ReceiptMethodEnum.CREDIT_CARD
                "SHOP_PAY" -> ReceiptMethodEnum.SHOP_PAY
                "PAYPAL" ->ReceiptMethodEnum.PAYPAL
                else -> TODO()
            }
        }
        fun convertOrderItemType(type : String) : OrderItemType {
            return when(type) {
                "RENTAL" -> OrderItemType.RENTAL
                "PURCHASE" -> OrderItemType.ONETIME
                "AUTO_ORDER" -> OrderItemType.AUTO_ORDER
                else -> OrderItemType.NONE
            }
        }
        //fun convertItemStatus(type : String) :
        fun convertChannelType(type : String) : ChannelTypeEnum {
            return when(type) {
                "ONLINE_MALL" -> ChannelTypeEnum.ONLINE_MALL
                "CUSTOMER_CENTER" -> ChannelTypeEnum.CUSTOMER_CENTER
                "OFFLINE_STORE" -> ChannelTypeEnum.OFFLINE_STORE
                "SELLER" -> ChannelTypeEnum.SELLER
                "OTHER" -> ChannelTypeEnum.OTHER
                "ONLINE" -> ChannelTypeEnum.ONLINE_MALL
                else -> TODO()
            }
        }
        fun convertOrderProductType(type : String) : OrderProductTypeEnum {
            return when(type) {
                "INSTALL" -> OrderProductTypeEnum.INSTALL
                "COURIER" -> OrderProductTypeEnum.COURIER
                else -> TODO()
            }
        }

        fun convertChargeItemType(type : String) : ChargeItemEnum {
            return when(type) {
                "SERVICE_FEE" -> ChargeItemEnum.SERVICE_FEE
                "INSTALLATION_FEE" -> ChargeItemEnum.INSTALLATION_FEE
                "DISMANTILING_FEE" -> ChargeItemEnum.DISMANTILING_FEE
                "REINSTALLATION_FEE" -> ChargeItemEnum.REINSTALLATION_FEE
                "TERMINATION_PENALTY" -> ChargeItemEnum.TERMINATION_PENALTY
                "LATE_FEE" -> ChargeItemEnum.LATE_FEE
                "LOSS_FEE" -> ChargeItemEnum.LOSS_FEE
                "PART_COST" -> ChargeItemEnum.PART_COST
                "RENTAL_FEE" -> ChargeItemEnum.RENTAL_FEE
                "RELOCATION_FEE" -> ChargeItemEnum.RELOCATION_FEE
                else -> TODO()
            }
        }
        fun convertOrderItemStatus(status : String) : OrderItemStatus {
            return when(status) {
                "ORDER_RECEIVED" -> OrderItemStatus.ORDER_RECEIVED
                "ORDER_COMPLETED" -> OrderItemStatus.ORDER_COMPLETED
                "BOOKING_SCHEDULED" -> OrderItemStatus.BOOKING_SCHEDULED
                "BOOKING_CONFIRMED" -> OrderItemStatus.BOOKING_CONFIRMED
                "ORDER_ON_HOLD" -> OrderItemStatus.ORDER_ON_HOLD
                "WORK_IN_PROGRESS" -> OrderItemStatus.WORK_IN_PROGRESS
                "CONTRACT_CONFIRMED" -> OrderItemStatus.CONTRACT_CONFIRMED
                "INSTALL_COMPLETED" -> OrderItemStatus.INSTALL_COMPLETED
                "ORDER_CONFIRMED" -> OrderItemStatus.ORDER_CONFIRMED
                "CANCELLATION_RECEIVED" -> OrderItemStatus.CANCELLATION_RECEIVED
                "CANCELLATION_PROCESSING" -> OrderItemStatus.CANCELLATION_PROCESSING
                "CANCELLATION_REQUESTED" -> OrderItemStatus.CANCELLATION_REQUESTED
                "CANCELLATION_COMPLETED" -> OrderItemStatus.CANCELLATION_COMPLETED
                "RETURN_RECEIVED" -> OrderItemStatus.RETURN_RECEIVED
                "RETURN_PROCESSING" -> OrderItemStatus.RETURN_PROCESSING
                "RETURN_ON_HOLD" -> OrderItemStatus.RETURN_ON_HOLD
                "RETURN_COMPLETED" -> OrderItemStatus.RETURN_COMPLETED
                "REFUND_RECEIVED" -> OrderItemStatus.REFUND_RECEIVED
                "REFUND_PROCESSING" -> OrderItemStatus.REFUND_PROCESSING
                "REFUND_ON_HOLD" -> OrderItemStatus.REFUND_ON_HOLD
                "DISMANTLING_STARTED" -> OrderItemStatus.DISMANTLING_STARTED
                "MATERIAL_RETURNED" -> OrderItemStatus.MATERIAL_RETURNED
                "REFUND_COMPLETED" -> OrderItemStatus.REFUND_COMPLETED
                "SUSPENDED" -> OrderItemStatus.SUSPENDED
                "PRODUCT_PREPARING" -> OrderItemStatus.PRODUCT_PREPARING
                "DELIVERY_IN_PROGRESS" -> OrderItemStatus.DELIVERY_IN_PROGRESS
                "DELIVERY_COMPLETED" -> OrderItemStatus.DELIVERY_COMPLETED
                else -> TODO()
            }
        }
//        fun buildChargeItem(chargeItems: MutableList<ChargeItem>) : MutableList<CollectChargeItem>{
//            val collects = mutableListOf<CollectChargeItem>()
//
//            chargeItems.forEach { item ->
//                val price = EmbeddablePrice(totalPrice = BigDecimal(item.totalPrice),
//                                            currency = item.currency)
//                    .apply {
//                        discountPrice = BigDecimal(item.discountPrice)
//                        itemPrice = BigDecimal(item.itemPrice)
//                        prepaidAmount = item.prepaidAmount?.let { BigDecimal(it) }
//                        isTaxExempt = item.isTaxExempt
//                }
//                //val relation = EmbeddableRelation().apply {  }
//                val collect = CollectChargeItem(price = price,
//                                  //relation = relation,
//                                  chargeItemId = item.id,
//                                  chargeId = item.chargeId,
//                                  chargeItemType =  convertChargeItemType(item.chargeItemType))
//                    .apply {
//                        serviceFlowId = item.serviceFlowId
//                        quantity = item.quantity
//                        createTime = item.createTime.let { LocalDateTimeToOffsetDateTime.convert(it) }
//                        updateTime = item.updateTime.let { LocalDateTimeToOffsetDateTime.convert(it) }
//                }
//                collects.add(collect)
//            }
//            return collects
//
//        }
//        fun buildCharges(contract : Contract) : MutableList<CollectCharge> {
//            val collects = mutableListOf<CollectCharge>()
//            contract.charges.forEach { charge ->
//                val price = EmbeddablePrice(totalPrice = BigDecimal(0.00),
//                                            currency = "USD")
//                    .apply {
//                        //tax = charge.payment?.let { payment -> payment.tax?.let { BigDecimal(it) } }
//                    }
//                val collect = CollectCharge(chargeId = charge.id,
//                                            price = price,
//                                            chargeStatus = convertChargeStatus(charge.chargeStatus),
//                                            billingCycle = charge.billingCycle,
//                                            contractId = charge.contractId).apply {
//
//                    targetMonth = charge.targetMonth
//                    //totalPrice = charge.totalPrice
//                    startDate = charge.startDate
//                    endDate = charge.endDate
//                    createTime = contract.createTime?.let { LocalDateTimeToOffsetDateTime.convert(it) }
//                    updateTime = contract.updateTime?.let { LocalDateTimeToOffsetDateTime.convert(it) }
//                    chargeItems = ChargeItemBuilder.build(charge)
//                    //receipt = charge.payment?.let { ReceiptBuilder.build(it) }
//                    //receipt = charge.chargePayment
//                }
//                collects.add(collect)
//            }
//            return collects
//        }
        fun buildInstallation(orderItem : OrderItem) : CollectInstallation? {
            return orderItem.installationInformation?.let { install ->
                return CollectInstallation().apply {
                    relation = EmbeddableRelation().apply {
                        entity = CollectOrderItem::class.simpleName
                        field = "order_item_id"
                        value = install.orderItemId
                    }
                    location = EmbeddableLocation().apply {
                        address1 = install.address1
                        address2 = install.address2
                        zipCode = install.zipcode
                        city = install.city
                        state = install.state
                        latitude = install.latitude
                        longitude = install.longitude
                        technicianId = install.technicianId
                        serviceFlowId = install.serviceFlowId
                        branchId = install.branchId
                        warehouseId = install.warehouseId
//                        createTime = OffsetDateTime.now()
//                        updateTime = OffsetDateTime.now()
                    }
                    installId = install.id
                    orderItemId = install.orderItemId
                    serialNumber = install.serialNumber
                    serialNumber = install.serialNumber
                    installationTime = install.installationTime?.toOffset()
                    warrantyStartTime = install.warrantyStartTime?.toOffset()
                    warrantyEndTime = install.warrantyEndTime?.toOffset()
                    waterType = install.waterType
                    createTime = install.createTime?.toOffset()
                    updateTime =install.updateTime?.toOffset()
                }
            }
        }

//        fun buildContracts(orderItem : OmsOrderItem) : MutableList<CollectContract> {
//            val collects = mutableListOf<CollectContract>()
//            orderItem.contract?.let {
//                collects.add(ContractBuilder.build(orderItem,it))
//            }
//            return collects
//        }

        fun buildContracts(orderItem : OrderItem) : MutableList<CollectContract> {
            val collects = mutableListOf<CollectContract>()
            orderItem.contracts?.let { contracts ->
                contracts.forEach { contract ->
                    collects.add(ContractBuilder.build(orderItem,contract))
                }
            }
            return collects
        }

        fun buildPromotions(orderItem : OrderItem) : MutableList<CollectPromotion> {
            val collects = mutableListOf<CollectPromotion>()
            orderItem.orderItemPromotions.forEach { p ->
                val collect = p.promotion?.let {promotion ->
                    CollectPromotion().apply {
                        relation = EmbeddableRelation().apply {
                            entity = CollectOrderItem::class.simpleName
                            field = "order_item_id"
                            value = orderItem.id
                        }
                        promotionId = promotion.id
                        promotionName = promotion.promotionName
                        promotionType = promotion.promotionType
                        amount = promotion.promotionDiscount?.let { it.amount?.let { a -> BigDecimal(a) } }
                        rate = promotion.promotionDiscount?.let { it.rate?.let { r -> BigDecimal(r) } }
                    }
                }
                collect?.let { collects.add(it) }
            }
            return collects

        }

        fun buildTaxLines(payment : Payment) : MutableList<CollectTaxLine> {
            val collects = mutableListOf<CollectTaxLine>()
            payment.taxLines.forEach { taxLine ->
                val collect = TaxLineBuilder.buildFromLine(r = EmbeddableRelation().apply {
                    entity = CollectReceipt::class.simpleName
                    field = "receipt_id"
                    value = payment.id},taxLine)
                collects.add(collect)
            }
            return collects
        }

        fun buildTaxLines(orderItem : OrderItem) : MutableList<CollectTaxLine> {
            val collects = mutableListOf<CollectTaxLine>()
            orderItem.taxLines.forEach { taxLine ->
                val collect = TaxLineBuilder.buildFromLine(r= EmbeddableRelation().apply {
                    entity = CollectOrder::class.simpleName
                    field = "order_item_id"
                    value = orderItem.id
                },taxLine)
                collects.add(collect)
            }
            return collects
        }


        fun buildLocation(a : DeliveryAddress) : CollectLocation {
            return CollectLocation().apply {
                locationId = a.id
                relation = EmbeddableRelation().apply {
                    entity = CollectOrder::class.simpleName
                    field = "order_id"
                    value = a.orderId
                }
                name = EmbeddableName().apply {
                    lastName = a.lastName
                    firstName = a.firstName
                    primaryPhone = a.phone
                    primaryEmail = a.email

                }
                location = EmbeddableLocation().apply {
                    address1 = a.address1
                    address2 = a.address2
                    zipCode = a.zipcode
                    city = a.city
                    state = a.state
                    latitude = a.latitude
                    longitude = a.longitude
                    locationRemark = a.remark
                }
            }
        }

        fun buildReceipt( p : Payment) : CollectReceipt {
            val price = EmbeddablePrice(totalPrice = BigDecimal(p.totalPrice),
                                        currency = p.currency)
                .apply {
                    discountPrice = BigDecimal(p.discountPrice)
                    itemPrice = BigDecimal(p.itemPrice)
                    prepaidAmount = p.prepaidAmount.let { BigDecimal(it) }
                    tax = BigDecimal(p.tax)
                    registrationPrice =p.registrationPrice.let { BigDecimal(it) }
                }
            val relation = EmbeddableRelation().apply {
                entity = CollectOrder::class.simpleName
                field = "order_id"
                value = p.orderId
            }
            val name = EmbeddableName().apply {
                    firstName = p.firstName
                    lastName = p.lastName
                    primaryEmail = p.email
                    primaryPhone = p.phone
                }
            val location = EmbeddableLocation()
                .apply {
                    address1 = p.address1
                    address2 = p.address2
                    state = p.state
                    city = p.city
                    zipCode = p.zipcode
                }

            return CollectReceipt(price = price,
                                  relation = relation,
                                  name = name,
                                  location = location).apply {

                receiptId = p.payoutId
                receiptMethod = convertReceiptMethod(p.paymentMethod)
                transactionId = p.transactionId
                receiptTime = p.paymentTime?.toOffset()
                cardNumber = p.cardNumber
                installmentMonths = p.installmentMonths
                taxLines = buildTaxLines(p)

            }
        }

        fun buildChannel(order : Order) : CollectChannel? {
            return order.channel?.let { c->
                CollectChannel().apply {
//                    relation = EmbeddableRelation().apply {
//                        entity = CollectOrder::class.simpleName
//                        field = "order_id"
//                        value = order.id
//                    }
                    channelId = c.id
                    channelType = c.channelType.let { convertChannelType(it) }
                    channelName = c.channelName
                    channelDetail = c.channelDetail
                    createTime = c.createTime?.toOffset()
                    updateTime = c.updateTime?.toOffset()
                }
            }
        }

        fun buildOrder(orderItem : OrderItem) : CollectOrder? {
            return orderItem.order?.let { o ->
                CollectOrder(orderId = orderItem.orderId,
                             customerId = o.customerId).apply {
                    channelId = o.channelId
                    channelOrderId = o.channelOrderId
                    orderProductType = o.orderProductType?.let { convertOrderProductType(it) }
                    referrerCode = o.referrerCode
                    orderCreateTime = o.orderCreateTime?.toOffset()
                    orderUpdateTime = o.orderCreateTime?.toOffset()
                    createTime = o.createTime?.toOffset()
                    updateTime = o.updateTime?.toOffset()
                    deliveryAddress = o.deliveryAddress?.let { buildLocation(it) }
                    receipt = o.payment?.let { buildReceipt(it) }
                    channel = buildChannel(o)
                    customer = o.customer?.let { CustomerBuilder.build(it) }
                }
            }
        }

        fun buildShipping(shipping : ShippingInformation) : CollectShipping {
            return CollectShipping(shippingId = shipping.id,
                serviceFlowId = shipping.serviceFlowId,
                trackingId = shipping.trackingId, )
                .apply {
                    companyName = shipping.companyName
                    companyCode = shipping.companyCode
                    trackingUrl = shipping.trackingUrl
                    shipmentDate = shipping.shipmentDate
                    deliveryDate = shipping.deliveryDate
                    estimatedDeliveryDate= shipping.estimatedDeliveryDate
                }
        }

//        fun build(orderItem : OmsOrderItem) : CollectOrderItem {
//            // TODO hschoid --> contract 가 N개 일때 contractId 가져오는 로직으로 변경해야함!!
//            val buildedContracts = buildContracts(orderItem)
//            var activeContractId: String? = null
//            if(buildedContracts.isNotEmpty())
//                activeContractId = buildedContracts[0].contractId
//
//            var buildedShipping = orderItem.shippingInformation?.let { buildShipping(it) }
//            var buildedShippingId = buildedShipping?.let { it.shippingId }
//
//            return CollectOrderItem(orderId = orderItem.orderId,
//                orderItemId = orderItem.id,
//                orderItemStatus = convertOrderItemStatus(orderItem.orderItemStatus),
//                orderItemType = convertOrderItemType(orderItem.orderItemType)).apply {
//                relation = EmbeddableRelation().apply {
//                    entity = CollectOrder::class.simpleName
//                    field = "order_id"
//                    value = orderItem.orderId
//                }
//
//                channelOrderId = orderItem.channelOrderId
//                channelOrderItemId = orderItem.channelOrderItemId
//                materialId = orderItem.materialId
//                installId = orderItem.installationInformation?.let { i -> i.id }
//                contractId = activeContractId
//
//                quantity = orderItem.quantity
//                price = EmbeddablePrice(totalPrice = BigDecimal(orderItem.totalPrice),
//                    currency = orderItem.currency).apply {
//                    itemPrice = BigDecimal(orderItem.itemPrice)
//                    discountPrice = BigDecimal(orderItem.discountPrice)
//                    registrationPrice = BigDecimal(orderItem.registrationPrice)
//                    totalPrice = BigDecimal(orderItem.totalPrice)
//                    //prepaidAmount = BigDecimal(orderItem.prepaidAmount)
//                    tax = BigDecimal(orderItem.tax)
//                }
//                createTime = orderItem.createTime?.let { LocalDateTimeToOffsetDateTime.convert(it) }
//                updateTime = orderItem.updateTime?.let { LocalDateTimeToOffsetDateTime.convert(it) }
//                order = buildOrder(orderItem)
//                contracts = buildedContracts
//                installation = buildInstallation(orderItem)
//                promotions = buildPromotions(orderItem)
//                taxLines = buildTaxLines(orderItem)
//                shipping = buildedShipping
//                shippingId = buildedShippingId
//            }
//        }
        fun build(orderItem : OmsEntityOrderItem) : CollectOrderItem{
            // TODO hschoid --> contract 가 N개 일때 contractId 가져오는 로직으로 변경해야함!!

            val buildedContracts = buildContracts(orderItem)
            var activeContractId: String? = null
            if(buildedContracts.isNotEmpty())
                activeContractId = buildedContracts[0].contractId
            var buildedShipping = orderItem.shippingInformation?.let { buildShipping(it) }
            var buildedShippingId = orderItem.shippingInformationId

            return CollectOrderItem(orderId = orderItem.orderId,
                                    orderItemId = orderItem.id,
                                    orderItemStatus = convertOrderItemStatus(orderItem.orderItemStatusCode),
                                    orderItemType = convertOrderItemType(orderItem.orderItemType)).apply {
                relation = EmbeddableRelation().apply {
                    entity = CollectOrder::class.simpleName
                    field = "order_id"
                    value = orderItem.orderId
                }

                channelOrderId = orderItem.channelOrderId
                channelOrderItemId = orderItem.channelOrderItemId
                materialId = orderItem.materialId
                installId = orderItem.installationInformation?.let { i -> i.id }
                contractId = activeContractId
                quantity = orderItem.quantity
                price = EmbeddablePrice(totalPrice = BigDecimal(orderItem.totalPrice),
                                        currency = orderItem.currency).apply {
                    itemPrice = BigDecimal(orderItem.itemPrice)
                    discountPrice = BigDecimal(orderItem.discountPrice)
                    registrationPrice = BigDecimal(orderItem.registrationPrice)
                    totalPrice = BigDecimal(orderItem.totalPrice)
                    //prepaidAmount = BigDecimal(orderItem.prepaidAmount)
                    tax = BigDecimal(orderItem.tax)
                }
                createTime = orderItem.createTime?.toOffset()
                updateTime = orderItem.updateTime?.toOffset()
                order = buildOrder(orderItem)
                contracts = buildedContracts
                installation = buildInstallation(orderItem)
                promotions = buildPromotions(orderItem)
                taxLines = buildTaxLines(orderItem)
                shipping = buildedShipping
                shippingId = buildedShippingId
                material = orderItem.material?.let { MaterialBuilder.build(it) }
            }
        }
//        fun builds(orderItems: OmsOrderItemMutableList) : MutableList<CollectOrderItem> {
//            val collects = mutableListOf< CollectOrderItem>()
//            orderItems.forEach { orderItem ->
//
//                collects.add(build(orderItem))
//            }
//            return collects
//        }

//        fun builds(orderItems: OmsApiOrderItemMutableList): MutableList<CollectOrderItem> {
//            val orders = mutableListOf< CollectOrderItem>()
////            originOrderItems.forEach { order ->
////                val orderItem = makeOrderItem(order)
////                val jsonData = converter.toJson(orderItem)
////                logger.info { "collect-order-item = [${jsonData}]" }
////                orders.add(orderItem)
////            }
//            return orders
//        }

        fun builds(orderItems: OmsEntityOrderItemMutableList) : MutableList<CollectOrderItem> {
            val collects = mutableListOf< CollectOrderItem>()
            orderItems.forEach { orderItem ->

                collects.add(build(orderItem))
            }
            return collects
        }
    }
}