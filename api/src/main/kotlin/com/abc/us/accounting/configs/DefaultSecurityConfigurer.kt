package com.abc.us.accounting.configs

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity(
    securedEnabled = true,
    prePostEnabled = true
)
@EnableWebSecurity
class DefaultSecurityConfigurer {

    /**
     * 어플리케이션 기본 보안 설정
     */
    @Bean
    fun defaultSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .authorizeHttpRequests { authorize ->
                // @PreAuthorize 어노테이션으로 권한 확인을 진행하므로 모든 Request 허용 처리.
                authorize.anyRequest().permitAll()
            }
            .oauth2ResourceServer { server ->
                // JWT 형식의 OAuth2 인증 사용
                server.jwt {}
            }
            .csrf { csrfConfigurer ->
                csrfConfigurer.disable()
            }
            .cors(Customizer.withDefaults())
            .sessionManagement { sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .build()
    }
}