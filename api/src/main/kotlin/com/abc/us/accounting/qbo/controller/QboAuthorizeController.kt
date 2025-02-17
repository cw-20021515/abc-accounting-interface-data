package com.abc.us.accounting.qbo.controller

import com.abc.us.accounting.qbo.interact.QBOCertifier
import com.abc.us.accounting.qbo.interact.QBOService
import jakarta.servlet.http.HttpSession
import mu.KotlinLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.View
import org.springframework.web.servlet.view.RedirectView

@Controller
@RequestMapping("/accounting/v1/qbo")
class QboAuthorizeController(private var certifier : QBOCertifier) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @RequestMapping("")
    fun home(model: Model): String {
        return "home"
    }

    @RequestMapping("/connected")
    fun connected(): String {
        return "connected"
    }

    @RequestMapping("/{companyCode}/connectToQuickbooks")
    fun connectToQuickbooks(@PathVariable(value = "companyCode") companyCode: String,
                            model: Model,
                            session: HttpSession): View {
        val redirectUrl = certifier.prepareUrl(companyCode)
        logger.info{"inside connectToQuickbooks (${redirectUrl}) "}
        return RedirectView(redirectUrl,true,true,false)
    }

    @RequestMapping("/{companyCode}/signInWithIntuit")
    fun signInWithIntuit(@PathVariable(value = "companyCode") companyCode: String,
                         model: Model,
                         session: HttpSession): View? {
        val redirectUrl = certifier.prepareUrl(companyCode)
        logger.info{"signInWithIntuit (${redirectUrl}) "}
        return RedirectView(redirectUrl,true,true,false)
    }

    @RequestMapping("/oauth2/redirect")
    fun callBackFromOAuth(
        @RequestParam("code") authCode: String?,
        @RequestParam("state") state: String?,
        @RequestParam("error") error: String?,
        @RequestParam("error_description") errorDescription: String?,
        @RequestParam(value = "realmId", required = false) realmId: String
    ): String? {

        if(StringUtils.hasText(error)) {
            logger.error { "FAILURE callBackFromOAuth(error=${error}, description=${errorDescription})" }
            return "connected"
        }

        logger.info { "callBackFromOAuth(code=${authCode}, state=${state}, realmId=${realmId})" }
        if (!StringUtils.hasText(realmId) || realmId.equals("null"))
            return "connected"

        certifier.updateToken(realmId,authCode!!,state!!)

        return "connected"
    }
}
