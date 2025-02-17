package com.abc.us.accounting.configs

import com.abc.us.accounting.config.Constants
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.dao.CannotAcquireLockException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.RetryListener
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.EnableRetry
import org.springframework.retry.backoff.ExponentialBackOffPolicy
import org.springframework.retry.backoff.FixedBackOffPolicy
import org.springframework.retry.policy.SimpleRetryPolicy
import org.springframework.retry.support.RetryTemplate

@Configuration
@EnableRetry  // 이게 있는지 확인
class RetryConfig {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Bean
    @Primary
    fun retryTemplate(): RetryTemplate {
        return RetryTemplate().apply {
            val retryTemplate = RetryTemplate()

            val retryPolicy = SimpleRetryPolicy(
                Constants.MAX_ATTEMPT,  // maxAttempts
                mapOf(Throwable::class.java to true)  // retryable exceptions
            )

            val backOffPolicy = ExponentialBackOffPolicy().apply {
                initialInterval = 100L
                maxInterval = 1000L
                multiplier = 2.0
            }
            retryTemplate.setRetryPolicy(retryPolicy)
            retryTemplate.setBackOffPolicy(backOffPolicy)
        }
    }

    @Bean
    @Qualifier("documentServiceRetry")
    fun documentServiceRetryTemplate(): RetryTemplate {
        return RetryTemplate().apply {
            setRetryPolicy(SimpleRetryPolicy(
                Constants.MAX_ATTEMPT,
                mapOf(
                    DataIntegrityViolationException::class.java to true,
                )
            ))

            setBackOffPolicy(ExponentialBackOffPolicy().apply {
                initialInterval = Constants.BACKOFF_DELAY
                maxInterval = Constants.BACKOFF_DELAY * 4
                multiplier = 2.0
            })

            registerListener(object : RetryListener {
                override fun <T : Any, E : Throwable> open(context: RetryContext, callback: RetryCallback<T, E>): Boolean {
                    logger.info("Starting retry operation. Attempt: ${context.retryCount}")
                    return true
                }

                override fun <T : Any, E : Throwable> onError(context: RetryContext, callback: RetryCallback<T, E>, throwable: Throwable) {
                    logger.error("Retry error occurred: ${throwable.message}, attempt: ${context.retryCount}")
                }

                override fun <T : Any, E : Throwable> close(context: RetryContext, callback: RetryCallback<T, E>, throwable: Throwable?) {
                    logger.info("Retry operation completed. Final status: ${if (throwable == null) "SUCCESS" else "FAILED"}")
                }
            })
        }
    }


    @Bean
    @Qualifier("accountBalanceServiceRetry")
    fun accountBalanceRetryTemplate(): RetryTemplate {
        return RetryTemplate().apply {
            setRetryPolicy(SimpleRetryPolicy(
                Constants.MAX_ATTEMPT,
                mapOf(
                    CannotAcquireLockException::class.java to true,
                    ObjectOptimisticLockingFailureException::class.java to true,
                    DataIntegrityViolationException::class.java to true,
                )
            ))

            setBackOffPolicy(ExponentialBackOffPolicy().apply {
                initialInterval = Constants.BACKOFF_DELAY
                maxInterval = Constants.BACKOFF_DELAY * 4
                multiplier = 2.0

            })

            registerListener(object : RetryListener {
                override fun <T : Any, E : Throwable> open(context: RetryContext, callback: RetryCallback<T, E>): Boolean {
                    logger.info("Starting retry operation. Attempt: ${context.retryCount}")
                    return true
                }

                override fun <T : Any, E : Throwable> onError(context: RetryContext, callback: RetryCallback<T, E>, throwable: Throwable) {
                    logger.error("Retry error occurred: ${throwable.message}, attempt: ${context.retryCount}")
                }

                override fun <T : Any, E : Throwable> close(context: RetryContext, callback: RetryCallback<T, E>, throwable: Throwable?) {
                    logger.info("Retry operation completed. Final status: ${if (throwable == null) "SUCCESS" else "FAILED"}")
                }
            })
        }
    }
}