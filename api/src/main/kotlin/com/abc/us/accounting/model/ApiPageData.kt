package com.abc.us.accounting.model

import com.abc.us.generated.models.AccountingPage
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid

data class ApiPageData<T>(

    @field:Valid
    @Schema(example = "null", description = "")
    @get:JsonProperty("page") val page: AccountingPage? = null,

    @field:Valid
    @Schema(example = "null", description = "")
    @get:JsonProperty("items") var items: T? = null
)