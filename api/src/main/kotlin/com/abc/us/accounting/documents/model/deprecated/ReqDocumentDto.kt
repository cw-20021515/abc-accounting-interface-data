package com.abc.us.accounting.documents.model.deprecated

import com.abc.us.accounting.documents.domain.type.DocumentDateType
import com.abc.us.accounting.documents.domain.type.DocumentStatus
import com.abc.us.accounting.documents.domain.type.DocumentType
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(name = "요청_회계_원장_전표_조회")
class ReqDocumentDto {

    @field:Schema(description = "기간유형", defaultValue = "DOCUMENT_DATE")
    //var  periodType: AccountingLedgersPeriodType = AccountingLedgersPeriodType.DOCUMENT_TIME  //기본:증빙일
    var periodType: DocumentDateType? = DocumentDateType.DOCUMENT_DATE

    @field:Schema(description = "시작일", defaultValue = "2024-08-01")
    var periodFromDate: LocalDate? = null

    @field:Schema(description = "종료일", defaultValue = "2024-08-31")
    var periodToDate: LocalDate? = null

    @field:Schema(description = "회사코드", defaultValue = "ALL")
    //autocomplete : NECOA(N100), NECOA Tech(N200), NECOA Holdings(N300)
    var companyCode: String? = "ALL"

    @field:Schema(description = "회계연도", defaultValue = "2024")
    var fiscalYear: String? = null   //년도

    @field:Schema(description = "회계월", defaultValue = "10")
    var fiscalMonth: String? = null  //월단위

    @field:Schema(description = "전표유형코드", defaultValue = "ALL")
    var documentTypeCode: DocumentType? = null  //전표유형 (autocomplete)

    @field:Schema(description = "생성자", defaultValue = "ABC")
    var createId: String? = null  //생성자

    @field:Schema(description = "전표상태", defaultValue = "ALL")
    //전표상태
    var documentStatus: DocumentStatus? = null

    @field:Schema(description = "정렬기준", defaultValue = "DOCUMENT_DATE")
    var sortBy: String? = "DOCUMENT_DATE"

    @field:Schema(description = "정렬방향", defaultValue = "ASC")
    var direction: String? = "ASC"

    @field:Schema(description = "현재 페이지", defaultValue = "1")
    var current: Int = 1

    @field:Schema(description = "페이지 당 항목 수", defaultValue = "30")
    var size: Int = 30

    fun excelHeadExport(list: List<ResDocumentExcelDto>): List<List<Any?>> {
        return list.map {
            listOf(
                  it.documentId ?: ""            /* 전표ID */
                , it.documentTypeCode ?: ""      /* 전표유형코드 */
                , it.documentType ?: ""          /* 전표유형 */
                , it.documentStatus ?: ""        /* 전표상태 */
                , it.approvalStatus ?: ""        /* 승인상태 */
                , it.companyCode ?: ""           /* 회사코드 */
                , it.documentDate ?: ""          /* 증빙일 */
                , it.postingDate ?: ""           /* 전기일 */
                , it.entryDate ?: ""             /* 발행일 */
                , it.currency ?: ""              /* 통화 */
                , it.amount ?: ""                /* 금액 */
                , it.remark ?: ""                /* 설명 */
                , it.reference ?: ""             /* 참조사유 */
                , it.createId ?: ""              /* 생성자ID */
                , it.referenceDocumentType ?: "" /* 참조전표유형코드 */
                , it.referenceDocumentId ?: ""   /* 참조전표ID */
                , it.bizTransactionTypeId ?: ""  /* 비즈거래유형코드(소스유형) */
                , it.bizTransactionId ?: ""      /* 비즈거래ID(소스거래ID) */
                , it.reversalDocumentId ?: ""    /* 역분개전표ID */
                , it.reversalReasonCode ?: ""    /* 역분개전표사유코드 */
                , it.reversalReason ?: ""        /* 역분개전표사유 */
                , it.documentItemId ?: ""        /* 전표항목번호 */
                , it.accountCode ?: ""           /* 계정코드 */
                , it.accountName ?: ""           /* 계정명 */
                , it.description ?: ""           /* 적요 */
                , it.currency ?: ""              /* 통화 */
                , it.debitAmount ?: ""           /* 차변('D') */
                , it.creditAmount ?: ""          /* 대변('C') */
                , it.documentStatus ?: ""        /* 전표상태_ */
                , it.refDocumentItemId ?: ""     /* 참조전표항목ID */
                , it.costCenter ?: ""            /* 코스트센터 */
                , it.profitCenter ?: ""          /* 손익센터 */
                , it.segment ?: ""               /* 세그먼트 */
                , it.project ?: ""               /* 프로젝트 */
                , it.customerId ?: ""            /* 고객ID */
                , it.orderId ?: ""               /* 주문번호 */
                , it.orderItemId ?: ""           /* 주문아이템ID */
                , it.serialNumber ?: ""          /* 시리얼번호 */
                , it.salesType ?: ""             /* 판매유형 */
                , it.salesItem ?: ""             /* 판매항목 */
                , it.rentalCode ?: ""            /* 렌탈코드 */
                , it.channelId ?: ""             /* 채널ID */
                , it.referralCode ?: ""          /* 레퍼럴코드 */
                , it.vendorId ?: ""              /* 거래처ID */
                , it.payoutId ?: ""              /* 지급ID */
                , it.invoiceId ?: ""             /* 인보이스 */
                , it.purchaseOrderId ?: ""       /* PO */
                , it.materialId ?: ""            /* 자재ID */
                , it.materialType ?: ""          /* 자재유형 */
                , it.materialCategory ?: ""      /* 카테고리 */
                , it.installType ?: ""           /* 설치유형 */
                , it.filterType ?: ""            /* 필터 */
                , it.featureType ?: ""           /* 기능군 */
            )
        }
    }
}