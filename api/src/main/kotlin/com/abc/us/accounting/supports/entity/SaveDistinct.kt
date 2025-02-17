package com.abc.us.accounting.supports.entity

import com.abc.us.accounting.config.Constants
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

open class SaveDistinct <T : Any> (
    private val repository: JpaRepository<T, *>,
    private val hashCodeProperty: String = "hashCode",
    private val findMethodName : String = "findByHashCodeIn"
) {
    private fun getProperty(entity: T, propertyName: String): Any? {
        return entity::class.memberProperties.find { it.name == propertyName }
            ?.apply { isAccessible = true }
            ?.getter
            ?.call(entity)
    }
    private fun extractHashCodes(entities: Iterable<T>): List<String> {
        return entities.mapNotNull { getProperty(it, hashCodeProperty)?.toString() }
    }
    fun findByHashCodes(hashCodes: List<String>): List<T> {
        val findByHashCodeInMethod = repository::class.functions.find { it.name == findMethodName }
        @Suppress("UNCHECKED_CAST")
        return findByHashCodeInMethod?.call(repository, hashCodes.toList()) as? List<T> ?: emptyList()
    }
    private fun distinctify(entities: MutableIterable<T>) : List<T> {
        // entities 내에서 중복된 해시값 존재시 제거
        return entities.associateBy { entity ->
            getProperty(entity, hashCodeProperty)?.toString() ?: throw IllegalArgumentException("HashCode is missing")
        }.values.toMutableList()
    }
    private fun setProperty(entity: T, propertyName: String, value: Any?) {
        val property = entity::class.memberProperties.find { it.name == propertyName } as? KMutableProperty1<T, Any?>
        property?.apply { isAccessible = true }?.setter?.call(entity, value)
    }
    private fun applyHashCodeIfMissing(entity: T) {
        val currentHashCode = getProperty(entity, hashCodeProperty)?.toString()
        if (currentHashCode.isNullOrEmpty()) {
            val hashCode = entity.toEntityHash()
            setProperty(entity, hashCodeProperty, hashCode)
        }
    }

    @Transactional
    open fun execute(entities: MutableIterable<T>): MutableList<T> {
        val newEntities = entities.filterNotNull().onEach { applyHashCodeIfMissing(it) }
        val deduceEntity = distinctify(newEntities.toMutableList())

        val hashCodes = extractHashCodes(deduceEntity)
        val existingEntities = findByHashCodes(hashCodes)
        val existingEntitiesMap = existingEntities.associateBy { getProperty(it, hashCodeProperty)?.toString() }

        val entitiesToSave = mutableListOf<T>()

        deduceEntity.forEach{ entity ->
            val hashCodeValue = getProperty(entity, hashCodeProperty)?.toString()
            val existingEntity = existingEntitiesMap[hashCodeValue]
            if(existingEntity == null)
                entitiesToSave.add(entity)
        }
        return repository.saveAll(entitiesToSave)
    }
}

@Repository
class BulkDistinctInserter( private val entityManager: EntityManager ){

    class Distinct <T : Any> (
        private val repository: JpaRepository<T, *>,
        private val hashCodeProperty: String = "hashCode",
        private val findMethodName : String = "findByHashCodeIn"
    ) {
        private fun getProperty(entity: T, propertyName: String): Any? {
            return entity::class.memberProperties.find { it.name == propertyName }
                ?.apply { isAccessible = true }
                ?.getter
                ?.call(entity)
        }
        private fun extractHashCodes(entities: Iterable<T>): List<String> {
            return entities.mapNotNull { getProperty(it, hashCodeProperty)?.toString() }
        }
        fun findByHashCodes(hashCodes: List<String>): List<T> {
            val findByHashCodeInMethod = repository::class.functions.find { it.name == findMethodName }
            @Suppress("UNCHECKED_CAST")
            return findByHashCodeInMethod?.call(repository, hashCodes.toList()) as? List<T> ?: emptyList()
        }
        private fun distinctify(entities: MutableIterable<T>) : List<T> {
            // entities 내에서 중복된 해시값 존재시 제거
            return entities.associateBy { entity ->
                getProperty(entity, hashCodeProperty)?.toString() ?: throw IllegalArgumentException("HashCode is missing")
            }.values.toMutableList()
        }
        private fun setProperty(entity: T, propertyName: String, value: Any?) {
            val property = entity::class.memberProperties.find { it.name == propertyName } as? KMutableProperty1<T, Any?>
            property?.apply { isAccessible = true }?.setter?.call(entity, value)
        }
        private fun applyHashCodeIfMissing(entity: T) {
            val currentHashCode = getProperty(entity, hashCodeProperty)?.toString()
            if (currentHashCode.isNullOrEmpty()) {
                val hashCode = entity.toEntityHash()
                setProperty(entity, hashCodeProperty, hashCode)
            }
        }
        fun separate(entities: MutableIterable<T>) : MutableList<T> {
            val newEntities = entities.filterNotNull().onEach { applyHashCodeIfMissing(it) }
            val deduceEntity = distinctify(newEntities.toMutableList())

            val hashCodes = extractHashCodes(deduceEntity)
            val existingEntities = findByHashCodes(hashCodes)
            val existingEntitiesMap = existingEntities.associateBy { getProperty(it, hashCodeProperty)?.toString() }

            val entitiesToSave = mutableListOf<T>()

            deduceEntity.forEach{ entity ->
                val hashCodeValue = getProperty(entity, hashCodeProperty)?.toString()
                val existingEntity = existingEntitiesMap[hashCodeValue]
                if(existingEntity == null)
                    entitiesToSave.add(entity)
            }
            return entitiesToSave
        }
    }

    private fun clearContext() {
        entityManager.flush()
        entityManager.clear()
    }

    fun <T:Any> bulkInsert(entities: Collection<T>): MutableList<T> {

        if (entities.isEmpty())
            return mutableListOf()

        val inserted = entities.chunked(Constants.JPA_BATCH_SIZE).flatMap { chunk ->
            chunk.map { entity ->
                entityManager.persist(entity)
                entity
            }
        }.toMutableList()
        clearContext()
        return inserted
    }

    @Transactional
    fun <T : Any> execute(repository: JpaRepository<T, *>,
                          entities: MutableIterable<T>): MutableList<T> {
        val distinctEntities = Distinct(repository).separate(entities)
        return bulkInsert(distinctEntities)
    }
}