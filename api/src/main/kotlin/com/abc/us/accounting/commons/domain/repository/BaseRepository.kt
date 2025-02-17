package com.abc.us.accounting.commons.domain.repository

import com.abc.us.accounting.config.Constants
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceException
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.stereotype.Repository
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.util.*

interface BulkOperationsRepository<T, ID>{
    fun bulkDeleteByIds(domainClass:Class<T>, ids: Collection<ID>):Int
    fun bulkInsert(entities: Collection<T>): List<T>
    fun bulkUpdate(entities: Collection<T>): List<T>
}

interface ReadOptimizationRepository<T, ID> {
    fun readOnlyFindAllByIds(domainClass:Class<T>, ids: Collection<ID>): List<T>
}

interface BaseRepository<T, ID>: BulkOperationsRepository<T, ID>, ReadOptimizationRepository<T, ID>


@Repository
class BaseRepositoryImpl<T, ID>(
    private val entityManager: EntityManager,
    private val transactionTemplate: TransactionTemplate
): BaseRepository<T, ID> {

    init {
        transactionTemplate.apply {
            isolationLevel = TransactionDefinition.ISOLATION_READ_COMMITTED
            propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
            timeout = TransactionDefinition.TIMEOUT_DEFAULT
        }
    }


    @Transactional(readOnly = true)
    override fun readOnlyFindAllByIds(domainClass:Class<T>, ids: Collection<ID>): List<T> {
        if (ids.isEmpty()) return emptyList()

        try {
            val tableName = getTableName(domainClass)
            val results = ids.chunked(Constants.JPA_BATCH_SIZE).flatMap { chunk ->
                val query = entityManager.createNativeQuery("""
                        SELECT * FROM ${tableName}
                        WHERE id  = ANY (:ids)
                    """, domainClass
                )
                query.setParameter("ids", convertToArray(chunk))
                    .resultList.map { it as T  }
            }

// TODO: 영향도는 추후 재검토 예정
// Batch update returned unexpected row count from update ==> OptimisticLock 문제 수정용
            if ( results.isNotEmpty() ) {
                results.map { it -> entityManager.detach(it) }
            }
            return results
        }finally {
            entityManager.clear()
        }
    }

//    @Transactional
    override fun bulkInsert(entities: Collection<T>): List<T> {
        if (entities.isEmpty()) return emptyList()

        return transactionTemplate.execute { status ->
            try {
                val inserted = entities.chunked(Constants.JPA_BATCH_SIZE).flatMap { chunk ->
                    chunk.map { entity ->
                        entityManager.persist(entity)
                        entity
                    }
                }
                entityManager.flush()
                inserted
            } catch (e: Exception) {
                status.setRollbackOnly()
                throw e
            } finally {
                entityManager.clear()
            }
        } ?: throw PersistenceException("Transaction returned null result")
    }

//    @Transactional
    override fun bulkUpdate(entities: Collection<T>): List<T> {
        if (entities.isEmpty()) return emptyList()

        return transactionTemplate.execute { status ->
            try {
                val updated = entities.chunked(Constants.JPA_BATCH_SIZE).flatMap { chunk ->
                    chunk.map { entity ->
                        entityManager.merge(entity)
                    }
                }
                entityManager.flush()
                updated
            } catch (e: Exception) {
                status.setRollbackOnly()
//                throw PersistenceException("Failed to merge entities", e)
                throw e
            } finally {
                entityManager.clear()
            }
        } ?: throw PersistenceException("Transaction returned null result")
    }


    //    @Transactional
    override fun bulkDeleteByIds(domainClass:Class<T>, ids: Collection<ID>):Int {
        if (ids.isEmpty()) return 0

        var result = 0
        return transactionTemplate.execute { status ->
            try {
                result = ids.chunked(Constants.JPA_BATCH_SIZE).sumOf { chunk ->
                    val queryString = "DELETE FROM ${domainClass.simpleName} e WHERE e.id IN :ids"
                    entityManager.createQuery(queryString)
                        .setParameter("ids", chunk)
                        .executeUpdate()
                }
                result
            }catch (e:Exception){
                status.setRollbackOnly()
                throw e
            }finally {
//                entityManager.clear()
                clearContext()
            }
        } ?: throw PersistenceException("Transaction returned null result")
    }



    private fun clearContext() {
        entityManager.flush()
        entityManager.clear()
    }

    fun getTableName(entityClass: Class<*>): String {
        val tableAnnotation = entityClass.getAnnotation(jakarta.persistence.Table::class.java)
        return tableAnnotation?.name?.ifEmpty { entityClass.simpleName } ?: entityClass.simpleName
    }

    fun convertToArray(chunk: List<ID>): Array<*> {
        return when (chunk.firstOrNull()) {
            is String -> chunk.map { it as String }.toTypedArray()
            is Long -> chunk.map { it as Long }.toTypedArray()
            is Int -> chunk.map { it as Int }.toTypedArray()
            is UUID -> chunk.map { it as UUID }.toTypedArray()
            else -> throw IllegalArgumentException("Unsupported ID type")
        }
    }
}