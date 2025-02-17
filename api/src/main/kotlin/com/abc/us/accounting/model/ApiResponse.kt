package com.abc.us.accounting.model

class ApiResponse<T> {
//    var header: ResHeader
    var code: String = ""
    var message: String = ""
    var data: T? = null

    constructor(header: ResHeader, data: T?) {
        this.code = header.rsltCode
        this.message = header.rsltMsge
        this.data = data
    }

    constructor(header: ResHeader) {
        this.code = header.rsltCode
        this.message = header.rsltMsge
    }

    fun ok(): ApiResponse<T> {
        return this
    }
}
