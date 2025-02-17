package com.abc.us.accounting.documents.model.deprecated

import com.abc.us.generated.models.AccountingCompanyCode
import com.abc.us.generated.models.AccountingDisplayLevel
import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(name = "요청_회계_시산표_조회")
 class ReqCompoundTrialBalanceDto {

 @field:Schema(description = "조회 시작월", defaultValue = "2024-08")
 var periodFromMonth: String? =  null

 @field:Schema(description = "조회 종료월", defaultValue = "2024-09")
 var periodToMonth: String? =  null

 @field:Schema(description = "회사코드", defaultValue = "N100")
 var companyCode: AccountingCompanyCode? = null

 @field:Schema(description = "계정그룹시작코드", defaultValue = "")
 var accountGroupFrom: String? = null

 @field:Schema(description = "계정그룹종료코드", defaultValue = "")
 var accountGroupTo: String? = null

 @field:Schema(description = "표시레벨", defaultValue = "1")
 var displayLevel: List<AccountingDisplayLevel?>? = null

 @field:Schema(description = "계정코드 시작", defaultValue = "")
 var accountCodeFrom: String? = null

 @field:Schema(description = "계정코드 종료", defaultValue = "")
 var accountCodeTo: String? = null

 @field:Schema(description = "현재 페이지", defaultValue = "1")
 var current: Int = 1

 @field:Schema(description = "페이지 당 항목 수", defaultValue = "30")
 var size: Int = 30

 @JsonIgnore
 var periodFromDate: LocalDate? = null

 @JsonIgnore
 var periodToDate: LocalDate? = null

}