package com.abc.us.accounting.qbo.interact

import com.abc.us.accounting.qbo.domain.entity.QboCompany
import com.abc.us.accounting.qbo.domain.entity.QboCredential
import com.abc.us.accounting.qbo.domain.repository.QboCompanyRepository
import com.abc.us.accounting.qbo.domain.repository.QboCredentialRepository
import com.abc.us.accounting.supports.converter.toISO
import com.abc.us.accounting.supports.properties.CredentialProperties
import com.abc.us.accounting.supports.properties.SchedulingProperties
import com.intuit.ipp.exception.AuthenticationException
import com.intuit.oauth2.data.BearerTokenResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

@Component
class QBOCertifier(
    private val environment: Environment,
    @Qualifier("schedulingProperties")
    private var schedulingProperties : SchedulingProperties,

    @Qualifier("credentialProperties")
    private var credentialProperties: CredentialProperties,

    @Qualifier("qboDiscovery")
    private val discovery : Discovery,
    private val credentialRepository : QboCredentialRepository,
    private val qboCompanyRepository: QboCompanyRepository
) : ApplicationListener<ContextRefreshedEvent>  {


    companion object {
        private val logger = KotlinLogging.logger {}
        private val csrfTokens = mutableSetOf<String>()
    }

    @JvmInline
    value class ByRealmId(val value: String )

    @JvmInline
    value class ByCompanyCode(val value: String )

    private val authorizer = QBOAuthorizer(discovery)

    //Map<companyCode,Company>
    var companies : MutableMap<String, QboCompany> = mutableMapOf()

    // Map<realmId, credential>
    var credentials : MutableMap<String, QboCredential> = mutableMapOf()
    val profile: String get() = credentialProperties.profile
    val baseUrl : String? get() = discovery.quickbooksBaseUrl

    fun buildLogMsg() : String {
        val msgBuilder = StringBuilder()
        msgBuilder.append("Companies=[\n")
        companies.forEach { companyCode,company ->
            msgBuilder.append("\t{")
            msgBuilder.append(company.code)
            msgBuilder.append(",")
            msgBuilder.append(company.name)
            msgBuilder.append("}\n")
        }
        msgBuilder.append("]\n")
        msgBuilder.append("Credential=[\n")
        credentials.forEach { credential,qboCredential ->
            msgBuilder.append("\t{")
            msgBuilder.append(qboCredential.realmId)
            msgBuilder.append(",")
            msgBuilder.append(qboCredential.companyCode)
            msgBuilder.append(",")
            qboCredential.accessTokenExpireTime?.let {
                msgBuilder.append(it.toLocalDate().toString())
            }
            msgBuilder.append("}\n")
        }
        msgBuilder.append("]")
        return msgBuilder.toString()
    }

    fun configure() : String{
        configureCompanies()
        configureCredentials()
        return buildLogMsg()
    }

    fun configureCompanies() {

        companies = qboCompanyRepository.findAll().map { company->
            company.credentials = mutableMapOf()
            company
        }.associateBy { it.code }.toMutableMap()
    }

    fun scheduledRefreshTokens() : String {
        val canSchedulingEnabled =
            environment.getProperty("scheduling.credentials.enabled", "true").toBoolean()

        if(canSchedulingEnabled) {
            refreshTokenWithCondition(beforeMinute = 2)
        }

        return buildLogMsg()
    }

    fun configureCredentials(){

        companies.forEach { code, company ->
            credentialRepository.findByCompanyCode(company.code)?.let { finds ->
                finds.forEach { credential ->
                    company.credentials[credential.activeProfile] = credential
                    credentials[credential.realmId] = credential
                }
            }
        }
    }

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        configure()
    }
    @Scheduled(
        fixedDelayString = "#{schedulingProperties.credentials.refresh.fixedDelayMillis}",
        initialDelayString = "#{schedulingProperties.credentials.refresh.initialDelayMillis}"
    )
    fun onScheduledRefreshTokens() {
        scheduledRefreshTokens()
    }

    fun buildCredential(tokens : BearerTokenResponse?, other : QboCredential) : QboCredential? {

        return tokens?.let {
            val now = OffsetDateTime.now(ZoneOffset.UTC)
            val credential = QboCredential.of(other).apply {
                accessToken = tokens.accessToken
                refreshToken = tokens.refreshToken
                tokenType = tokens.tokenType
                idToken = tokens.idToken
                accessTokenIssuedTime = now
                refreshTokenIssuedTime = now
                accessTokenExpireTime = now.plusSeconds(tokens.expiresIn)
                refreshTokenExpireTime = now.plusSeconds(tokens.xRefreshTokenExpiresIn)
                // TODO : 현재로써는 의미가 없는 시간이긴 한데 추후에 어떻게 써먹을지 고민은 필요해보임
                updateTime = now
                createTime = now
            }
            credential
        }
    }
    fun flush(credential : QboCredential, tokens : BearerTokenResponse) : QboCredential? {
        val newC  =buildCredential(tokens,credential)
        if(newC != null) {
            val company = companies[newC.companyCode]
            company?.let { it.credentials[profile] = newC }
            credentials[newC.realmId] = newC
            credentialRepository.save(newC)
        }
        return newC
    }
    fun refreshTokenWithCondition(beforeMinute: Long) {
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val iso8856Now = now.toISO()
        companies.forEach{ (code,company)->
            val credential = credentialRepository.findByCompanyCodeAndActiveProfile(code,profile)
            credential?.let {c ->
                c.accessTokenExpireTime?.let { expire ->
                    //var iso8856 = expire.toISO()
                    val adjustedExpire = expire.minusMinutes(beforeMinute)
                    if (now.isAfter(adjustedExpire)) {
                        try {
                            val tokens = authorizer.refreshBearerToken(credential)
                            flush(credential,tokens)
                        } catch (e: Exception) {
                            logger.error { "refreshTokenWithCondition-Failure update refreshToken[${company.name}.${profile}(${code})]-[${e.message}]" }
                        }
                    }
                    else {
                        //logger.info { "refreshTokenWithCondition-Time remains until it expires[${company.name}.${profile}(${code})]-[now=${iso8856Now}]-[expire=${iso8856}]" }
                    }
                }
            }
        }
    }

    fun getCompany(code: ByCompanyCode) : QboCompany? {

        return companies.get(code.value)
    }


    fun getCredential(realmId : ByRealmId) : QboCredential {
        if ( credentials.get(realmId.value) == null ) {
            throw NoSuchFieldException("Credential '${realmId.value}' not found")
        }
        return credentials[realmId.value]!!
    }

    fun getCredential(code : ByCompanyCode) : QboCredential {
        if(companies.get(code.value) == null) {
            throw NoSuchFieldException("Credential '$code' not found")
        }
        if(companies[code.value]!!.credentials.get(profile) == null) {
            throw NoSuchFieldException("Credential 'profile(${profile})' not found")
        }
        return companies[code.value]!!.credentials[profile]!!
    }
    fun refreshToken(credential: QboCredential?) : QboCredential? {
        if(credential == null ) {
            throw AuthenticationException("Do not credential null value")
        }

        if (credential.refreshToken == null) {
            val errmsg =
                "You have not yet obtained authentication information." +
                    "Please log in to obtain authentication information."
            throw AuthenticationException(errmsg)
        }
        var newCredential : QboCredential? = null
        credential.refreshToken?.let {token ->
            val bearerTokenResponse = authorizer.refreshBearerToken(credential)
            newCredential = flush(credential,bearerTokenResponse)
        }
        return newCredential
    }

    fun updateToken(realmId: String, token: String,csrfToken : String) {

        val credential = getCredential(ByRealmId(realmId))
        if(!csrfTokens.contains(csrfToken))
            throw NoSuchFieldException("Credential '$csrfToken' not found")
        csrfTokens.remove(csrfToken)
        val bearerTokens = authorizer.retrieveBearerTokens(credential,token)
        if (StringUtils.hasText(credential.idToken)) {
            if (credential.idToken?.let { authorizer.validateIDToken(it,credential) } == true){
                logger.info("IdToken is Valid")
                val userInfo = authorizer.getUserInfo(credential.accessToken!!)
                credential.sub = userInfo.sub
                credential.givenName = userInfo.givenName
                credential.email = userInfo.email
            }
        }
        flush(credential,bearerTokens)
    }

    fun prepareUrl(companyCode : String): String {
        val credential = getCredential(ByCompanyCode(companyCode))
        try {
            val csrfToken = UUID.randomUUID().toString()
            val url: String = (
                discovery.authorizationEndpoint +
                    "?client_id=" + credential.clientId +
                    "&response_type=code" +
                    "&scope=" + URLEncoder.encode(credential.scope, "UTF-8") +
                    "&redirect_uri=" + URLEncoder.encode(discovery.redirectUri, "UTF-8") +
                    "&state=" + csrfToken
                )
            csrfTokens.add(csrfToken)
            return url
        } catch (e: UnsupportedEncodingException) {
            logger.error("Exception while preparing url for redirect ", e)
            throw e
        }
    }
    fun visit( block : (QboCredential)->Unit ) {
        companies.forEach {code, company ->
            company.credentials[profile]?.let{
                block(it)
            }
        }
    }

}