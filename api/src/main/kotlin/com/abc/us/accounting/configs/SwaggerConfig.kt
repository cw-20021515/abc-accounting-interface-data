package com.abc.us.accounting.configs

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.servers.Server
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@OpenAPIDefinition(
    info = Info(title = "ABC-accounting 코웨이(NECOA) 랜탈 서비스  API",
        description = "코웨이(NECOA) ABC(AI BIZ CORE)-accounting API 명세서"),
    servers = [
        Server(url = "/",description = "Server"),
        Server(url = "https://apis.abc.necoa.dev",description = "DEV Server"),
        Server(url = "https://apis.abc.necoa.blue", description = "STG Server")
    ]
)
@Configuration
class SwaggerConfig {

    val securitySchemeName = "x-abc-sdk-apikey"

    @Bean
    fun customOpenAPI(): OpenAPI {
        val securityRequirement = SecurityRequirement()
            .addList(securitySchemeName)

        return OpenAPI()
            .addSecurityItem(securityRequirement)
            .components(Components().addSecuritySchemes(securitySchemeName, apiKey()))
    }

    protected fun apiKey(): SecurityScheme {
        val apiKey: SecurityScheme = SecurityScheme()
            .type(SecurityScheme.Type.APIKEY)
            .`in`(SecurityScheme.In.HEADER)
            .name(securitySchemeName)
            .description(
                "<font size='2'>This is the API key required for accessing the ABC SDK.<br>" +
                        "dev: XUrUVTo5pKSECGT3vU7dkippIcFkIERg<br>" +
                        "stg: LisK83DJavOZZP5VC5Krql5iLHKXYLAJ<br>" +
                        "Please use the appropriate key based on your environment." +
                        "</font>"
            )

        return apiKey
    }
}