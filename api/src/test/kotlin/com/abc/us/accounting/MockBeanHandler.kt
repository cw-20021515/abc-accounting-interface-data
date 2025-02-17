package com.abc.us.accounting

import jakarta.servlet.ServletContext
import org.mockito.Mockito
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.handler.HandlerMappingIntrospector


@Configuration
class MockBeanHandler {
//    @Bean
//    @Primary
//    fun mvcHandlerMappingIntrospector(): HandlerMappingIntrospector {
//        return HandlerMappingIntrospector()
//    }

    @Bean(name = ["customMvcHandlerMappingIntrospector"])
    fun mvcHandlerMappingIntrospector(): HandlerMappingIntrospector {
        return HandlerMappingIntrospector()
    }

    @Bean
    fun servletContext(): ServletContext {
        return Mockito.mock(ServletContext::class.java)
    }
}