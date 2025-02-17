package com.abc.us.accounting.collects.collectable

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import mu.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.server.ResponseStatusException

open class Collectable (
    private val sortProperty: String,
    private val pageSize: Int,
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    data class Paging(

        @get:Min(1)
        @Schema(example = "null", required = true, description = "현재 페이지")
        @get:JsonProperty("current", required = true) val current: kotlin.Int,

        @Schema(example = "10", required = true, description = "전체 페이지 수")
        @get:JsonProperty("total", required = true) val total: kotlin.Int,

        @Schema(example = "20", required = true, description = "페이지 당 항목 수")
        @get:JsonProperty("size", required = true) val propertySize: kotlin.Int,

        @Schema(example = "200", required = true, description = "전체 항목 수")
        @get:JsonProperty("totalItems", required = true) val totalItems: kotlin.Int
    ) {

    }

    protected fun pagenation(block: (PageRequest) -> Paging) {

        var currentPage = 1
        var page: Paging

        do {
            val pageable = PageRequest.of(currentPage, pageSize, Sort.Direction.DESC, sortProperty)
            page = block(pageable)
            currentPage++
        } while (currentPage <= page.total)

    }

    protected fun <T> visit(request : (Int,Int,String)-> ResponseEntity<T>, response: (T?) -> Paging) {

        pagenation() { pageable ->
            val responseEntity = request(pageable.pageNumber, pageable.pageSize, Sort.Direction.DESC.name)

            if (responseEntity.statusCode == HttpStatus.OK) {
                response(responseEntity.body)
            } else {
                throw ResponseStatusException(responseEntity.statusCode, responseEntity.statusCode.toString())
            }
        }
    }
}