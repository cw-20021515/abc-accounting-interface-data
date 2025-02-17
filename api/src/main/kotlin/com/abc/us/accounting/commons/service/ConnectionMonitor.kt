package com.abc.us.accounting.commons.service

import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ConnectionMonitor(
    private val hikariDataSource: HikariDataSource
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val threshold = 10 // 임계값 설정

    @Scheduled(fixedRate = 300000) // 5분
    fun monitorConnections() {
        val poolStats = """${hikariDataSource.poolName} - ConnectionPoolStats:{Active: ${hikariDataSource.hikariPoolMXBean.activeConnections}, Idle: ${hikariDataSource.hikariPoolMXBean.idleConnections}, Total: ${hikariDataSource.hikariPoolMXBean.totalConnections}, Threads Awaiting: ${hikariDataSource.hikariPoolMXBean.threadsAwaitingConnection}}""".trimIndent()

        logger.info { "Connection pool stats: $poolStats" }
        if (hikariDataSource.hikariPoolMXBean.activeConnections > threshold) {
            logger.warn { "Too many active connections. Cleaning..., active: ${hikariDataSource.hikariPoolMXBean.activeConnections}, threshold: $threshold" }
            hikariDataSource.hikariPoolMXBean.softEvictConnections()
        }
    }
}
