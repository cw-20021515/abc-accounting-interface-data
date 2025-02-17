package com.abc.us.accounting.supports

import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.core.io.ClassPathResource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class MockUtil {

    companion object {
        private val logger = KotlinLogging.logger {}

        inline fun <reified T> getDataFromJson(path: String): T {
            try {
                val stringBuilder = StringBuilder()
                val resource = ClassPathResource(path)
                BufferedReader(InputStreamReader(resource.inputStream)).useLines { lines ->
                    lines.forEach { stringBuilder.append(it) }
                }
                val data = GsonBuilder()
                    .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                    .create()
                    .fromJson(stringBuilder.toString(), T::class.java)
                return data
            } catch (exception: IOException) {
                return null as T
            }
        }

        fun excelDownload(
            path: String,
            response: HttpServletResponse,
        ) {
            try {
                val resource = ClassPathResource(path)
                val fileName = resource.filename
                response.contentType = "ms-vnd/excel"
                response.setHeader(
                    HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("$fileName", StandardCharsets.UTF_8).build().toString()
                )
                response.outputStream.write(resource.inputStream.readAllBytes())
            } catch (ex: IOException) {
                logger.error("Excel Download Error : $ex.message")
            }
        }
    }
}