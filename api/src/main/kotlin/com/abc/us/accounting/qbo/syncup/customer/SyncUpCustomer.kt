package com.abc.us.accounting.qbo.syncup.customer

import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableName
import com.abc.us.accounting.commons.domain.type.TimeZoneCode
import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.qbo.domain.entity.QboCustomer
import com.abc.us.accounting.qbo.domain.entity.key.QboCustomerKey
import com.abc.us.accounting.qbo.domain.repository.QboCustomerRepository
import com.abc.us.accounting.qbo.interact.QBOCertifier
import com.abc.us.accounting.qbo.interact.QBOService
import com.abc.us.accounting.qbo.service.QboCustomerService

import com.abc.us.accounting.supports.converter.JsonConverter
import com.abc.us.accounting.iface.domain.entity.oms.IfCustomer
import com.abc.us.accounting.iface.domain.repository.oms.IfCustomerRepository
import com.abc.us.accounting.iface.domain.type.oms.IfCustomerType
import com.intuit.ipp.data.Customer
import com.intuit.ipp.data.EmailAddress
import com.intuit.ipp.data.TelephoneNumber
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.OffsetDateTime

@Service
class SyncUpCustomer (
    private val qboService : QBOService,
    private val certifier : QBOCertifier,
    private val ifCustomerRepository: IfCustomerRepository,
    private val qboCustomerRepository: QboCustomerRepository,
    private val qboCustomerService : QboCustomerService
)  {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val converter = JsonConverter()
    }

    fun inquiryAlreadyExistCustomers(companyCode : String) : MutableMap<String, Customer> {

        val qboCustomer = mutableMapOf<String, Customer>()

        qboService.selectAll(companyCode, Customer::class) { customer ->
            qboCustomer[customer.displayName] = customer
        }
        return qboCustomer
    }

    fun buildSubmittedCustomer(companyCode : String,
                               customer : Customer,
                               abcCustomer : IfCustomer
    ) : QboCustomer {
        val submitJson = converter.toJson(customer)
        return QboCustomer(key = QboCustomerKey(qboId = customer.id,
                                              customerId=customer.displayName,
                                              companyCode = companyCode),
                           name = EmbeddableName().apply {
                               firstName= abcCustomer.firstName
                               lastName= abcCustomer.lastName
                           },
                           submitResult = submitJson?.let { it }?:"")
            .apply {
                customer.customerTypeRef?.let { it.type }
                customerType = abcCustomer.accountType
                customerStatus = abcCustomer.customerStatus
//                channelType = abcCustomer.channelType
                createTime = OffsetDateTime.now()
                updateTime = OffsetDateTime.now()
            }
    }
    fun buildCustomerName(customer : IfCustomer) : String {

        return "ABC.R." + customer.customerId
    }

    fun buildQboCustomer( customer : IfCustomer) : Customer {
        return Customer().apply {
            displayName = buildCustomerName(customer)
            title = customer.accountType?.let { it.name }
            notes = customer.customerStatus?.let { it.name }
//            companyName = customer.channelType?.let { it.name }
//            isActive = customer.isActive
            //taxExemptionReasonId = customer.isTaxLiable
//            isTaxable = customer.isTaxLiability


            primaryEmailAddr = EmailAddress().apply {
                address = customer.email
            }
            primaryPhone = customer.phone?.let { phoneNumber ->
                TelephoneNumber().apply {
                    freeFormNumber = phoneNumber
                }
            }
            givenName = customer.firstName
            familyName = customer.lastName

//            customer.name.let { name ->
//                primaryEmailAddr = EmailAddress().apply {
//                    address = name.primaryEmail
//                }
//
//                primaryPhone = TelephoneNumber().apply {
//                    freeFormNumber = name.primaryPhone
//                }
//                givenName = name.firstName
//                //firstName = etlvendor.givenName
//                //middleName = etlvendor.middleName
//                familyName = name.lastName
//            }
//            billAddr = PhysicalAddress().apply {
//
//            }
        }
    }
    fun addCustomers(customers : List<IfCustomer>) : MutableList<QboCustomer>{
        val addTargets = mutableMapOf<String,MutableList<Customer>>()
        val submitMap = mutableMapOf<String, IfCustomer>()
        val submittedCustomer = mutableListOf<QboCustomer>()

        certifier.visit { credential ->
            customers.forEach { abcCustomer ->
                var submitCustomer = addTargets[credential.companyCode]
                if(submitCustomer == null) {
                    submitCustomer = mutableListOf()
                    addTargets[credential.companyCode] = submitCustomer
                }
                val qboCustomer = buildQboCustomer(abcCustomer)
                submitCustomer.add(qboCustomer)
                submitMap[qboCustomer.displayName] = abcCustomer
            }
            true
        }
        addTargets.forEach{ (companyCode, customers) ->
            try {
                val results = qboService.batchAdd(companyCode, Customer::class.java,customers)
                val submitteds = mutableListOf<QboCustomer>()
                results?.let {
                    results.forEach { result ->
                        val abcCustomer = submitMap[result.displayName]!!
                        val submitted = buildSubmittedCustomer(companyCode, result,abcCustomer)
                        submitteds.add(submitted)
                        logger.info("Add QboCustomer-[${result.id}.${result.displayName}]")
                    }
                }
                submittedCustomer.addAll(submitteds)
            }
            catch (e: Exception) {
                logger.error { "Failure Add QboCustomer [${e.message}]" }
            }
        }
        return submittedCustomer
    }
    fun remainingCustomers(from : OffsetDateTime , to : OffsetDateTime) : MutableList<IfCustomer>? {
        //1. create time 기준으로 customer 검색
        val findCustomers =ifCustomerRepository.findActiveCustomerWithinCreateTimeRange(from,to)

        //2. customerId 만 분리
        val customerIdSet = findCustomers?.mapNotNull { it.customerId }?.toMutableSet() ?: mutableSetOf()

        //3. quickbook 에 제출된 customerId 리스트 분리
        val submittedCustomers = qboCustomerRepository.findCustomerWithinCustomerId(customerIdSet)

        //4. submit 완료된 customerId 분리
        val submittedCustomerIds = submittedCustomers?.mapNotNull { it.key.customerId }?.toMutableSet()?: mutableSetOf()

        //5. submitted customer 를 제외한 나머지 customer 정보 반환
        return findCustomers?.filterNot { it.customerId in submittedCustomerIds }?.toMutableList()
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun bulkInsert(qboCustomers : MutableList<QboCustomer>) {
        qboCustomerRepository.saveAll(qboCustomers)
    }
    fun submit(trailer: AsyncEventTrailer) {
        val from = trailer.queries().get("startDateTime") as LocalDateTime
        val to = trailer.queries().get("endDateTime") as LocalDateTime
        val timezone = trailer.queries().get("timezone") as TimeZoneCode
        val reversing = trailer.reversing()

        logger.info { "QBO-START[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }
        val corporateCustomers =ifCustomerRepository.findAllByAccountType(IfCustomerType.CORPORATE)

        val customerMap: Map<String, MutableSet<String>> = corporateCustomers
            .filter { it.userId != null } // userId가 null이 아닌 경우만 포함
            .groupBy { it.userId!! } // userId를 Key로 그룹핑
            .mapValues { entry -> entry.value.map { it.customerId }.toMutableSet() }

        customerMap.forEach { companyCode,customerIdSet ->
            qboCustomerService.raise(companyCode,customerIdSet)
        }



//        if(reversing ) {
////            certifier.visit { credential ->
////                val submittedCustomers = inquiryAlreadyExistCustomers(credential.companyCode)
////                submittedCustomers.forEach{customer ->
////                    val submitted = buildSubmittedCustomer(credential.companyCode, customer,abcCustomer)
////                }
////                true
////            }
//        }
//        else {
//            val remainingCustomers = remainingCustomers(
//                timezone.convertTime(from,TimeZoneCode.UTC),
//                timezone.convertTime(to,TimeZoneCode.UTC)
//            )
//            remainingCustomers?.let { customers ->
//                logger.info { "QBO-CUSTOMERS[${customers.size}])" }
//
//                customers.chunked(QBOService.BATCH_SIZE).forEach { chunk ->
//                    bulkInsert(addCustomers(chunk))
//                }
//            }
//        }


        logger.info { "QBO-END[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }
    }
}