package com.abc.us.accounting.model

import com.abc.us.generated.models.AccountingPage
import org.springframework.data.domain.Page

@Suppress("UNCHECKED_CAST")
class ApiPageResponse<T> {
    var code: String = ""
    var message: String = ""
    var data: ApiPageData<T>? = null

    constructor(header: ResHeader, data: Any?) {
        this.code = header.rsltCode
        this.message = header.rsltMsge
        if (data is Page<*>) {
            val dataBilling: ApiPageData<T> = ApiPageData(
                page = AccountingPage(
                    current = data.pageable.pageNumber + 1,
                    total = data.totalPages,
                    propertySize = data.pageable.pageSize,
                    totalItems = data.totalElements.toInt()
                ), items = data.content as T
            )
            this.data = dataBilling
        } else {
            val dataBilling: ApiPageData<T> =
                ApiPageData(items = data as T)
            this.data = dataBilling
        }
    }

    constructor(header: ResHeader) {
        this.code = header.rsltCode
        this.message = header.rsltMsge
    }

    fun ok(): ApiPageResponse<T> {
        return this
    }
}
