package com.abc.us.accounting.collects.trigger.audit

import com.abc.us.accounting.collects.domain.entity.audit.AuditEntityLog
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class AuditEntityLogClosure {

    @PersistenceContext
    private lateinit var entityManager: EntityManager
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    @Transactional
    fun applyComplete(event : AuditEntityLog) {
        try {
            event.processed = true
            entityManager.merge(event)
        }
        catch (e : Exception ) {
            logger.error { "Failure merge ${e.message}" }
        }
        finally {
            entityManager.close()
        }
    }
}