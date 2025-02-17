package com.abc.us.accounting.supports.pubsub.redis

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter

//@Configuration
//class RedisExpirationListenerConfig {
//    @Bean
//    fun redisMessageListenerContainer(connectionFactory: RedisConnectionFactory,listenerAdapter: MessageListenerAdapter
//    ): RedisMessageListenerContainer {
//        val container = RedisMessageListenerContainer()
//        container.setConnectionFactory(connectionFactory)
//        // 키 만료 이벤트를 구독
//        container.addMessageListener(listenerAdapter, ChannelTopic("__keyevent@0__:expired"))
//        return container
//    }
//    @Bean
//    fun listenerAdapter(expirationListener: ExpirationListener): MessageListenerAdapter {
//        return MessageListenerAdapter(expirationListener,"onMessage")
//    }
//}