package com.abc.us.accounting.documents.model.deprecated

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "응답_회계_시산표_조회")
//주생성자 사용
data class ResCompoundTrialBalanceDto (
    var accountCode: String? = null //계정코드
    ,var level: Int? = null //계정그룹 level
    ,var accountName: String? = null //계정이름
    ,var beginningDebitBalance: Double? = null //차변기초잔액
    ,var totalDebitAmount: Double? = null //차변합계
    ,var endingDebitBalance: Double? = null //차변기말잔액
    ,var beginningCreditBalance: Double? = null //대변기초잔액
    ,var totalCreditAmount: Double? = null //대변합계
    ,var endingCreditBalance: Double? = null //대변기말잔액
    ,@JsonIgnore var groupCode: String? = null //그룹코드
    ,@JsonIgnore var parentAccountCode: String? = null //상위계정코드
    ,@JsonIgnore var isAccount: String? = null //계정과목 여부
)
