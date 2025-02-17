package com.abc.us.accounting.collects.helper.builder

import com.abc.us.accounting.collects.domain.entity.collect.CollectCustomer
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableName
import com.abc.us.accounting.collects.domain.type.CustomerStatusEnum
import com.abc.us.accounting.collects.domain.type.CustomerTypeEnum
import com.abc.us.accounting.collects.helper.*
import com.abc.us.accounting.supports.converter.toOffset
import mu.KotlinLogging
import java.time.ZoneOffset
import java.util.*

class CustomerBuilder{

    companion object {
        private val logger = KotlinLogging.logger {}
        fun convertStatus(status: String): CustomerStatusEnum {
            return when (status) {
                "ACTIVE" -> CustomerStatusEnum.ACTIVE
                "BANKRUPT" -> CustomerStatusEnum.BANKRUPT
                "DECEASED" -> CustomerStatusEnum.DECEASED
                else -> CustomerStatusEnum.NONE
            }
        }
        fun convertStatus(status: OmsApiCustomerStatus): CustomerStatusEnum {
            return when (status) {
                OmsApiCustomerStatus.ACTIVE -> CustomerStatusEnum.ACTIVE
                OmsApiCustomerStatus.BANKRUPT -> CustomerStatusEnum.BANKRUPT
                OmsApiCustomerStatus.DECEASED -> CustomerStatusEnum.DECEASED
                else -> CustomerStatusEnum.NONE
            }
        }

        fun convertType(type: String): CustomerTypeEnum {
            return when (type) {
                "CORPORATE" -> CustomerTypeEnum.CORPORATE
                "INDIVIDUAL" -> CustomerTypeEnum.INDIVIDUAL
                "STAFF" -> CustomerTypeEnum.STAFF
                "ACADEMY" -> CustomerTypeEnum.ACADEMY
                "OTHERS" -> CustomerTypeEnum.OTHERS
                else -> CustomerTypeEnum.NONE
            }
        }
        fun convertType(type : OmsApiCustomerType) : CustomerTypeEnum {
            return when (type) {
                OmsApiCustomerType.CORPORATE -> CustomerTypeEnum.CORPORATE
                OmsApiCustomerType.INDIVIDUAL -> CustomerTypeEnum.INDIVIDUAL
                OmsApiCustomerType.STAFF -> CustomerTypeEnum.STAFF
                OmsApiCustomerType.ACADEMY -> CustomerTypeEnum.ACADEMY
                OmsApiCustomerType.OTHERS -> CustomerTypeEnum.OTHERS
                else -> CustomerTypeEnum.NONE
            }
        }
        fun build(customer: OmsApiCustomer) : CollectCustomer {
            logger.info { "BUILD-OMS_API_CUSTOMER[${customer}]" }
            return CollectCustomer(customerId = customer.customerId,
                                   name = EmbeddableName().apply {
                                       firstName = customer.firstName
                                       lastName = customer.lastName
                                       primaryEmail = customer.email
                                       primaryPhone = customer.phone
                                       userId = customer.userId
                                   }).apply {
                channelCustomerId = customer.channelCustomerId
                customerStatus = customer.customerStatus?.let { convertStatus(it) }
                customerType = customer.accountType?.let{convertType(it) }
                currency = Currency.getInstance("USD")
                referrerCode = customer.referrerCode
                createTime = customer.createTime
                updateTime = customer.updateTime
            }
        }
        fun build(customer: OmsEntityCustomer): CollectCustomer {
            //logger.info { "BUILD-OMS_ENTITY_CUSTOMER[${customer}]" }
            return CollectCustomer(customerId = customer.id,
                                   name = EmbeddableName().apply {
                                       firstName = customer.firstName
                                       lastName = customer.lastName
                                       primaryEmail = customer.email
                                       primaryPhone = customer.phone
                                       userId = customer.userId
                                   }).apply {
                channelCustomerId = customer.channelCustomerId
                customerStatus = customer.customerStatus.let { convertStatus(it) }
                customerType = customer.accountType.let { convertType(it) }
                currency = Currency.getInstance("USD")
                referrerCode = customer.referrerCode
                updateTime = customer.updateTime?.toOffset()
                createTime = customer.createTime?.toOffset()
            }
        }
        fun build(customers: OmsEntityCustomerMutableList): MutableList<CollectCustomer> {
            val collectedCustomers = mutableListOf<CollectCustomer>()
            customers.forEach { customer ->
                collectedCustomers.add(build(customer))
            }
            return collectedCustomers
        }
    }
}