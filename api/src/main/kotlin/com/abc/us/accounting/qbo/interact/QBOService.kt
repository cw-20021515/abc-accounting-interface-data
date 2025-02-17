package com.abc.us.accounting.qbo.interact

import com.abc.us.accounting.qbo.domain.entity.QboCredential
import com.abc.us.accounting.supports.converter.JsonConverter
import com.intuit.ipp.core.IEntity
import com.intuit.ipp.data.OperationEnum
import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import kotlin.reflect.KClass

@Service
class QBOService(private val certifier : QBOCertifier) {

    companion object {
        private val logger = KotlinLogging.logger {}
        private val converter = JsonConverter()
        val BATCH_SIZE = 30
    }

    private var communicator: Communicator? = null

    init {
        communicator = Communicator { credential, e ->
            var newCredential: QboCredential? = null
            e.errorList.forEach { item ->
                val msg = item.message
                if(msg.contains("401") == true || msg.contains("403"))
                    newCredential = certifier.refreshToken(credential)
            }
            newCredential // Boolean 값을 반환
        }
    }

    fun getCredential(companyCode : String) : QboCredential? {
        try {
            return certifier.getCredential(QBOCertifier.ByCompanyCode(companyCode))
        }
        catch (e : Exception) {
            logger.error { "Failure add(${companyCode}-${e.message}" }
        }
        return null
    }

    fun executeQuery(companyCode : String,query : String )  : List<Any>?  {
        return getCredential(companyCode)?.let {credential ->
            communicator!!.executeQuery(credential,query)
        }
    }

    private val restTemplate = RestTemplate()

    fun getLocations(companyCode : String) : Any? {
        return getCredential(companyCode)?.let { credential ->

            val url = certifier.baseUrl!! + "/${credential.realmId}/reports/ProfitAndLoss?minorversion=74"

            val headers = HttpHeaders().apply {
                set("Authorization", "Bearer ${credential.accessToken}")
                set("Accept", "application/json")
            }
            val entity = org.springframework.http.HttpEntity<String>(headers)

            val response: ResponseEntity<String> = restTemplate.exchange(url, HttpMethod.GET, entity, String::class.java)

            return if (response.statusCode.is2xxSuccessful) {
//                converter.toObj()
//                val qboResponse = objectMapper.readValue<QboLocationResponse>(response.body!!)
//                qboResponse.locations
            } else {
                null
            }
        }
    }
    fun <T : IEntity> batchAdd(companyCode : String,
                               entityClass: Class<T>,
                               entitiesLambda: List<T>): List<T>? {
        return getCredential(companyCode)?.let {credential ->
            communicator!!.executeBatch(credential, OperationEnum.CREATE, entityClass, entitiesLambda)
        }
    }
    fun <T : IEntity> batchUpdate(companyCode : String,
                                  entityClass: Class<T>,
                                  entitiesLambda: List<T>): List<T>? {
        return getCredential(companyCode)?.let {credential ->
            communicator!!.executeBatch(credential, OperationEnum.UPDATE,entityClass, entitiesLambda)
        }
    }
    fun <T : IEntity> batchDelete(companyCode : String,
                                  entityClass: Class<T>,
                                  entitiesLambda: List<T>): List<T>? {
        return getCredential(companyCode)?.let {credential ->
            communicator!!.executeBatch(credential, OperationEnum.DELETE,entityClass, entitiesLambda)
        }
    }

    fun <T : IEntity> add(companyCode : String,entity : T) : T? {
        return getCredential(companyCode)?.let {credential ->
            communicator!!.add(credential, entity)
        }
    }

    fun <T : IEntity> update(companyCode : String,entity : T) : T? {
        return getCredential(companyCode)?.let { credential ->
            return communicator!!.update(credential, entity)
        }
    }

    fun <T : IEntity> select(companyCode : String,entities : List<T>,block : (T)->Unit) {
        getCredential(companyCode)?.let { credential ->
            val responses = communicator!!.select(credential, entities)
            responses.forEach { account -> block(account) }
        }
    }
    fun <T : IEntity> selectsIn(companyCode : String,
                                key : String,
                                entity : T,
                                items : List<String>,
                               block : (T)->Unit) {
        getCredential(companyCode)?.let { credential ->
            val responses = communicator!!.selectsIn(credential, key,entity,items)
            responses?.let { it.forEach { account -> block(account) } }
        }
    }

    fun <T : IEntity> select(companyCode : String,
                             parameters : Map<String,Any>,
                             entity : T,
                             block : (T)->Unit) {

        getCredential(companyCode)?.let { credential ->
            val responses = communicator!!.select(credential, parameters,entity)
            responses?.forEach { response -> block(response) }
        }
    }

    fun <T : IEntity> find(companyCode : String, entity : T) : T? {
        return getCredential(companyCode)?.let { credential ->
            communicator!!.find(credential, entity)
        }
    }

    fun <T : IEntity> selectAll(companyCode : String,entityClass: KClass<T>, block : (T)->Unit) {

        getCredential(companyCode)?.let { credential ->
            val entityInstance: T = entityClass.constructors.first().call()

            var start = 1
            var max = 100
            do {
                val queryBuilder = QueryBuilder(entityInstance)
                    .startPosition(start)
                    .maxResults(max)

                val responses = communicator!!.select(credential, queryBuilder)

                responses?.let {
                    it.forEach { result -> block(result) }
                    start += max
                }

            } while (responses?.isNotEmpty() == true)
        }
    }
}