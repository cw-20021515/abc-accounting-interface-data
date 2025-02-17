package com.abc.us.accounting.qbo.helper.builder

import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableName
import com.abc.us.accounting.qbo.domain.entity.QboCustomer
import com.abc.us.accounting.qbo.domain.entity.key.QboCustomerKey
import com.abc.us.accounting.qbo.interact.QBOCertifier
import com.abc.us.accounting.supports.converter.JsonConverter
import com.abc.us.accounting.iface.domain.entity.oms.IfCustomer
import com.abc.us.accounting.iface.domain.type.oms.IfCustomerType
import com.intuit.ipp.data.Customer
import com.intuit.ipp.data.EmailAddress
import com.intuit.ipp.data.TelephoneNumber
import mu.KotlinLogging

class CustomerBuilder(
    private val certifier : QBOCertifier
) {
    companion object {
        private val converter = JsonConverter()
        private val logger = KotlinLogging.logger {}

        fun buildSubmit(companyCode : String,
                        customer : Customer,
                        ifCustomer : IfCustomer
        ) : QboCustomer {
            val submitJson = converter.toJson(customer)
            return QboCustomer(key = QboCustomerKey(qboId = customer.id,
                customerId= ifCustomer.customerId,
                companyCode = companyCode),
                name = EmbeddableName().apply {
                    firstName = ifCustomer.firstName
                    lastName = ifCustomer.lastName
                },
                submitResult = submitJson?.let { it }?:"")
                .apply {
                    customer.customerTypeRef?.let { it.type }
                    customerType = ifCustomer.accountType
                    customerStatus = ifCustomer.customerStatus
//                    channelType = ifCustomer.channelType
                    createTime = ifCustomer.createTime
                    updateTime = ifCustomer.updateTime
                }
        }
        fun buildCustomerName(customer : IfCustomer) : String{
            val builder = StringBuilder()
            if(customer.accountType == IfCustomerType.CORPORATE) {
                builder.append(customer.lastName)
            }
            else {
                builder.append("ABC.R.")
                builder.append(customer.customerId)
            }


            return builder.toString()
        }
    }
    fun build(companyCode: String, customer : IfCustomer) : Customer {
        val qboCompanyName = certifier.getCompany(QBOCertifier.ByCompanyCode(companyCode))?.let { it.name }
        return Customer().apply {
            displayName = buildCustomerName(customer)
            title = customer.accountType?.let { it.name }
            notes = customer.customerStatus?.let { it.name }
            companyName = qboCompanyName
//            isActive = customer.isActive
            //taxExemptionReasonId = customer.isTaxLiable
//            isTaxable = customer.isTaxLiability

            primaryEmailAddr = EmailAddress().apply { address = customer.email }
            primaryPhone = customer.phone?.let {
                TelephoneNumber().apply {
                    freeFormNumber = customer.phone
                }
            }
            givenName = customer.firstName
            familyName = customer.lastName            //notes = customer.
//            billAddr = PhysicalAddress().apply {
//
//            }
        }
    }
}