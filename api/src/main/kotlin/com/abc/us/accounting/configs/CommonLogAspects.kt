package com.abc.us.accounting.configs

import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.Signature
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Aspect
@Component
@EnableAspectJAutoProxy
class CommonLogAspects {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Pointcut("execution(public * com.abc.us.ledgers.controller.*.*Rest*.*(..))")
    private fun publicTarget() {
    }

    @Around("publicTarget() && args(.., @RequestBody reqBody)")
    @Throws(Throwable::class)
    fun actionHistory(pjp: ProceedingJoinPoint, reqBody: Any?): Any? {
        val request: HttpServletRequest =
            (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        val startTime = System.currentTimeMillis()

        val result: Any? = pjp.proceed()
        val endTime = System.currentTimeMillis()

        commonLogFormat(request.requestURI, pjp.signature, ( endTime - startTime )/1000.0f)
        return result
    }

    fun lastIndexOf(str: String, ch: Char): Int {
        val len = str.length
        for (i in len - 1 downTo 0) {
            if (str[i] == ch) {
                return i
            }
        }
        return -1
    }

    /**
     * LogAspect 로그 처리 포멧 정보
     */
    fun commonLogFormat(actionUri: String?, pjpSignature: Signature, apiTimeSeconds: Float) {
        var logFormat: String = ""
        val buffer = StringBuffer()
        val prefix : Int = (lastIndexOf(pjpSignature.declaringTypeName, '.') + 1)
        val className: String = pjpSignature.declaringTypeName.substring(prefix)
        val methodName: String = pjpSignature.name

        buffer.append("\n--------------------------------------------------")
        buffer.append("--------------------------------------------------\n")
        logFormat += "| URL            : %s\n"
        logFormat += "| CLASS > METHOD : %s.java > %s\n"
        logFormat += "| API TIME       : %s Seconds #######\n"
        buffer.append(
            String.format(logFormat, actionUri, className, methodName, String.format("%.4f", apiTimeSeconds))
        )
        buffer.append("--------------------------------------------------")
        buffer.append("--------------------------------------------------\n")
        logger.info(buffer.toString())
    }
}


