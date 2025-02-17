package com.abc.us.accounting.configs

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.method.HandlerMethod
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView

@Component
class CommonInterceptor : HandlerInterceptor {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        if (request is MultipartHttpServletRequest) {
            RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
        }

        return true
    }

    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?
    ) {
        this.callLog(request, handler, modelAndView)
    }

    private fun callLog(request: HttpServletRequest, handler: Any, modelAndView: ModelAndView?) {
        try {
            if (handler is HandlerMethod) {
                val method = handler

                var logFormat = ""
                val requestUri = request.requestURI
                val className = method.beanType.simpleName
                val methodName = method.method.name

                if (modelAndView != null && modelAndView.viewName != null) {
                    val buffer = StringBuffer()
                    buffer.append("\n---------------------------------------")
                    buffer.append(" EXECUTE INFORMATION ")
                    buffer.append("----------------------------------------\n")
                    val viewName = modelAndView.viewName

                    logFormat += "| URL                   : %s\n"
                    logFormat += "| CLASS > METHOD > VIEW : %s.java > %s > %s\n"

                    buffer.append(String.format(logFormat, requestUri, className, methodName, viewName))
                    buffer.append("--------------------------------------------------")
                    buffer.append("--------------------------------------------------\n")
                    logger.info(buffer.toString())
                }
            }
        } catch (ex: Exception) {
            logger.error("CommonInterceptor postHandle : ${ex.message}")
        }
    }
}