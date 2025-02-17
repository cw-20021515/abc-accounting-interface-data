package com.abc.us.accounting.supports.client

import feign.Feign
import feign.Logger
import mu.KotlinLogging
import okhttp3.OkHttpClient
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Configuration
@EnableDiscoveryClient
@Profile("local")  // 'local' 프로파일에서만 활성화
class IgnoreSslFeignClientConfig {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Bean
    fun feignBuilder(): Feign.Builder {
        return Feign.builder()
            .client(feign.okhttp.OkHttpClient(okHttpClient()))  // feign.okhttp.OkHttpClient로 수정
    }
    @Bean
    fun feignLoggerLevel(): Logger.Level {
        return Logger.Level.FULL
    }

    @Bean
    fun okHttpClient(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier(HostnameVerifier { _, _ -> true })
            .build()
    }
}