package com.abc.us.accounting.documents.model.deprecated

import com.abc.us.generated.models.AccountingAttributeType
import com.abc.us.generated.models.AccountingLedgerState
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Page
import java.time.LocalDate

@Schema(name = "요청_회계_원장_조회")
class ReqLedgerDto {

    //증빙일(DOCUMENT_DATE),전기일(POSTING_DATE),발행일(ENTRY_DATE),만기일(DUE_DATE)
    @field:Schema(description = "기간유형", defaultValue = "DOCUMENT_DATE") //기본:증빙일
    //var periodType: AccountingLedgersPeriodType = AccountingLedgersPeriodType.DOCUMENT_DATE  //기본:증빙일
    var periodType: String? = "DOCUMENT_DATE"

    @field:Schema(description = "시작일", defaultValue = "2024-08-01")
    var periodFromDate: LocalDate? = null //LocalDate.parse("2024-10-01")

    @field:Schema(description = "종료일", defaultValue = "2024-08-31")
    var periodToDate: LocalDate? = null

    @field:Schema(description = "계정그룹", defaultValue = "1117")
    var accountGroup: String? = null

    @field:Schema(description = "계정코드 시작", defaultValue = "1111000")
    var accountCodeFrom: String? = null

    @field:Schema(description = "계정코드 종료", defaultValue = "4103020")
    var accountCodeTo: String? = null

    @field:Schema(description = "회사코드", defaultValue = "N100")
    //NECOA(N100), NECOA Tech(N200), NECOA Holdings(N300)
    //var companyCode: CompanyCode? = CompanyCode.NECOA
    var companyCode: String? = "N100"

    @field:Schema(description = "코스트센터", defaultValue = "")
    var costCenter: String? = null

    @field:Schema(description = "속성", defaultValue = "ALL")
    var accountingAttributeType: AccountingAttributeType? = AccountingAttributeType.ALL
   // var accountingAttributeType: List<String>? = AccountingAttributeType.ALL as List<String>?  //다건 처리시

    @field:Schema(description = "상태", defaultValue = "ALL")  //ALL ,OPEN ,CLEARED
    var accountingLedgerState: AccountingLedgerState? = AccountingLedgerState.ALL

    //@field:Schema(description = "속성관련 입력데이터", defaultValue = "")
    //var accountingAttributeTypeValue: String? = null

    /***************************** 추가 extraData *******************************/
    //고객
    @field:Schema(description = "고객ID", defaultValue = "817c44bb985253e3a563cb769d268cb0")
    var customerId: String? = null  //_M

    @field:Schema(description = "주문번호", defaultValue = "106424900078")
    var orderId: String? = null //_M

    @field:Schema(description = "시리얼번호", defaultValue = "20102FQP0110900223")
    var serialNumber: String? = null  //_M

    //거래처
    @field:Schema(description = "거래처ID", defaultValue = "")
    var vendorId: String? = null  //_M

    @field:Schema(description = "지급ID", defaultValue = "")
    var payoutId: String? = null  //_M

    @field:Schema(description = "PO", defaultValue = "")
    var purchaseOrderId: String? = null  //_M

    //자재
    @field:Schema(description = "자재번호(ID)", defaultValue = "")
    var materialId: String? = null  //_M

    /********************************************************/

    @field:Schema(description = "정렬기준", defaultValue = "DOCUMENT_DATE")
    var sortBy: String? = "DOCUMENT_DATE"

    @field:Schema(description = "정렬방향", defaultValue = "ASC")
    var direction: String? = "ASC"

    @field:Schema(description = "현재 페이지", defaultValue = "1")
    var current: Int = 1

    @field:Schema(description = "페이지 당 항목 수", defaultValue = "30")
    var size: Int = 30

    fun excelHeadExport(list: Page<ResLedgerDto>): List<List<Any?>> {
        return list.content.map { dto ->
            listOf(
                dto.documentStatus ?: ""       /* 전표상태(전체, 미결, 반제) */,
                dto.documentId ?: ""           /* 전표ID */,
                dto.documentTypeCode ?: ""     /* 전표유형코드 */,
                dto.documentType ?: ""         /* 전표유형 */,
                dto.documentDate ?: ""         /* 증빙일(실제 거래발생일) */,
                dto.postingDate ?: ""          /* 전기일(거래가 재무제표에 반영되는날짜) */,
                dto.entryDate ?: ""            /* 발행일(회계기록이 시스템에 입력된날짜) */,
                dto.documentItemId ?: ""       /* 전표항목ID */,
                dto.companyCode ?: ""          /* 회사코드 */,
                dto.accountCode ?: ""          /* 계정코드 */,
                dto.accountName ?: ""          /* 계정명 */,
                dto.remark ?: ""               /* 적요 */,
                dto.currency ?: ""             /* 통화(USD) */,
                dto.debitAmount ?: ""          /* 차변('D') */,
                dto.creditAmount ?: ""         /* 대변('C') */,
                dto.balance ?: ""              /* 잔액 */,
                dto.costCenter ?: ""         /* 코스트센터 */,
                dto.profitCenter ?: ""         /* 손익센터 */,
                dto.segment ?: ""              /* 세그먼트 */,
                dto.project ?: ""              /* 프로젝트 */,
                dto.customerId ?: ""           /* 고객ID */,
                dto.orderId ?: ""              /* 주문번호 */,
                dto.orderItemId ?: ""          /* 주문아이템ID */,
                dto.serialNumber ?: ""         /* 시리얼번호 */,
                dto.salesType ?: ""            /* 판매유형 */,
                dto.salesItem ?: ""            /* 판매항목 */,
                dto.rentalCode ?: ""           /* 렌탈코드 */,
                dto.channelId ?: ""            /* 채널ID */,
                dto.referralCode ?: ""         /* 레퍼럴코드 */,
                dto.vendorId ?: ""             /* 거래처ID */,
                dto.payoutId ?: ""             /* 지급ID */,
                dto.invoiceId ?: ""            /* 인보이스 */,
                dto.purchaseOrderId ?: ""      /* PO */,
                dto.materialId ?: ""           /* 자재ID */,
                dto.materialType ?: ""         /* 자재유형 */,
                dto.materialCategory ?: ""     /* 카테고리 */,
                dto.installType ?: ""          /* 설치유형 */,
                dto.filterType ?: ""           /* 필터  */,
                dto.featureType ?: ""          /* 기능군 */
            )
        }
    }
}
