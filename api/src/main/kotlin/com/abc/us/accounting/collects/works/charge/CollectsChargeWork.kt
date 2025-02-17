package com.abc.us.accounting.collects.works.charge

import com.abc.us.accounting.collects.collectable.ChargeCollectable
import com.abc.us.accounting.collects.domain.entity.collect.CollectCharge
import com.abc.us.accounting.collects.domain.entity.collect.CollectChargeItem
import com.abc.us.accounting.collects.domain.entity.collect.CollectReceipt
import com.abc.us.accounting.collects.domain.entity.collect.CollectTaxLine
import com.abc.us.accounting.collects.domain.repository.*
import com.abc.us.accounting.collects.helper.OmsApiChargeMutableList
import com.abc.us.accounting.collects.helper.builder.ChargeBuilder
import com.abc.us.accounting.commons.domain.type.TimeZoneCode
import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.client.OmsClient
import com.abc.us.accounting.supports.converter.toUTCLocalDate
import com.abc.us.accounting.supports.entity.BulkDistinctInserter
import com.abc.us.generated.models.GetBillingChargesChargeSearchParameter
import com.abc.us.generated.models.OmsBillingCharge
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class CollectsChargeWork(
    @Value("\${abc-sdk.api-key}")
    private val xAbcSdkApikey: String,
    @Value("\${collects.read.page.sort-by:createTime}")
    private val sortProperty: String,
    @Value("\${collects.read.page.max-size:100}")
    private val pageSize: Int,
    @Lazy
    private val omsClient : OmsClient,
    private val chargeRepository: CollectChargeRepository,
    private val chargeItemRepository: CollectChargeItemRepository,
    private val receiptRepository: CollectReceiptRepository,
    private val taxLineRepository : CollectTaxLineRepository,
    private val contractRepository: CollectContractRepository,
    private val depositRepository: CollectDepositRepository,
    private val bulkInserter : BulkDistinctInserter
) : ChargeCollectable(xAbcSdkApikey, sortProperty, pageSize, omsClient){

    companion object {
        private val logger = KotlinLogging.logger {}
        data class Builder(
            var charges : MutableList<CollectCharge> = mutableListOf(),
            var chargeItems : MutableList<CollectChargeItem> = mutableListOf(),
            var receipts  :MutableList<CollectReceipt> = mutableListOf(),
            var taxLines : MutableList<CollectTaxLine> = mutableListOf()
        ) {
            fun separateChargeItems(collectCharges : MutableList<CollectCharge>) : MutableList<CollectChargeItem> {
                val collects = mutableListOf<CollectChargeItem>()
                collectCharges.forEach { charge ->
                    collects.addAll(charge.chargeItems)
                }
                return collects
            }
            fun separateReceipts(collectCharges : MutableList<CollectCharge>) : MutableList<CollectReceipt> {
                val collects = mutableListOf<CollectReceipt>()
                collectCharges.forEach { charge ->
                    charge.receipt?.let { collects.add(it) }
                }
                return collects
            }
            fun separateTaxLines(receipts : MutableList<CollectReceipt>) : MutableList<CollectTaxLine> {
                val collects = mutableListOf<CollectTaxLine>()
                receipts.forEach { receipt ->
                    collects.addAll(receipt.taxLines)
                }
                return collects
            }
            fun execute(origin : MutableList<OmsBillingCharge>) : Builder {
                val collectCharges = ChargeBuilder.builds(OmsApiChargeMutableList(origin))
                val collectChargeItems = separateChargeItems(collectCharges)
                val collectReceipts = separateReceipts(collectCharges)
                val collectTaxLines = separateTaxLines(collectReceipts)

                charges.addAll(collectCharges)
                chargeItems.addAll(collectChargeItems)
                receipts.addAll(collectReceipts)
                taxLines.addAll(collectTaxLines)
                return this
            }
        }
    }


    fun collectCharges(contractId : String) : MutableList<OmsBillingCharge>{
        var charge = mutableListOf<OmsBillingCharge>()
        collects(GetBillingChargesChargeSearchParameter(contractId = contractId)){ omsCharges ->
            charge.addAll(omsCharges)
            true
        }
        return charge
    }

    @Transactional
    fun bulkInsert(results : Builder) {
        bulkInserter.execute(chargeRepository,results.charges)
        bulkInserter.execute(chargeItemRepository,results.chargeItems)
        bulkInserter.execute(receiptRepository,results.receipts)
        bulkInserter.execute(taxLineRepository,results.taxLines)
        logger.info {
            "COLLECT-CHARGE-BULK_INSERT[charges=${results.charges.size}]," +
                "chargeItems=${results.chargeItems.size}]," +
                "receipts=${results.receipts.size}]," +
                "taxLines=${results.taxLines.size}]"
        }
    }
    fun build(fromTime : LocalDate,toTime : LocalDate, isTest : Boolean) : Builder {
        val builder = Builder()
        contractRepository.findActiveContractsWithinCreateTimeRange(fromTime,toTime)?.let { contracts->
            contracts.forEach {contract ->
                contract.contractId?.let { id ->
                    val omsCharges = collectCharges(id)
                    bulkInsert(Builder().execute(omsCharges))
                }
            }
        }
        return builder
    }

    fun collects(trailer: AsyncEventTrailer) {
        val from = trailer.queries().get("fromDateTime") as LocalDateTime
        val to = trailer.queries().get("toDateTime") as LocalDateTime
        val timezone = trailer.queries().get("timezone") as TimeZoneCode
        logger.info { "COLLECT-START[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }
        val builder = build(from.toUTCLocalDate(),to.toUTCLocalDate(),trailer.test())
        bulkInsert(builder)
        logger.info { "COLLECT-END[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }
    }
}