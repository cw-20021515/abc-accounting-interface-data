package com.abc.us.accounting.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "응답_헤더")
class ResHeader {

    @field:Schema(description = "결과코드")
    var rsltCode: String = "SUCCESS"

    @field:Schema(description = "결과메시지내용")
    var rsltMsge: String = "요청이 성공했습니다." // The request was successful[요청이 성공했습니다]

    @field:Schema(description = "결과 개수")
    var rsltCnt = 0

    fun failed(): ResHeader {
        this.rsltCode = "BAD_REQUEST"
        this.rsltMsge = "Invalid request data." // Invalid request data[잘못된 데이터 요청]
        return this
    }

    fun failed(rsltCode: String, rsltMsge: String): ResHeader {
        this.rsltCode = rsltCode
        this.rsltMsge = rsltMsge
        return this
    }

    constructor()

    constructor(rsltCode: String, rsltMsge: String) {
        this.rsltCode = rsltCode
        this.rsltMsge = rsltMsge
        rsltCnt = 1
    }

    constructor(rsltCode: String, rsltMsge: String, rsltCnt: Int) {
        this.rsltCode = rsltCode
        this.rsltMsge = rsltMsge
        this.rsltCnt = rsltCnt
    }
}

