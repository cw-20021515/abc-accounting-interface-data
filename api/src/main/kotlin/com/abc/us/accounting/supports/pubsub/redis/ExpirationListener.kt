package com.abc.us.accounting.supports.pubsub.redis

import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.stereotype.Component

@Component
class ExpirationListener() : MessageListener {
    override fun onMessage(message: Message, pattern: ByteArray?) {
        val expiredKey = message.toString()
        println("Expired key: $expiredKey")
    }
}