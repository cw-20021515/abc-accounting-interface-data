package com.abc.us.accounting.qbo.domain.repository

//import com.abc.us.accounting.config.Constants
//import com.abc.us.accounting.qbo.domain.entity.QboAccount
//import com.abc.us.accounting.supports.converter.JsonConverter
//import jakarta.persistence.EntityManager
//import mu.KotlinLogging
//import org.springframework.stereotype.Component
//import org.springframework.stereotype.Service
//import org.springframework.transaction.annotation.Isolation
//import org.springframework.transaction.annotation.Transactional
//
//@Service
//class QboAccountRepositoryTransactional(
//    private val entityManager: EntityManager,
//    private val qboAccountRepository: QboAccountRepository
//) : QboAccountRepository by qboAccountRepository {
//
//    companion object {
//        private val logger = KotlinLogging.logger {}
//    }
//
//    private fun clearContext() {
//        entityManager.flush()
//        entityManager.clear()
//    }
//
//    private fun <T:Any> bulkInsert(entities: Collection<T>): MutableList<T> {
//
//        if (entities.isEmpty())
//            return mutableListOf()
//
//        val inserted = entities.chunked(Constants.JPA_BATCH_SIZE).flatMap { chunk ->
//            chunk.map { entity ->
//                entityManager.persist(entity)
//                entity
//            }
//        }.toMutableList()
//        clearContext()
//        return inserted
//    }
//
//    @Transactional(isolation = Isolation.READ_COMMITTED)
//    fun saveAll(accounts : MutableList<QboAccount>) {
//        logger.info { "QBO-BULK-INSERT[${this::class.java.simpleName}]-COUNT(${accounts.size})" }
//        this.bulkInsert(accounts)
//    }
//}
