package com.abc.us.accounting.collects.helper.builder

import com.abc.us.accounting.collects.domain.entity.collect.CollectContract
import com.abc.us.accounting.collects.domain.entity.collect.CollectOrderItem
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableRelation
import com.abc.us.accounting.collects.helper.*
import com.abc.us.accounting.supports.converter.toOffset
import com.abc.us.generated.models.Contract
import com.abc.us.generated.models.DurationInMonths
import com.intuit.ipp.util.StringUtils
import liquibase.util.StringUtil
import mu.KotlinLogging
import java.time.ZoneOffset

class ContractBuilder {
    companion object {
        private val logger = KotlinLogging.logger {}

        fun build(orderItem : OmsEntityOrderItem,contract : OmsEntityContract) : CollectContract {
            if(!StringUtils.hasText(contract.customerId ) ) {
                logger.warn { "Contract[${contract.id}] customer not found from OmsEntityContract" }
            }

            return CollectContract(contractId = contract.id).apply {
                relation= EmbeddableRelation().apply {
                    entity = CollectOrderItem::class.java.simpleName
                    field = "order_item_id"
                    value = contract.orderItemId
                }
                channelContractId = contract.channelContractId
                channelOrderItemId = orderItem.channelOrderItemId
                isSigned = contract.isSigned
                signedTime = contract.signedTime?.toOffset()
                formId = contract.formId
                revision = contract.revision
                rentalCode = contract.rentalCode
                orderItemId = orderItem.id
                orderId = orderItem.orderId
                materialId = orderItem.materialId
                startDate = contract.startDate
                endDate = contract.endDate
                durationInMonths = contract.durationInMonths
                contractStatus = contract.contractStatusCode
                createTime = contract.createTime?.toOffset()
                updateTime = contract.updateTime?.toOffset()
                customer = contract.customer?.let { CustomerBuilder.build( it) }
                customerId = contract.customerId
                //charges = ChargeBuilder.builds(OmsEntityChargeMutableList(contract.charges.toMutableList()))
            }
        }
//        fun build(orderItem : OmsOrderItem, omsContract : Contract) : CollectContract {
//            logger.info { "BUILD-ORDER-ITEM_CONTRACT[${omsContract}]" }
//            return CollectContract(contractId = omsContract.contractId).apply {
//                relation= EmbeddableRelation().apply {
//                    entity = CollectOrderItem::class.java.simpleName
//                    field = "order_item_id"
//                    value = omsContract.orderItemId
//                }
//                channelContractId = omsContract.channelContractId
//                channelOrderItemId = orderItem.channelOrderItemId
//                isSigned = omsContract.isSigned
//                signedTime = omsContract.signedTime
//                formId = omsContract.formId
//                revision = omsContract.revision
//                rentalCode = omsContract.rentalCode
//                customerId =  orderItem.customerInformation.customerId
//                orderItemId = orderItem.orderItemId
//                orderId = orderItem.orderId
//                materialId = orderItem.material.materialId
//                startDate = omsContract.startDate
//                endDate = omsContract.endDate
//                durationInMonths = when (omsContract.durationInMonths) {
//                    DurationInMonths.MONTHS_24 -> 24
//                    DurationInMonths.MONTHS_36 -> 36
//                    DurationInMonths.MONTHS_48 -> 48
//                    DurationInMonths.MONTHS_60 -> 60
//                    DurationInMonths.MONTHS_84 -> 84
//                    DurationInMonths.NO_CONTRACT -> 0
//                }
//                contractStatus = omsContract.contractStatus.name
//                createTime = omsContract.createTime
//                updateTime = omsContract.updateTime
//            }
//        }
        fun build(omsOrderItem : OmsApiOrderItem,omsContract : Contract) : CollectContract {
            //logger.info { "BUILD-ORDER-ITEM_CONTRACT[${omsContract}]" }
            if(!StringUtils.hasText(omsContract.customerId ) ) {
                logger.warn { "Contract[${omsContract.contractId}] customer not found from OmsContract" }
            }


            return CollectContract(contractId = omsContract.contractId).apply {
                relation= EmbeddableRelation().apply {
                    entity = CollectOrderItem::class.java.simpleName
                    field = "order_item_id"
                    value = omsContract.orderItemId
                }
                channelContractId = omsContract.channelContractId
                channelOrderItemId = omsOrderItem.channelOrderItemId
                isSigned = omsContract.isSigned
                signedTime = omsContract.signedTime
                formId = omsContract.formId
                revision = omsContract.revision
                rentalCode = omsContract.rentalCode
                customerId =  omsContract.customerId
                orderItemId = omsOrderItem.orderItemId
                orderId = omsOrderItem.orderId
                materialId = omsOrderItem.material.materialId
                startDate = omsContract.startDate
                endDate = omsContract.endDate
                durationInMonths = when (omsContract.durationInMonths) {
                        DurationInMonths.MONTHS_24 -> 24
                        DurationInMonths.MONTHS_36 -> 36
                        DurationInMonths.MONTHS_48 -> 48
                        DurationInMonths.MONTHS_60 -> 60
                        DurationInMonths.MONTHS_84 -> 84
                        DurationInMonths.NO_CONTRACT -> 0
                    }
                contractStatus = omsContract.contractStatus.name
                createTime = omsContract.createTime
                updateTime = omsContract.updateTime
            }
        }
        fun build(view: OmsApiContract): CollectContract {
            //logger.info { "BUILD-OMS_API_CONTRACT[${view}]" }



            val customerIdFromInfo = view.customerInformation?.customerId
            if(!StringUtils.hasText(customerIdFromInfo ) ) {
                logger.warn { "Contract[${view.contractId}] customer not found from OmsApiContract" }
            }

            return CollectContract(contractId = view.contractId).apply {
                relation= EmbeddableRelation().apply {
                    entity = CollectOrderItem::class.java.simpleName
                    field = "order_item_id"
                    value = view.orderItem?.let { it.orderItemId }
                }
                channelContractId = view.channelContractId
                channelOrderItemId = view.orderItem?.let { it.channelOrderItemId }
                isSigned = view.isSigned
                signedTime = view.signedTime
                formId = view.formId
                revision = view.revision
                rentalCode = view.rentalCode
                customerId =  customerIdFromInfo
                orderItemId = view.orderItem?.let { it.orderItemId }
                orderId = view.orderItem?.let { it.orderId }
                materialId = view.orderItem?.let { it.material.materialId }
                startDate = view.startDate
                endDate = view.endDate
                durationInMonths =when (view.durationInMonths) {
                    DurationInMonths.MONTHS_24 -> 24
                    DurationInMonths.MONTHS_36 -> 36
                    DurationInMonths.MONTHS_48 -> 48
                    DurationInMonths.MONTHS_60 -> 60
                    DurationInMonths.MONTHS_84 -> 84
                    DurationInMonths.NO_CONTRACT -> 0
                }
                contractStatus = view.contractStatus.name
                // 없어서 입력 못함
//                createTime = view.createTime
//                updateTime = view.updateTime
            }
        }

        fun builds(orderItems: OmsApiOrderItemMutableList)  :MutableList<CollectContract> {
            val contracts = mutableListOf<CollectContract>()
            orderItems.forEach { orderItem ->
                orderItem.contract?.let {
                    val contract = build(orderItem,it)
                    contracts.add(contract)
                }
            }
            return contracts
        }

        fun builds(contractViews : OmsApiContractMutableList) :MutableList<CollectContract> {
            val contracts = mutableListOf<CollectContract>()
            contractViews.forEach {contractView ->
                contracts.add(build(contractView))
            }
            return contracts
        }
    }
}