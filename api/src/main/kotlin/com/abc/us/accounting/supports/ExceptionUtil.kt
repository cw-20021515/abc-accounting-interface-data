package com.abc.us.accounting.supports

import org.springframework.http.ResponseEntity

class ExceptionUtil {

    companion object {
        fun notFound(
            key: String,
            value: String,
        ): ResponseEntity<Any> {
            val body = mapOf(
                "code" to "BAD_REQUEST_PARAMETER",
                "requestData" to mapOf(
                    key to value
                ),
                "message" to "not found $key: $value"
            )
            return ResponseEntity.badRequest().body(body)
        }

        fun notFound404(): ResponseEntity<Any> {
            return ResponseEntity.notFound().build()
        }
    }
}