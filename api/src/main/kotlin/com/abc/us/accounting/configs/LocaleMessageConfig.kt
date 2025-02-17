package com.abc.us.accounting.configs

import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.MessageSourceAccessor
import java.security.SecureRandom


@Configuration
class LocaleMessageConfig  {
    @Bean
    fun messageSourceAccessor(messageSource: MessageSource?): MessageSourceAccessor {
        val message = MessageSourceAccessor(messageSource!!)
        return message
    }

    @Bean
    fun secureRandom(): SecureRandom {
        return SecureRandom()
    }
}
