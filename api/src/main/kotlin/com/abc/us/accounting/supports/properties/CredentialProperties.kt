package com.abc.us.accounting.supports.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component


@Component("credentialProperties")
@ConfigurationProperties(prefix = "credential")
data class CredentialProperties(
    var redirectUri: String ="",
    //var realmId : String ="",
    var playground : String ="",
    var profile : String = ""
) {
    companion object    {
        fun of(other: CredentialProperties): CredentialProperties {
            return CredentialProperties(//realmId = other.realmId,
                                        redirectUri = other.redirectUri,
                                        playground = other.playground,
                                        profile = other.profile)
        }
    }
}