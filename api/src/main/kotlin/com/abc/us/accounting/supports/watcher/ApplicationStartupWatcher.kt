package com.abc.us.accounting.supports.watcher

import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

private var started = false
@Component
class ApplicationStartupWatcher() : ApplicationListener<ApplicationReadyEvent?> {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        started = true
        logger.info { "Application Ready!" }
    }
}