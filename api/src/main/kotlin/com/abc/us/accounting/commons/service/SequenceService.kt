package com.abc.us.accounting.commons.service

import com.abc.us.accounting.commons.domain.entity.CustomSequence
import com.abc.us.accounting.documents.exceptions.DocumentException
import org.postgresql.util.PSQLException
import org.slf4j.LoggerFactory
import org.springframework.dao.CannotAcquireLockException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import kotlin.random.Random

@Service
class SequenceService(
    private val jdbcTemplate: JdbcTemplate
) {
    companion object {
        private val logger = LoggerFactory.getLogger(SequenceService::class.java)
        private const val current_value = "current_value"
        private const val version = "version"

        // 현재 값과 버전 조회
        private const val SELECT_SEQUENCE = """
            SELECT current_value, version 
            FROM custom_sequence 
            WHERE sequence_name = ?
        """

        // 버전 확인과 함께 업데이트
        private const val UPDATE_SEQUENCE = """
            UPDATE custom_sequence 
            SET current_value = ?, version = ? 
            WHERE sequence_name = ? AND version = ?
        """

        // 새 시퀀스 생성
        private const val INSERT_SEQUENCE = """
            INSERT INTO custom_sequence (sequence_name, current_value, version) 
            VALUES (?, ?, ?) 
            ON CONFLICT (sequence_name) DO NOTHING
        """
    }

    @Transactional(
        propagation = Propagation.REQUIRES_NEW,
        timeout = 2
    )
    fun getNextValue(sequenceName: String): Long {
        return try {
            // 시퀀스 조회 또는 생성
            val sequence = getOrCreateSequence(sequenceName)
            // 업데이트 시도
            updateSequence(sequence)
        } catch (e: OptimisticLockingException) {
            logger.debug("Optimistic lock failed for sequence: $sequenceName")
            throw e
        }
    }

    private fun getOrCreateSequence(sequenceName: String): CustomSequence {
        return jdbcTemplate.query(SELECT_SEQUENCE, { rs, _ ->
            CustomSequence.create(
                name = sequenceName,
                initialValue = rs.getLong(current_value),
                version = rs.getLong(version)
            )
        }, sequenceName).firstOrNull() ?: createSequence(sequenceName)
    }

    private fun createSequence(sequenceName: String): CustomSequence {
        val initialValue = 0L
        val initialVersion = 0L

        val inserted = jdbcTemplate.update(
            INSERT_SEQUENCE,
            sequenceName,
            initialValue,
            initialVersion
        )

        return if (inserted > 0) {
            CustomSequence.create(sequenceName, initialValue, initialVersion)
        } else {
            // 동시 생성 시도로 인해 이미 존재하는 경우 재조회
            jdbcTemplate.query(SELECT_SEQUENCE, { rs, _ ->
                CustomSequence.create(
                    name = sequenceName,
                    initialValue = rs.getLong(current_value),
                    version = rs.getLong(version)
                )
            }, sequenceName).first()
        }
    }

    private fun updateSequence(sequence: CustomSequence): Long {
        val nextValue = sequence.currentValue + 1
        val nextVersion = sequence.version + 1

        val updated = jdbcTemplate.update(
            UPDATE_SEQUENCE,
            nextValue,
            nextVersion,
            sequence.name,
            sequence.version
        )

        if (updated == 0) {
            throw OptimisticLockingException("Sequence was modified concurrently: ${sequence.name}")
        }

        return nextValue
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun getNextValueWithRetry(
        sequenceName: String,
        maxAttempts: Int = 5,
        backoffMillis: Long = 10
    ): Long {
        var attempt = 0
        var lastException: Exception? = null

        while (attempt < maxAttempts) {
            try {
                return getNextValue(sequenceName)
            } catch (e: Exception ) {
                if ( e is OptimisticLockingException || e is PSQLException || e is CannotAcquireLockException) {
                    lastException = e
                    attempt++
                    if (attempt < maxAttempts) { // retry
                        val sleepTimeClaim = backoffMillis * (1L shl attempt) // exponential backoff
                        val sleepTime = Random.nextLong(sleepTimeClaim)
                        logger.info("acquire lock failed, attempt $attempt of $maxAttempts, retrying after ${sleepTime}ms by exception: ${e.javaClass.simpleName}")
                        Thread.sleep(sleepTime)
                    }
                } else {
                    throw e
                }
            }
        }

        throw DocumentException.SequenceGenerationException("Failed to generate sequence after $maxAttempts attempts", cause = lastException)
    }

    class OptimisticLockingException(message: String, cause: Throwable? = null) :
        RuntimeException(message, cause)
}
