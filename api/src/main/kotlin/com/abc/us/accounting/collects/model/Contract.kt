package com.abc.us.accounting.collects.model

import java.time.OffsetDateTime

class Contract {

    // 계약 아이디
    var contractId: String?=null

    // 접수채널에서 생성된 계약 아이디
    var channelContractId: String? = null

    // 판매채널 주문상세 아이디
    var channelOrderItemId: String? = null

    // 서명여부
    var isSigned: Boolean?=null

    //서명시간
    var signedTime: java.time.OffsetDateTime? = null

    //계약서 양식 아이디
    var formId: String? = null

    //계약서 리비전
    var revision: Int?=null

    // 렌탈 코드
    var rentalCode: String?=null

    // 고객아이디
    var customerId: String?=null

    // 주문아이디
    var orderId: String?=null

    // 주문상세 아이디
    var orderItemId: String?=null

    // 자재 ID
    var materialId: String?=null

   //자재 설치 로케이션 ID
    var installationLocationId: String?=null

    //약정 시작
    var startDate: java.time.LocalDate? = null

    // 약정 끝 날짜
    var endDate: java.time.LocalDate? = null

    // 약정기간
    var durationInMonths: Int?=null

    // 계약상태 code
    var contractStatus: String?=null

    // 생성시간
    var createTime: OffsetDateTime?=null

    // 수정시간
    var updateTime: OffsetDateTime?= null
}