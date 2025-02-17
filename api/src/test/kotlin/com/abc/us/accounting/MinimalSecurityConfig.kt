package com.abc.us.accounting

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder

@TestConfiguration
class MinimalSecurityConfig {
    @Bean
    fun jwtDecoder(): JwtDecoder {
        return object : JwtDecoder {
            override fun decode(token: String): Jwt {
                return Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .claim("sub", "test")
                    .build()
            }
        }
    }

//    @Bean
//    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
//        http {
//            authorizeHttpRequests {
//                it.anyRequest().permitAll()
//            }
//            csrf { it.disable() } // CSRF 비활성화
//        }
//        return http.build()
//    }
}