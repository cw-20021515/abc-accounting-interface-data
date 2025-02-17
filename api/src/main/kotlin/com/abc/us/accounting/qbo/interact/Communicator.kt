package com.abc.us.accounting.qbo.interact

import com.abc.us.accounting.qbo.domain.entity.QboCredential
import com.abc.us.accounting.supports.converter.JsonConverter
import com.intuit.ipp.core.Context
import com.intuit.ipp.core.IEntity
import com.intuit.ipp.core.ServiceType
import com.intuit.ipp.data.OperationEnum
import com.intuit.ipp.exception.AuthenticationException
import com.intuit.ipp.exception.FMSException
import com.intuit.ipp.exception.ServiceException
import com.intuit.ipp.security.OAuth2Authorizer
import com.intuit.ipp.services.BatchOperation
import com.intuit.ipp.services.DataService
import mu.KotlinLogging
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class Communicator(
    private var refreshToken: (QboCredential?, FMSException) -> QboCredential?
) {

    companion object {
        private val logger = KotlinLogging.logger {}
        private val converter = JsonConverter()
    }
    protected fun <T : IEntity> makeSelectQuery(entity : T,parameters : Map<String,Any>) : String {

        var baseQuery = "SELECT * FROM " + entity.javaClass.simpleName;
        // 조건이 있는 경우 WHERE 절 추가
        if (parameters.isNotEmpty()) {
            val whereClause = parameters.entries.joinToString(" AND ") { (key, value) ->
                val formattedValue = when (value) {
                    is String -> "'$value'" // 문자열 값은 단일 따옴표로 감쌈
                    is Number, is Boolean -> value.toString() // 숫자 및 불리언 값은 그대로 사용
                    else -> throw IllegalArgumentException("Unsupported parameter type: ${value::class}")
                }
                "$key = $formattedValue"
            }
            return "$baseQuery WHERE $whereClause"
        }

        return baseQuery
    }

    private fun makeDataService(credential: QboCredential?) : DataService{
        val oauth = OAuth2Authorizer(credential?.accessToken)
        val context = Context(oauth, ServiceType.QBO, credential?.realmId)
        return DataService(context)
    }
    private fun <T> executeWithAuthorize(credential: QboCredential?, block: (DataService) -> T) : T?{
        return try {
            block(makeDataService(credential))  // 첫 번째 시도
        } catch (e: ServiceException) {
            val newCredential = refreshToken(credential,e)  // 토큰 갱신
            block(makeDataService(newCredential))
        } catch (e : AuthenticationException) {
            val newCredential = refreshToken(credential,e)
            block(makeDataService(newCredential))
        }
        return null
    }

    // Reflection을 사용해 `isActive` 필드를 설정하는 함수
    private fun <T : Any, V : Any> setFieldValue(instance: T, fieldName: String, value: V) {
        try {
            // 필드 찾기
            val property = instance::class.memberProperties
                .find { it.name == fieldName }
                ?: throw NoSuchFieldException("Field '$fieldName' not found")

            property.isAccessible = true  // 접근 가능 설정

            // mutable인 경우에만 값 설정
            if (property is KMutableProperty1<*, *>) {
                (property as KMutableProperty1<T, V>).set(instance, value)
            } else {
                throw IllegalAccessException("'$fieldName' is not mutable")
            }
        } catch (e: Exception) {
            println("Error setting field '$fieldName': ${e.message}")
        }
    }

    fun <T : IEntity> add(credential: QboCredential?, entity : T) : T? {

        return executeWithAuthorize(credential){ dataService ->
            dataService.add(entity)
        }
    }
    fun <T : IEntity> executeBatch(credential: QboCredential?,
                                   operation : OperationEnum,
                                   entityClass: Class<T>,
                                   entitiesLambda:  List<T>): List<T>? {

        // 최대 30개의 엔터티만 처리하도록 제한
        if (entitiesLambda.size > 30) {
            throw IllegalArgumentException("Batch operation cannot process more than 30 entities at once.")
        }

        return executeWithAuthorize(credential) { dataService ->
            val batchOperation = BatchOperation()

            // 각 엔터티를 BatchOperation에 추가
            entitiesLambda.forEachIndexed { index, entity ->
                batchOperation.addEntity(entity, operation, "Entity$index")
            }

            // Batch 실행
            dataService.executeBatch(batchOperation)

            // 결과 처리
            val results = mutableListOf<T>()
            entitiesLambda.forEachIndexed { index, _ ->
                val response = batchOperation.getEntity("Entity$index")
                if (entityClass.isInstance(response)) {  // 런타임 타입 확인
                    @Suppress("UNCHECKED_CAST")
                    results.add(response as T)
                } else {
                    val entity = entitiesLambda.get(index)
                    val fault = batchOperation.getFault("Entity$index")
                    logger.error { "[${operation.name}] Failed to add entity [${converter.toJson(fault.error)}]-[${converter.toJson(entity)}]" }
                }
            }
            results
        }
    }
    fun executeQuery(credential: QboCredential?, query : String) : List<Any>? {

        return executeWithAuthorize(credential){dataService ->
            val result = dataService.executeQuery(query)
            (result.entities as List<Any>)
        }
    }

    fun <T : IEntity> selectsIn(credential: QboCredential?,
                                key : String,
                                entity : T,
                                items : List<String>) : List<T>? {

        var baseQuery = "SELECT * FROM " + entity.javaClass.simpleName + " WHERE ";
        val builder = StringBuilder(baseQuery)
        builder.append(key).append(" IN (")
        items.forEachIndexed { index, name ->
            builder.append("'").append(name).append("'")
            if (index < items.size - 1) {
                builder.append(", ")
            }
        }
        builder.append(")")
        val query = builder.toString()
        return executeWithAuthorize(credential){ dataService ->
            val result = dataService.executeQuery(query)
            (result.entities as List<T>)
        }
    }

    fun <T : IEntity> select(credential: QboCredential?,
                             parameters : Map<String,Any>,
                             entity : T) : List<T>? {

        val query = makeSelectQuery(entity,parameters)
        return executeWithAuthorize(credential){ dataService ->
            val result = dataService.executeQuery(query)
            (result.entities as List<T>)
        }
    }

    fun <T : IEntity> select(credential: QboCredential?,
                             queryBuilder: QueryBuilder<T>) : List<T>? {

        val query = queryBuilder.build()
        return executeWithAuthorize(credential){ dataService ->
            val result = dataService.executeQuery(query)
            (result.entities as List<T>)
        }
    }

    fun <T : IEntity> select(credential: QboCredential?,
                             entities : List<T>) : List<T> {

        val results : MutableList<T> = mutableListOf()
        entities.forEach { entity ->
            val result = find(credential,entity)
            if(result != null)
                results.add( result )
        }
        return results
    }


    fun <T : IEntity> find(credential: QboCredential?, entity : T) : T? {
        return executeWithAuthorize(credential) { dataService ->
            dataService.findById(entity)
        }
    }

    fun <T : IEntity> update(credential: QboCredential?, entity : T) : T? {

        return executeWithAuthorize(credential){ dataService ->
            find(credential,entity).let {dataService.update(entity)}
        }
    }

    fun <T : IEntity> deletes(credential: QboCredential?, entities : List<T>) : List<T> {

        val results : MutableList<T> = mutableListOf()

        entities.forEach { entity ->
            val result = find(credential,entity)
            if(result != null) {
                // QBO 에서는 삭제는 없고 inactive 기능만 존재
                setFieldValue(result, "isActive", false)
                update(credential,result).let {
                    if (it != null) { results.add( it )}
                }
            }
        }
        return results
    }
}