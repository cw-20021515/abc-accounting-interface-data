package com.abc.us.accounting.configs

import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.stereotype.Component
import java.util.*

@Component
class LocaleMessageHandler {

    @Autowired
    private val msgeSourceAccessor: MessageSourceAccessor? = null

    fun getMessage(code: String): String {
//        return this.getMessage(code, null, Locale.KOREA)
        return this.getMessage(code, null, Locale.getDefault())
    }

    fun getMessage(code: String, defaultMessage: String?): String {
        return this.getMessage(code, defaultMessage, Locale.getDefault())
    }

    fun getMessage(code: String, defaultMessage: String?, locale: Locale?): String {
        var message = ""

        if (StringUtils.isBlank(code)) {
            return message
        }

        message.let { msgeSourceAccessor!!.getMessage(this.setCodePrefix(code), defaultMessage!!, locale!!) }.toString()
            .also { message = it }

        return message
    }

    private fun setCodePrefix(code: String): String {
        if (StringUtils.isBlank(code)) {
            return code
        }

        var resultCode = code

        if (!code.startsWith(CODE_PREFIX)) {
            resultCode = CODE_PREFIX + code
        }

        return resultCode
    }

    companion object {
        private const val CODE_PREFIX = "ABC-ERROR-"
    }
}
