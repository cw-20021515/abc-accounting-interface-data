package com.abc.us.accounting.supports.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

data class TimeUnitConfig(
    val fixedDelay: Long,
    val initialDelay: Long,
    val timeUnit: TimeUnit
) {
    val fixedDelayMillis: Long
        get() = timeUnit.toMillis(fixedDelay)

    val initialDelayMillis: Long
        get() = timeUnit.toMillis(initialDelay)
}

@Component("schedulingProperties")
@ConfigurationProperties(prefix = "scheduling")
class SchedulingProperties {
    lateinit var auditTriggerManager: AuditTriggerManagerConfig
    lateinit var auditEventListener: AuditEventListenerConfig
    lateinit var credentials: CredentialsConfig

    class AuditTriggerManagerConfig {
        lateinit var refresh: TimeUnitConfig
    }

    class AuditEventListenerConfig {
        lateinit var poll: TimeUnitConfig
    }
    class CredentialsConfig {
        lateinit var refresh: TimeUnitConfig
    }
}