package com.abc.us.accounting.supports.client

import com.abc.us.generated.models.CommonCodeIdListGetResponse
import com.abc.us.generated.models.StandardErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "default")
interface CommonsClient {

    @Throws(Throwable::class)
    @Operation(
        summary = "공통코드 최신 버전 목록 조회",
        operationId = "getCommonCodes",
        description = """- 공통코드 목록을 조회합니다.
- 등록된 공통코드 목록이 스냅샷과 관계없이 최신버전으로 반환됩니다.""",
        responses = [
            ApiResponse(responseCode = "200", description = "공통코드 최신 버전 목록 조회 성공", content = [Content(schema = Schema(implementation = CommonCodeIdListGetResponse::class))]),
            ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = [Content(schema = Schema(implementation = StandardErrorResponse::class))])
        ]
    )
    @RequestMapping(
        method = [RequestMethod.GET],
        value = ["/commons/v1/common-codes"],
        produces = ["application/json"]
    )
    fun getCommonCodes(
        @Parameter(description = "공통코드 식별자(아이디)")
        @Valid
        @RequestParam(value = "commonCodeIds", required = false) commonCodeIds: kotlin.collections.List<kotlin.String>?,

        @Parameter(description = "- 공통코드 삭제여부 조건  - `NONE` : 조회조건으로 isRemoved 미사용 - `TRUE` or `FALSE` : 조회조건으로 true or false 사용 ", schema = Schema(allowableValues = ["NONE", "TRUE", "FALSE"], defaultValue = "NONE"))
        @Valid
        @RequestParam(value = "isRemovedCriteria", required = false, defaultValue = "NONE") isRemovedCriteria: kotlin.String
    ): ResponseEntity<CommonCodeIdListGetResponse>
}