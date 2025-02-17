package com.abc.us.accounting.supports.pubsub

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import mu.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class EventMessageConsumer(private val container: EventListenerContainer) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Async("eventMessageExecutor")
    @EventListener
    fun onEventMessageConsume(trailer: AsyncEventTrailer) {

        val listener = container.findListeners(trailer.listener())
        listener?.let {
            val method = listener.first
            val bean = listener.second;
            method.invoke(bean, trailer)
        }
    }
}