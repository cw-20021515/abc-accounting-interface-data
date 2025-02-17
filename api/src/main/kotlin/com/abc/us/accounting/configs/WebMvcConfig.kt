package com.abc.us.accounting.configs


import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


//@EnableWebMvc
//@Configuration
//class WebMvcConfig(val commonInterceptor: CommonInterceptor) : WebMvcConfigurer {
//
//    @Value("\${project.apiPathPattn}")
//    private val apiPathPattn: String? = null
//    override fun addInterceptors(registry: InterceptorRegistry) {
//
//        // URL 로깅 인터셉터 추가
//        if (commonInterceptor != null) {
//            registry.addInterceptor(commonInterceptor).addPathPatterns("$apiPathPattn/**")
//                .excludePathPatterns("/accounting/v1/payouts/favicon.ico"
//                    ,"/accounting/v1/payouts/swagger-ui/**"
//                    ,"/accounting/v1/payouts/swagger-resources/**"
//                    ,"/accounting/v1/payouts/api-docs/**"
//                    ,"/favicon.ico"
//                )
//        }
//    }
//}
