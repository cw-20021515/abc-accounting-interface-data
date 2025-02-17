package com.abc.us.accounting.qbo.interact

import com.abc.us.accounting.supports.properties.CredentialProperties
import com.intuit.ipp.util.Config
import com.intuit.oauth2.config.Environment
import com.intuit.oauth2.config.OAuth2Config
import com.intuit.oauth2.config.ProxyConfig
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component

@Component("qboDiscovery")
class Discovery(
    @Qualifier("credentialProperties")
    var properties: CredentialProperties,
) : ApplicationListener<ContextRefreshedEvent> {

    var issuer: String? = null
    var quickbooksBaseUrl : String? = null
    var authorizationEndpoint: String? = null
    var bearerTokenEndpoint: String? = null
    var userProfileEndpoint: String? = null
    var revocationEndpoint: String? = null
    var jwksUri: String? = null
    var proxyConfig : ProxyConfig? = null

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        quickbooksBaseUrl = Config.getProperty(Config.BASE_URL_QBO)
        logger.info { "QuickBooks Connection Base URL = [$quickbooksBaseUrl]" }
        val oauth2Config = OAuth2Config.OAuth2ConfigBuilder(null, null)
            .proxyConfig(null)
            .callDiscoveryAPI(Environment.fromValue(properties.playground))
            .buildConfig()
        issuer = oauth2Config?.intuitIdTokenIssuer
        authorizationEndpoint = oauth2Config?.intuitAuthorizationEndpoint
        bearerTokenEndpoint = oauth2Config?.intuitBearerTokenEndpoint
        userProfileEndpoint = oauth2Config?.userProfileEndpoint
        revocationEndpoint = oauth2Config?.intuitRevokeTokenEndpoint
        jwksUri = oauth2Config?.intuitJwksURI
    }

    val redirectUri: String get() = properties.redirectUri
}