package com.abc.us.accounting.collects.service

//import com.abc.us.accounting.collects.domain.entity.collect.CollectCharge
//import com.abc.us.accounting.collects.domain.entity.collect.CollectChargeItem
//import com.abc.us.accounting.collects.domain.entity.collect.ar.*
//import com.abc.us.accounting.collects.domain.entity.collect.ar.ARLocation
//import com.abc.us.accounting.collects.domain.entity.collect.ar.ARPayment
//import com.abc.us.accounting.collects.domain.repository.*
//import com.abc.us.accounting.collects.domain.type.ARChargeItemType
//import com.abc.us.accounting.collects.domain.type.ARChargePaymentMethod
//import com.abc.us.accounting.collects.domain.type.ARChargeStatus
//import com.abc.us.accounting.collects.model.*
//import com.abc.us.accounting.supports.converter.JsonConverter
//import com.abc.us.accounting.supports.entity.toEntityHash
//import com.abc.us.generated.models.*
//import com.fasterxml.jackson.core.type.TypeReference
//import jakarta.transaction.Transactional
//import mu.KotlinLogging
//import org.springframework.stereotype.Service
//import java.math.BigDecimal
//import java.time.OffsetDateTime
//
//@Service
//class ARService(
//    private val chargeRepository: CollectChargeRepository,
//    private val chargeItemRepository: CollectChargeItemRepository,
//    private val paymentRepository: PaymentRepository,
//    private val locationRepository: LocationRepository,
//    private val contractRepository : CollectContractRepository
//) {
//    companion object {
//        private val logger = KotlinLogging.logger {}
//    }
//    private fun mapChargeStatus(status: ChargeStatus?): ARChargeStatus? {
//        return when (status) {
//            ChargeStatus.CREATED -> ARChargeStatus.CREATED
//            ChargeStatus.SCHEDULED -> ARChargeStatus.SCHEDULED
//            ChargeStatus.PENDING -> ARChargeStatus.PENDING
//            ChargeStatus.PAID -> ARChargeStatus.PAID
//            ChargeStatus.UNPAID -> ARChargeStatus.UNPAID
//            ChargeStatus.OVERDUE -> ARChargeStatus.OVERDUE
//            null -> null
//        }
//    }
//    fun generateCollectCharge(charge: OmsBillingCharge) : CollectCharge {
//        return CollectCharge(
//            chargeId = charge.chargeId,
//            billingCycle = charge.billingCycle,
//            targetMonth = charge.targetMonth,
//            totalPrice = charge.totalPrice?.let { BigDecimal(it) },
//            chargeStatus = mapChargeStatus(charge.chargeStatus),
//            contractId = charge.contractId,
//            paymentId = charge.payment?.invoiceId, // Payment ID로 참조
//            createTime = charge.createTime,
//            updateTime = charge.updateTime,
//            isActive = true
//        )
//    }
//    @Transactional
//    fun saveFromCharges(jsonData: String) {
//        val omsCharges = Json2Charge().convert(jsonData)
//        val charges = omsCharges.map {
//            Charge2DB(this).convert(it)
//        }
//        chargeRepository.saveAll(charges)
//
//        logger.debug { "Saving AccountsReceivable data: ${charges}" }
//        charges.forEach { ar ->
//            // Check if existing active record exists
//            val existingAr = chargeRepository.findByChargeIdAndIsActive(ar.chargeId!!)
//            if (existingAr.isPresent) {
//                logger.debug { "Updating existing AccountsReceivable: ${ar.chargeId}" }
//                val existingEntity = existingAr.get()
//                // Update logic (merge details, e.g., AR items, Payment)
//                mergeAccountsReceivable(existingEntity, ar)
//                chargeRepository.save(existingEntity)
//            } else {
//                logger.debug { "Creating new AccountsReceivable: ${ar.chargeId}" }
//                chargeRepository.save(ar)
//            }
//        }
//    }
//
//    @Transactional
//    fun saveFromAR(ar: AccountsReceivable) {
//
//        // Convert AccountsReceivable DTO to CollectAccountsReceivable entity
//        val collectAr = CollectCharge(
//            chargeId = ar.arId,
//            billingCycle = ar.billingCycle,
//            targetMonth = ar.targetMonth,
//            totalPrice = ar.totalPrice,
//            chargeStatus = ar.arStatus,
//            contractId = ar.contract?.let { it.contractId },
//            createTime = ar.createTime,
//            updateTime = ar.updateTime,
//            isActive = true
//        ).apply { hashCode = toEntityHash() }
//
//        // Save AccountsReceivable entity
//        chargeRepository.save(collectAr)
//
//        // Convert AR Items and save
//        val chargeItems = ar.arItems.map { dto ->
//            CollectChargeItem(
//                chargeItemId = dto.arItemId,
//                chargeId = collectAr.chargeId,
//                chargeItemType = dto.arItemType,
//                serviceFlowId = dto.serviceFlowId,
//                quantity = dto.quantity,
//                totalPrice = dto.totalPrice,
//                embeddablePriceDetail = dto.priceDetail?.let {
//                    EmbeddablePriceDetail(
//                        discountPrice = it.discountPrice,
//                        itemPrice = it.itemPrice,
//                        prepaidAmount = it.prepaidAmount,
//                        tax = it.tax,
//                        currency = it.currency,
//                        embeddableTaxLines = it.taxLines.map { taxLine ->
//                            EmbeddableTaxLine(
//                                title = taxLine.title,
//                                rate = taxLine.rate,
//                                price = taxLine.price
//                            )
//                        }.toSet(),
//                        embeddablePromotions = it.promotions.map { promotion ->
//                            EmbeddablePromotion(
//                                promotionId = promotion.promotionId,
//                                promotionName = promotion.promotionName,
//                                promotionDescription = promotion.promotionDescription,
//                                startDate = promotion.startDate,
//                                endDate = promotion.endDate,
//                                discountPrice = promotion.discountPrice
//                            )
//                        }
//                    )
//                },
//                isTaxExempt = dto.isTaxExempt,
//                createTime = dto.createTime,
//                isActive = true
//            ).apply { hashCode = toEntityHash() }
//        }
//
//        chargeItemRepository.saveAll(chargeItems)
//
//        // Convert and save Payment if exists
//        ar.payment?.let { dto ->
//            val payment = ARPayment(
//                paymentId = dto.paymentId,
//                chargeId = collectAr.chargeId,
//                totalPrice = dto.totalPrice,
//                paymentMethod = ARChargePaymentMethod.valueOf(dto.paymentMethod),
//                transactionId = dto.transactionId,
//                cardNumber = dto.cardNumber,
//                cardType = dto.cardType,
//                installmentMonths = dto.installmentMonths,
//                paymentTime = dto.paymentTime,
//                billingAddress = dto.billingAddress?.let {
//                    ARLocation(
//                        firstName = it.firstName,
//                        lastName = it.lastName,
//                        email = it.email,
//                        phone = it.phone,
//                        mobile = it.mobile,
//                        state = it.state,
//                        city = it.city,
//                        address1 = it.address1,
//                        address2 = it.address2,
//                        zipcode = it.zipcode,
//                        remark = it.remark,
//                        isActive = true
//                    ).apply { hashCode = toEntityHash() }
//                },
//                isActive = true
//            ).apply { hashCode = toEntityHash() }
//
//            paymentRepository.save(payment)
//
//            // Save Location separately if it exists
//            payment.billingAddress?.let { locationRepository.save(it) }
//        }
//    }
//
//
//
//    @Transactional
//    fun findById(chargeId: String): AccountsReceivable {
//        // AccountsReceivable 가져오기
//        val charge = chargeRepository.findByChargeIdAndIsActive(chargeId)
//            .orElseThrow { IllegalArgumentException("AccountsReceivable with arId=$chargeId not found") }
//        return Charge2AR(this).convert(charge)
//    }
//
//    fun findAll() : List<AccountsReceivable> {
//        var allCharge =  chargeRepository.findAllActive().orElseThrow{
//            IllegalArgumentException("AccountsReceivable empty")
//        }
//        return allCharge.map { charge -> Charge2AR(this).convert(charge) }
//    }
//    private fun mergeAccountsReceivable(existing: CollectCharge, updated: CollectCharge) {
//        existing.billingCycle = updated.billingCycle
//        existing.targetMonth = updated.targetMonth
//        existing.totalPrice = updated.totalPrice
//        existing.chargeStatus = updated.chargeStatus
//        existing.contractId = updated.contractId
//        existing.paymentId = updated.paymentId // Payment ID 업데이트
//        existing.updateTime = OffsetDateTime.now() // 업데이트 시간은 현재 시간으로 설정
//    }
//
//    class Json2Charge {
//        fun convert(jsonData: String): List<OmsBillingCharge> {
//            val converter = JsonConverter()
//            try {
//                val chargeResponse = converter.toObjFromTypeRef(
//                    jsonData,
//                    object : TypeReference<OmsChargesResponse>() {}
//                )
//                return chargeResponse.data?.items ?: emptyList()
//            } catch (e: Exception) {
//                throw IllegalArgumentException("Failed to parse JSON: ${e.message}", e)
//            }
//        }
//    }
//
//    class Charge2AR(private val service : ARService) {
//        fun convert(ar: CollectCharge): AccountsReceivable {
//            // AccountsReceivableItem과 관련된 데이터 가져오기
//            //val arItems = service.arItemRepository.findItemsWithDetailsByArId(ar.arId!!)
//            val chargeItems = getItemsWithDetails(ar.chargeId!!)
//            val payment = ar.paymentId?.let { paymentId ->
//                service.paymentRepository.findByPaymentIdAndIsActive(paymentId)?.let { toPaymentDTO(it.get(),chargeItems) }
//            }
//
//            val activeContract = ar.contractId?.let {
//                service.contractRepository.findByContractIdAndActiveIsTrue(it).orElse(null)
//            }
//
//
//            return AccountsReceivable(
//                arId = ar.chargeId!!,
//                billingCycle = ar.billingCycle!!,
//                targetMonth = ar.targetMonth!!,
//                totalPrice = ar.totalPrice?.let { it }?:BigDecimal(0.0),
//                arStatus = ar.chargeStatus!!,
//                createTime = ar.createTime!!,
//                updateTime = ar.updateTime!!,
//                //contractId = ar.contractId,
//                contract = activeContract,
//                payment = payment,
//                arItems = chargeItems.toMutableList()
//            )
//        }
//        fun getItemsWithDetails(chargeId: String): List<AccountsReceivableItem> {
//            val flatItems = service.chargeItemRepository.findFlatItemsByChargeId(chargeId)
//
//            // Group by AR Item ID to reassemble the structure
//            val groupedItems = flatItems.groupBy { it["arItemId"] as String }
//
//            return groupedItems.map { (chargeItemId, rows) ->
//                val firstRow = rows.first()
//
//                val taxLines = rows.mapNotNull {
//                    if (it["taxLineTitle"] != null) {
//                        TaxLine(
//                            title = it["taxLineTitle"] as String,
//                            rate = it["taxLineRate"] as BigDecimal,
//                            price = it["taxLinePrice"] as BigDecimal
//                        )
//                    } else null
//                }.toMutableList()
//
////                val promotions = rows.mapNotNull {
////                    if (it["promotionId"] != null) {
////                        PromotionDTO(
////                            promotionId = it["promotionId"] as String,
////                            promotionName = it["promotionName"] as String,
////                            promotionDescription = it["promotionDescription"] as String,
////                            startDate = it["promotionStartDate"] as OffsetDateTime,
////                            endDate = it["promotionEndDate"] as OffsetDateTime,
////                            discountPrice = it["promotionDiscountPrice"] as BigDecimal
////                        )
////                    } else null
////                }.toMutableList()
//
//                val priceDetail = PriceDetail(
//                    discountPrice = firstRow["discountPrice"] as BigDecimal,
//                    itemPrice = firstRow["itemPrice"] as BigDecimal,
//                    prepaidAmount = firstRow["prepaidAmount"] as BigDecimal,
//                    tax = firstRow["tax"] as BigDecimal,
//                    currency = firstRow["currency"] as String,
//                    taxLines = taxLines,
//                    promotions = emptyList()
//                )
//
//                AccountsReceivableItem(
//                    arItemId = chargeItemId,
//                    arItemType = firstRow["chargeItemType"] as ARChargeItemType,
//                    serviceFlowId = firstRow["serviceFlowId"] as String?,
//                    quantity = firstRow["quantity"] as Int,
//                    totalPrice = firstRow["totalPrice"] as BigDecimal,
//                    isTaxExempt = firstRow["isTaxExempt"] as Boolean,
//                    createTime = firstRow["createTime"] as OffsetDateTime,
//                    priceDetail = priceDetail
//                )
//            }
//        }
//        private fun toPaymentDTO(payment: ARPayment, chargeItems : List<AccountsReceivableItem>): PaymentDTO {
//            val billingAddress = payment.billingAddress?.let { toLocationDTO(it) }
//            return PaymentDTO(
//                paymentId = payment.paymentId!!,
//                arId = payment.chargeId!!,
//                totalPrice = payment.totalPrice?.let { it }?:BigDecimal(0.0),
//                paymentMethod = payment.paymentMethod!!.name,
//                transactionId = payment.transactionId,
//                cardNumber = payment.cardNumber,
//                cardType = payment.cardType,
//                installmentMonths = payment.installmentMonths!!,
//                paymentTime = payment.paymentTime!!,
//                billingAddress = billingAddress,
//                arItems = chargeItems
//            )
//        }
//
//        private fun toLocationDTO(location: ARLocation): LocationDTO {
//            return LocationDTO(
//                firstName = location.firstName ?: "",
//                lastName = location.lastName ?: "",
//                email = location.email ?: "",
//                phone = location.phone ?: "",
//                mobile = location.mobile?: "",
//                state = location.state ?: "",
//                city = location.city ?: "",
//                address1 = location.address1 ?: "",
//                address2 = location.address2,
//                zipcode = location.zipcode ?: "",
//                remark = location.remark
//            )
//        }
//
//
//    }
//
//    class Charge2DB(private val service : ARService) {
//
//
//
//        fun convert(charge: OmsBillingCharge): CollectCharge {
//            val ar = CollectCharge(
//                chargeId = charge.chargeId,
//                billingCycle = charge.billingCycle,
//                targetMonth = charge.targetMonth,
//                totalPrice = charge.totalPrice?.let { BigDecimal(it) },
//                chargeStatus = mapChargeStatus(charge.chargeStatus),
//                contractId = charge.contractId,
//                paymentId = charge.payment?.invoiceId, // Payment ID로 참조
//                createTime = charge.createTime,
//                updateTime = charge.updateTime,
//                isActive = true
//            ).apply { hashCode = toEntityHash() }
//
//            // Save AccountsReceivableItem separately
//            val arItems = charge.chargeItems.map {
//                val arItem = toAccountsReceivableItem(it)
//                arItem.chargeId= charge.chargeId
//                arItem
//            }
//            service.chargeItemRepository.saveAll(arItems)
//
//            // Save Payment and Location separately
//            charge.payment?.let { payment ->
//                val location = payment.billingAddress?.let { toLocation(it) }
//                location?.let { service.locationRepository.save(it) }
//
//                val newPayment = toPayment(payment, location)
//                service.paymentRepository.save(newPayment)
//            }
//
//            return ar
//        }
//
//        private fun mapChargeStatus(status: ChargeStatus?): ARChargeStatus? {
//            return when (status) {
//                ChargeStatus.CREATED -> ARChargeStatus.CREATED
//                ChargeStatus.SCHEDULED -> ARChargeStatus.SCHEDULED
//                ChargeStatus.PENDING -> ARChargeStatus.PENDING
//                ChargeStatus.PAID -> ARChargeStatus.PAID
//                ChargeStatus.UNPAID -> ARChargeStatus.UNPAID
//                ChargeStatus.OVERDUE -> ARChargeStatus.OVERDUE
//                null -> null
//            }
//        }
//
//        fun toAccountsReceivableItem(chargeItem: OmsBillingChargeItem): CollectChargeItem {
//            return CollectChargeItem(
//                chargeItemId = chargeItem.chargeItemId,
//                chargeItemType = mapChargeItemType(chargeItem.chargeItemType),
//                serviceFlowId = chargeItem.serviceFlowId,
//                quantity = chargeItem.quantity,
//                totalPrice = chargeItem.totalPrice?.let { BigDecimal(it) },
//                embeddablePriceDetail = chargeItem.priceDetail?.let { toPriceDetail(it) },
//                isTaxExempt = chargeItem.isTaxExempt ?: false,
//                createTime = chargeItem.createTime,
//                isActive = true
//            ).apply { hashCode = toEntityHash() }
//        }
//
//        private fun mapChargeItemType(type: ChargeItemType?): ARChargeItemType? {
//            return when (type) {
//                ChargeItemType.SERVICE_FEE -> ARChargeItemType.SERVICE_FEE
//                ChargeItemType.INSTALLATION_FEE -> ARChargeItemType.INSTALLATION_FEE
//                ChargeItemType.DISMANTILING_FEE -> ARChargeItemType.DISMANTILING_FEE
//                ChargeItemType.REINSTALLATION_FEE -> ARChargeItemType.REINSTALLATION_FEE
//                ChargeItemType.TERMINATION_PENALTY -> ARChargeItemType.TERMINATION_PENALTY
//                ChargeItemType.LATE_FEE -> ARChargeItemType.LATE_FEE
//                ChargeItemType.LOSS_FEE -> ARChargeItemType.LOSS_FEE
//                ChargeItemType.PART_COST -> ARChargeItemType.PART_COST
//                ChargeItemType.RENTAL_FEE -> ARChargeItemType.RENTAL_FEE
//                null -> null
//            }
//        }
//
//        fun toPayment(omsPayment: OmsBillingPayment, location: ARLocation?): ARPayment {
//            return ARPayment(
//                paymentId = omsPayment.invoiceId,
//                chargeId = omsPayment.chargeId,
//                totalPrice = omsPayment.totalPrice?.let { BigDecimal(it) },
//                paymentMethod = mapPaymentMethod(omsPayment.paymentMethod),
//                transactionId = omsPayment.transactionId,
//                cardNumber = omsPayment.cardNumber,
//                cardType = omsPayment.cardType,
//                installmentMonths = omsPayment.installmentMonths,
//                paymentTime = omsPayment.paymentTime,
//                billingAddress = location,
//                isActive = true
//            ).apply { hashCode = toEntityHash() }
//        }
//
//        private fun mapPaymentMethod(method: PaymentMethod?): ARChargePaymentMethod? {
//            return when (method) {
//                PaymentMethod.CREDIT_CARD -> ARChargePaymentMethod.CREDIT_CARD
//                PaymentMethod.SHOP_PAY -> ARChargePaymentMethod.SHOP_PAY
//                PaymentMethod.PAYPAL -> ARChargePaymentMethod.PAYPAL
//                null -> null
//            }
//        }
//
//        fun toLocation(dto: AddressProperties): ARLocation {
//            return ARLocation(
//                firstName = dto.firstName,
//                lastName = dto.lastName,
//                email = dto.email,
//                phone = dto.phone,
//                mobile = dto.mobile,
//                state = dto.state,
//                city = dto.city,
//                address1 = dto.address1,
//                address2 = dto.address2,
//                zipcode = dto.zipcode,
//                remark = dto.remark,
//                isActive = true
//            ).apply { hashCode = toEntityHash() }
//        }
//
//        fun toPriceDetail(omsPrice: OmsChargePriceDetail): EmbeddablePriceDetail {
//
//            return EmbeddablePriceDetail(
//                discountPrice = omsPrice.discountPrice?.let { BigDecimal(it) },
//                itemPrice = omsPrice.itemPrice?.let { BigDecimal(it) },
//                prepaidAmount = omsPrice.prepaidAmount?.let { BigDecimal(it) },
//                tax = omsPrice.tax?.let { BigDecimal(it) },
//                embeddableTaxLines = omsPrice.taxLines?.map { toTaxLine(it) }?.toSet() ?: emptySet(), // List를 Set으로 변환
//                embeddablePromotions = omsPrice.promotions?.map { toPromotion(it) } ?: emptyList(),   // null 대신 빈 리스트 처리
//                currency = omsPrice.currency
//            )
//        }
//
//        fun toTaxLine(line: TaxLineProperties): EmbeddableTaxLine {
//            return EmbeddableTaxLine(
//                title = line.title,
//                rate = line.rate?.let { BigDecimal(it) },
//                price = line.price?.let { BigDecimal(it) }
//            )
//        }
//
//        fun toPromotion(dto: OrderItemPromotion): EmbeddablePromotion {
//            return EmbeddablePromotion(
//                promotionId = dto.promotionId,
//                promotionName = dto.promotionName,
//                promotionDescription = dto.promotionDescription,
//                startDate = dto.startDate,
//                endDate = dto.endDate,
//                discountPrice = dto.discountPrice?.let { BigDecimal(it) }
//            )
//        }
//    }
//}
//
