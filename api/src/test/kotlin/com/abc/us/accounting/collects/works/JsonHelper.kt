package com.abc.us.accounting.collects.works

import com.abc.us.accounting.supports.converter.JsonConverter
import com.abc.us.generated.models.ResourceHistory
import com.abc.us.generated.models.ResourceHistoryListResponse
import com.fasterxml.jackson.core.type.TypeReference
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.reflect.KClass

class JsonHelper {

    companion object {

        fun readFromFile(filePath: String,clazz: KClass<*>) : String {
            val uri = clazz.java.classLoader.getResource(filePath)?.toURI()
                ?: throw IllegalArgumentException("File not found: $filePath")
            return Files.readString(Paths.get(uri))
        }

        fun jsonToResourceHistory(jsonData: String): List<ResourceHistory> {
            val converter = JsonConverter()
            try {
                val response = converter.toObjFromTypeRef(
                    jsonData,
                    object : TypeReference<ResourceHistoryListResponse>() {}
                )
                return response.data?.items ?: emptyList()
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to parse JSON: ${e.message}", e)
            }
        }
    }

}