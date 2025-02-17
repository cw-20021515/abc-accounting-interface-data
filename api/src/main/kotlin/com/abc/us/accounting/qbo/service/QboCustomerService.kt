package com.abc.us.accounting.qbo.service

import com.abc.us.accounting.qbo.domain.entity.QboCustomer
import com.abc.us.accounting.qbo.domain.repository.QboCustomerRepository
import com.abc.us.accounting.qbo.helper.builder.CustomerBuilder
import com.abc.us.accounting.qbo.interact.QBOCertifier
import com.abc.us.accounting.qbo.interact.QBOService
import com.abc.us.accounting.supports.converter.JsonConverter
import com.abc.us.accounting.iface.domain.entity.oms.IfCustomer
import com.abc.us.accounting.iface.domain.repository.oms.IfCustomerRepository
import com.intuit.ipp.data.Customer
import mu.KotlinLogging
import okio.withLock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.locks.ReentrantLock

@Service
class QboCustomerService(
    private val qboService : QBOService,
    private val certifier : QBOCertifier,
    private val ifCustomerRepository: IfCustomerRepository,
    private val qboCustomerRepository: QboCustomerRepository
) {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val converter = JsonConverter()
    }
    private val lock = ReentrantLock()
    var customerMap : MutableMap<String,MutableMap<String,QboCustomer>> = mutableMapOf()

    fun flush(companyCode : String,customer : QboCustomer ) : QboCustomer {
        var map = customerMap[companyCode]
        val customerId = customer.key.customerId
        lock.withLock {
            if(map == null) {
                map = mutableMapOf()
                customerMap[companyCode] = map!!
            }
            map!![customerId] = customer
        }
        return customer
    }

    fun addCustomer(companyCode: String,ifCustomer : IfCustomer) : QboCustomer?{
        val submitCustomer = CustomerBuilder(certifier).build(companyCode, ifCustomer)
        var addedCustomer: QboCustomer? = null
        try {
            val result = qboService.add(companyCode, submitCustomer)
            addedCustomer = result?.let {
                val submitted = CustomerBuilder.buildSubmit(companyCode, result, ifCustomer)
                logger.info("Add QboCustomer-[${result.displayName}]-[${result.id}]")
                qboCustomerRepository.save(submitted)
                flush(companyCode, submitted)
            } ?: run {
                null
            }
        }
        catch (e: Exception) {

            qboService.select(companyCode, mutableMapOf(Pair("DisplayName",submitCustomer.displayName)),Customer()) { result ->
                val submitted = CustomerBuilder.buildSubmit(companyCode, result, ifCustomer)
                logger.info("Add QboCustomer-[${result.displayName}]-[${result.id}]")
                qboCustomerRepository.save(submitted)
                addedCustomer = flush(companyCode, submitted)
            }

            if(addedCustomer == null)
                logger.error { "Failure Add QboCustomer-[${ifCustomer.customerId}]-[${e.message}]"}
        }
        return addedCustomer
    }

    fun findByCustomer(companyCode : String,customerId : String) : QboCustomer? {

        var customers = customerMap[companyCode]

        if(customers.isNullOrEmpty()) {
            // 등록되지 않은 company 일 경우 오류
            var credential = certifier.getCredential(QBOCertifier.ByCompanyCode(companyCode))
            if(credential == null) {
                logger.error { "No QBOCustomer found for companyCode [$companyCode]" }
                return null
            }
            customers = mutableMapOf()
            customerMap[companyCode] = customers
        }

        var result : QboCustomer? = customers[customerId]
        if(result == null) {
            result = qboCustomerRepository.findByCustomerId(customerId)?.let { submitted ->
                flush(companyCode,submitted)
            }

            if(result == null) {
                result = ifCustomerRepository.findByCustomerId(customerId)?.let { collected ->
                    addCustomer(companyCode,collected)
                }?: run {
                    null
                }
            }
        }
        return result
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun bulkInsert(companyCode: String,submitted : MutableList<QboCustomer>) {
        if(submitted.isEmpty())
            return

        logger.info { "BULK-INSERT-CUSTOMER[${companyCode}.${submitted.size}])" }
        qboCustomerRepository.saveAll(submitted)
    }

    fun bulkAdd(companyCode: String,customerMap : MutableMap<String, IfCustomer>) : MutableList<QboCustomer> {
        val bulkSaves = mutableListOf<QboCustomer>()

        if(customerMap.isEmpty())
            return bulkSaves

        try {
            val chunkMap = customerMap.entries.chunked(QBOService.BATCH_SIZE).map { chunk ->
                chunk.associate { it.key to it.value }.toMutableMap()
            }
            chunkMap.forEach { chunk ->
                val customerMap = mutableMapOf<String,Customer>()
                val ifCustomerMap = mutableMapOf<String, IfCustomer> ()
                chunk.forEach { (key, collected) ->
                    val customer = CustomerBuilder(certifier).build(companyCode, collected)
                    customerMap[customer.displayName] = customer
                    ifCustomerMap[customer.displayName] = collected
                }

                qboService.selectsIn(companyCode,"DisplayName",Customer(),customerMap.keys.toMutableList()) { result ->
                    // 퀵북에 확인 진행 후 이미 등록되어 있다면 batchAdd 에서 제외함
                    ifCustomerMap[result.displayName]?.let { ifCustomer ->
                        val submitted = CustomerBuilder.buildSubmit(companyCode, result, ifCustomer)
                        flush(companyCode,submitted)
                        ifCustomerMap.remove(result.displayName)
                        customerMap.remove(result.displayName)
                    }
                }
                if(customerMap.isNotEmpty()) {
                    val results =
                        qboService.batchAdd(companyCode, Customer::class.java, customerMap.values.toMutableList())
                    results?.let {
                        results.forEach { result ->
                            val ifCustomer = ifCustomerMap[result.displayName]
                            val submitted = CustomerBuilder.buildSubmit(companyCode, result, ifCustomer!!)
                            bulkSaves.add(submitted)
                            flush(companyCode, submitted)
                            logger.info("Add QboCustomer-[${result.displayName}]-[${result.id}]")
                        }
                    }
                }
            }
        }
        catch (e: Exception) {
            // 이쪽으로 들어오면 좀 난감한데 ㅠㅠ
            logger.error { "Failure bulkAdd QboCustomer-[${e.message}]"}
        }
        return bulkSaves
    }
    fun raise(companyCode : String,customerIdSet : MutableSet<String>) : MutableList<QboCustomer> {
        if(customerIdSet.isEmpty())
            return mutableListOf()
        // 이미 퀵북에 등록되어 있는지 여부 확인
        val qboCustomers = qboCustomerRepository.findCustomerWithinCustomerId(customerIdSet)
        var idSet = customerIdSet.toMutableSet()
        qboCustomers?.let { customers ->
            customers.forEach {customer ->
                flush(companyCode,customer)
                idSet.remove(customer.key.customerId)
            }
        }

        // 퀵북에 등록되지 않고 남은 customer 는 퀵북 등록 진행
        val submitTarget = mutableMapOf<String, IfCustomer>()
        ifCustomerRepository.findActiveByCustomerIds(idSet.toMutableList()).forEach { collected ->
            //val customer = CustomerBuilder(certifier).build(companyCode, collected)
            submitTarget[collected.customerId] = collected
        }
        return bulkAdd(companyCode,submitTarget)
    }
}