package com.abc.us.accounting

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableScheduling
import java.util.*

@SpringBootApplication(scanBasePackages = ["com.abc.us"])
@ComponentScan(basePackages = ["com.abc.us"])
@EntityScan(basePackages = ["com.abc.us"])
@EnableFeignClients(basePackages = ["com.abc.us"])
@EnableScheduling
open class ApiMain

fun main(args: Array<String>) {
    System.setProperty("spring.config.name", "application")
    System.setProperty("com.amazonaws.sdk.disableEc2Metadata", "true")
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    println("Default TimeZone set to: ${TimeZone.getDefault().id}")
    runApplication<ApiMain>(*args)
}