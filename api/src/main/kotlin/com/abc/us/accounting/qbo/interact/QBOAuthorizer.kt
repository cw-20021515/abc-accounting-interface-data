package com.abc.us.accounting.qbo.interact

import com.abc.us.accounting.qbo.domain.entity.QboCredential
import com.abc.us.accounting.supports.converter.JsonConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.intuit.oauth2.data.BearerTokenResponse
import com.intuit.oauth2.data.UserInfoResponse
import com.intuit.oauth2.exception.ConnectionException
import com.intuit.oauth2.exception.OAuthException
import com.intuit.oauth2.exception.OpenIdException
import com.intuit.oauth2.http.HttpRequestClient
import com.intuit.oauth2.http.MethodType
import com.intuit.oauth2.http.Request
import mu.KotlinLogging
import org.apache.commons.codec.binary.Base64
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair
import org.json.JSONObject
import org.springframework.util.StringUtils
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.RSAPublicKeySpec

class QBOAuthorizer (private var discovery : Discovery) {

    companion object {
        private val logger = KotlinLogging.logger {}
        private val converter = JsonConverter()
        private val mapper = ObjectMapper()
    }

    private fun getAuthHeader(credential : QboCredential): String {
        val bytesEncoded =
            Base64.encodeBase64((credential.clientId + ":" + credential.clientSecret).toByteArray())
        val base64ClientIdSec = String(bytesEncoded)
        return "Basic $base64ClientIdSec"
    }

    private fun getUrlParameters(action: String, token: String): List<NameValuePair> {
        val urlParameters: MutableList<NameValuePair> = ArrayList()
        if (action === "revoke") {
            urlParameters.add(BasicNameValuePair("token", token))
        } else if (action === "refresh") {
            urlParameters.add(BasicNameValuePair("refresh_token", token))
            urlParameters.add(BasicNameValuePair("grant_type", "refresh_token"))
        } else {
            urlParameters.add(BasicNameValuePair("code", token))
            urlParameters.add(BasicNameValuePair("redirect_uri", discovery.redirectUri))
            urlParameters.add(BasicNameValuePair("grant_type", "authorization_code"))
        }
        return urlParameters
    }

    private fun buildRequest(credential: QboCredential, token : String, action: String) : Request{

        return Request.RequestBuilder(MethodType.POST, discovery.bearerTokenEndpoint)
            .requiresAuthentication(true)
            .authString(getAuthHeader(credential))
            .postParams(getUrlParameters(action, token))
            .build()
    }
    private fun requestBearerToken(request : Request) : BearerTokenResponse {
        try {
            val client = HttpRequestClient(discovery.proxyConfig)
            val response = client.makeRequest(request)
            logger.info{"requestBearerToken - Response( code = ${response.statusCode} intuit_tid=${response.intuit_tid}"  }
            if (response.statusCode != 200) {
                logger.debug("Failed to refresh token")
                logger.debug("Response content : " + response.content)
                throw OAuthException(
                    "Failed to refresh token",
                    response.statusCode.toString() + "",
                    response.intuit_tid,
                    response
                )
            }
            val reader = mapper.readerFor(BearerTokenResponse::class.java)
            val bearerTokenResponse = reader.readValue<BearerTokenResponse>(response.content)
            bearerTokenResponse.intuit_tid = response.intuit_tid
            return bearerTokenResponse
        } catch (ex: OAuthException) {
            logger.error("requestBearerToken while calling refreshToken:  " + ex.responseContent)
            throw OAuthException(ex.message, ex.statusCode + "", ex.intuit_tid, ex.response)
        } catch (ex: java.lang.Exception) {
            logger.error("Exception while requestBearerToken calling refreshToken ")
            throw OAuthException(ex.message, ex)
        }
    }
    @Throws(OAuthException::class)
    fun refreshBearerToken(credential: QboCredential): BearerTokenResponse {
        logger.info("refreshBearerToken(${credential.realmId})")
        if(!StringUtils.hasText(credential.refreshToken)) {
            throw NoSuchFieldException("RefreshToken '${credential.companyCode}' not found")
        }
        val request = buildRequest(credential,credential.refreshToken!!,"refresh")
        return requestBearerToken(request)
    }
    @Throws(OAuthException::class)
    fun retrieveBearerTokens(credential: QboCredential, token: String): BearerTokenResponse {
        logger.info("refreshBearerToken(${credential.realmId})")
        val request = buildRequest(credential,token,"")
        return requestBearerToken(request)
    }
    @Throws(OpenIdException::class)
    fun getUserInfo(accessToken: String): UserInfoResponse {
        logger.info("getUserInfo(${accessToken})")

        try {
            val request =
                Request.RequestBuilder(MethodType.GET, discovery.userProfileEndpoint)
                    .requiresAuthentication(true)
                    .authString("Bearer $accessToken")
                    .build()

            val client = HttpRequestClient(discovery.proxyConfig)
            val response = client.makeRequest(request)
            logger.info{"getUserInfo-Response( code = ${response.statusCode} intuit_tid=${response.intuit_tid}"  }
            if (response.statusCode == 200) {
                val reader = mapper.readerFor(
                    UserInfoResponse::class.java
                )
                val userInfoResponse = reader.readValue<Any>(response.content) as UserInfoResponse
                userInfoResponse.intuit_tid = response.intuit_tid
                return userInfoResponse
            } else {
                logger.debug("getUserInfo-Failed getting user info")
                throw OpenIdException(
                    "failed getting user info",
                    response.statusCode.toString() + "",
                    response.intuit_tid,
                    response
                )
            }
        } catch (var7: OpenIdException) {
            logger.error("getUserInfo while retrieving user info: " + var7.responseContent)
            throw OpenIdException("failed getting user info", var7.statusCode + "", var7.intuit_tid, var7.response)
        } catch (var8: java.lang.Exception) {
            logger.error("Exception while getUserInfo retrieving user info ", var8)
            throw OpenIdException(var8.message, var8)
        }
    }
    @Throws(OpenIdException::class)
    fun validateIDToken(idToken: String,validate : QboCredential): Boolean {
        logger.info("validateIDToken(token=${idToken}")
        val idTokenParts = idToken.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (idTokenParts.size < 3) {
            logger.debug("invalid idTokenParts length")
            return false
        } else {
            val idTokenHeader: String = this.base64UrlDecode(idTokenParts[0])
            val idTokenPayload: String = this.base64UrlDecode(idTokenParts[1])
            val idTokenSignature: ByteArray = this.base64UrlDecodeToBytes(idTokenParts[2])
            val idTokenHeaderJson = JSONObject(idTokenHeader)
            val idTokenHeaderPayload = JSONObject(idTokenPayload)
            val issuer = idTokenHeaderPayload.getString("iss")
            if (!issuer.equals(this.discovery.issuer, ignoreCase = true)) {
                logger.info("validateIDToken issuer value mismtach")
                return false
            } else {
                val jsonaud = idTokenHeaderPayload.getJSONArray("aud")
                val aud = jsonaud.getString(0)
                if (!aud.equals(validate.clientId, ignoreCase = true)) {
                    logger.info("validateIDToken incorrect client id")
                    return false
                } else {
                    val expirationTimestamp = idTokenHeaderPayload.getLong("exp")
                    val currentTime = System.currentTimeMillis() / 1000L
                    if (expirationTimestamp - currentTime <= 0L) {
                        logger.info("validateIDToken expirationTimestamp has elapsed")
                        return false
                    } else {
                        val keyMap: MutableMap<String, JSONObject> = this.getKeyMapFromJWKSUri()
                        if (keyMap != null && !keyMap.isEmpty()) {
                            val keyId = idTokenHeaderJson.getString("kid")
                            val keyDetails = keyMap[keyId]
                            val exponent = keyDetails!!.getString("e")
                            val modulo = keyDetails!!.getString("n")
                            val publicKey: PublicKey = this.getPublicKey(modulo, exponent)
                            val data = (idTokenParts[0] + "." + idTokenParts[1]).toByteArray(StandardCharsets.UTF_8)

                            try {
                                val isSignatureValid: Boolean =
                                    this.verifyUsingPublicKey(data, idTokenSignature, publicKey)
                                logger.info("validateIDToken isSignatureValid: $isSignatureValid")
                                return isSignatureValid
                            } catch (var21: GeneralSecurityException) {
                                logger.error("Exception while validating ID token ", var21)
                                throw OpenIdException(var21.message, var21)
                            }
                        } else {
                            logger.info("unable to retrive keyMap from JWKS url")
                            return false
                        }
                    }
                }
            }
        }
    }
    @Throws(OpenIdException::class)
    private fun getKeyMapFromJWKSUri(): MutableMap<String, JSONObject> {
        logger.info("getKeyMapFromJWKSUri")

        try {
            val client = HttpRequestClient(discovery.proxyConfig)
            val request =
                Request.RequestBuilder(MethodType.GET, discovery.jwksUri)
                    .requiresAuthentication(false).build()
            val response = client.makeRequest(request)
            logger.info("getKeyMapFromJWKSUri-Response Code : " + response.statusCode)
            if (response.statusCode != 200) {
                logger.info("getKeyMapFromJWKSUri failed JWKS URI")
                throw OpenIdException("getKeyMapFromJWKSUri failed JWKS URI", response.statusCode.toString() + "")
            } else {
                return buildKeyMap(response.content)
            }
        } catch (var4: java.lang.Exception) {
            logger.error("Exception while retrieving jwks ", var4)
            throw OpenIdException(var4.message, var4)
        }
    }
    private fun getPublicKey(MODULUS: String, EXPONENT: String): PublicKey {
        val nb = this.base64UrlDecodeToBytes(MODULUS)
        val eb = this.base64UrlDecodeToBytes(EXPONENT)
        val n = BigInteger(1, nb)
        val e = BigInteger(1, eb)
        val rsaPublicKeySpec = RSAPublicKeySpec(n, e)

        try {
            val publicKey = KeyFactory.getInstance("RSA").generatePublic(rsaPublicKeySpec)
            return publicKey
        } catch (var9: java.lang.Exception) {
            logger.error("Exception while getting public key ", var9)
            throw RuntimeException("Cant create public key", var9)
        }
    }

    @Throws(GeneralSecurityException::class)
    private fun verifyUsingPublicKey(data: ByteArray, signature: ByteArray, pubKey: PublicKey): Boolean {
        val sig = Signature.getInstance("SHA256withRSA")
        sig.initVerify(pubKey)
        sig.update(data)
        return sig.verify(signature)
    }

    private fun base64UrlDecode(input: String): String {
        val decodedBytes = this.base64UrlDecodeToBytes(input)
        val result = String(decodedBytes, StandardCharsets.UTF_8)
        return result
    }

    private fun base64UrlDecodeToBytes(input: String): ByteArray {
        val decoder = Base64(-1, null as ByteArray?, true)
        val decodedBytes = decoder.decode(input)
        return decodedBytes
    }

    @Throws(ConnectionException::class)
    private fun buildKeyMap(content: String): MutableMap<String,JSONObject> {
        var retMap : MutableMap<String,JSONObject> = mutableMapOf()
        val jwksPayload = JSONObject(content)
        val keysArray = jwksPayload.getJSONArray("keys")

        for (i in 0 until keysArray.length()) {
            val obj = keysArray.getJSONObject(i)
            val keyId = obj.getString("kid")
            retMap[keyId] = obj
        }
        return retMap
    }
}