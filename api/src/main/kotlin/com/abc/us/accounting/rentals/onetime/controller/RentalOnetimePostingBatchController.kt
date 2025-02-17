package com.abc.us.accounting.rentals.onetime.controller

import com.abc.us.accounting.commons.domain.type.TimeZoneCode
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.type.DocumentTemplateCode
import com.abc.us.accounting.documents.model.DocumentServiceContext
import com.abc.us.accounting.rentals.onetime.service.OnetimeDocumentService
import com.abc.us.accounting.rentals.onetime.service.OnetimeDocumentService.Companion.DEFAULT_MAX_RESULT
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.OffsetDateTime

@RestController
@RequestMapping("/accounting/v1/rentals/onetime/batch/posting")
class RentalOnetimePostingBatchController(
    private val onetimeDocService : OnetimeDocumentService,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    fun createContext() : DocumentServiceContext {
        return DocumentServiceContext.SAVE_DEBUG
    }
    private inline fun <T> executeSafely(
        code: DocumentTemplateCode,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime,
        timezone: String,
        companyCode: CompanyCode,
        action: (OffsetDateTime,OffsetDateTime,DocumentTemplateCode,DocumentServiceContext) -> T
    ): ResponseEntity<Any> {

        logger.info {
            "ONETIME-POSTING[${code.koreanText}] FROM(${startDateTime})-TO(${endDateTime}-TZ(${timezone})) : COMPANY(${companyCode})"
        }

        val tzCode = TimeZoneCode.fromCode(timezone)
        val fromDateTime = tzCode.convertTime(startDateTime, TimeZoneCode.UTC)
        val toDateTime = tzCode.convertTime(endDateTime, TimeZoneCode.UTC)

        return try {
            val results = action(fromDateTime,toDateTime,code,createContext())
            ResponseEntity.ok(results)
        } catch (e: Exception) {
            logger.error("ONETIME-POSTING[${code.koreanText}] EXCEPTION: ${e.message}",e)
            ResponseEntity.ok("SUCCESS")
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    //                                      일시불 관련
    //------------------------------------------------------------------------------------------------------------------
    @PostMapping("/CTCP010")
    fun postingCTCP010(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode,
        @RequestParam(required = false) maxResult: Int = DEFAULT_MAX_RESULT,
        @RequestParam(required = false) test: Boolean = false
    ) : ResponseEntity<Any>  {
        return executeSafely(
            DocumentTemplateCode.ONETIME_PAYMENT_RECEIVED,
            startDateTime,
            endDateTime,
            timezone,
            companyCode) { from,to,code,context->
            onetimeDocService.processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(code), from, to, maxResult)
        }
    }

    @PostMapping("/CTCP050")
    fun postingCTCP050(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode,
        @RequestParam(required = false) maxResult: Int = DEFAULT_MAX_RESULT,
        @RequestParam(required = false) test: Boolean = false
    ) : ResponseEntity<Any>  {
        return executeSafely(
            DocumentTemplateCode.ONETIME_PAYMENT_DEPOSIT,
            startDateTime,
            endDateTime,
            timezone,
            companyCode) { from,to,code,context->
            onetimeDocService.processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(code), from, to, maxResult)
        }
    }
    @PostMapping("/CTLO010")
    fun postingCTLO010(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode,
        @RequestParam(required = false) maxResult: Int = DEFAULT_MAX_RESULT,
        @RequestParam(required = false) test: Boolean = false
    ) : ResponseEntity<Any>  {
        return executeSafely(
            DocumentTemplateCode.ONETIME_PRODUCT_SHIPPED,
            startDateTime,
            endDateTime,
            timezone,
            companyCode) { from,to,code,context->
            onetimeDocService.processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(code), from, to, maxResult)
        }
    }
    @PostMapping("/CTOR030")
    fun postingCTOR030(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode,
        @RequestParam(required = false) maxResult: Int = DEFAULT_MAX_RESULT,
        @RequestParam(required = false) test: Boolean = false
    ) : ResponseEntity<Any>  {
        return executeSafely(
            DocumentTemplateCode.ONETIME_SALES_RECOGNITION,
            startDateTime,
            endDateTime,
            timezone,
            companyCode) { from,to,code,context->
            onetimeDocService.processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(code), from, to, maxResult)
        }
    }
    @PostMapping("/CTOR040")
    fun postingCTOR040(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode,
        @RequestParam(required = false) maxResult: Int = DEFAULT_MAX_RESULT,
        @RequestParam(required = false) test: Boolean = false
    ) : ResponseEntity<Any>  {
        return executeSafely(
            DocumentTemplateCode.ONETIME_COGS_RECOGNITION,
            startDateTime,
            endDateTime,
            timezone,
            companyCode) { from,to,code,context->
            onetimeDocService.processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(code), from, to, maxResult)
        }
    }
    @PostMapping("/CTOR050")
    fun postingCTOR050(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode,
        @RequestParam(required = false) maxResult: Int = DEFAULT_MAX_RESULT,
        @RequestParam(required = false) test: Boolean = false
    ) : ResponseEntity<Any>  {
        return executeSafely(
            DocumentTemplateCode.ONETIME_ADVANCE_PAYMENT_OFFSET,
            startDateTime,
            endDateTime,
            timezone,
            companyCode) { from,to,code,context->
            onetimeDocService.processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(code), from, to, maxResult)
        }
    }
    @PostMapping("/CTOR060")
    fun postingCTOR060(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode,
        @RequestParam(required = false) maxResult: Int = DEFAULT_MAX_RESULT,
        @RequestParam(required = false) test: Boolean = false
    ) : ResponseEntity<Any>  {
        return executeSafely(
            DocumentTemplateCode.ONETIME_PRICE_DIFFERENCE,
            startDateTime,
            endDateTime,
            timezone,
            companyCode) { from,to,code,context->
            onetimeDocService.processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(code), from, to, maxResult)
        }
    }
    //------------------------------------------------------------------------------------------------------------------
    //                                      일시불 취소 관련
    //------------------------------------------------------------------------------------------------------------------
    @PostMapping("/CTCA010")
    fun postingCTCA010(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode,
        @RequestParam(required = false) maxResult: Int = DEFAULT_MAX_RESULT,
        @RequestParam(required = false) test: Boolean = false
    ) : ResponseEntity<Any>  {
        return executeSafely(
            DocumentTemplateCode.ONETIME_CANCEL_RECEIVED,
            startDateTime,
            endDateTime,
            timezone,
            companyCode) { from,to,code,context->
            onetimeDocService.processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(code), from, to, maxResult)
        }
    }

    @PostMapping("/CTCP030")
    fun postingCTCP030(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode,
        @RequestParam(required = false) maxResult: Int = DEFAULT_MAX_RESULT,
        @RequestParam(required = false) test: Boolean = false
    ) : ResponseEntity<Any>  {
        return executeSafely(
            DocumentTemplateCode.ONETIME_PAYMENT_VOID,
            startDateTime,
            endDateTime,
            timezone,
            companyCode) { from,to,code,context->
            onetimeDocService.processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(code), from, to, maxResult)
        }
    }
    @PostMapping("/CTCP040")
    fun postingCTCP040(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode,
        @RequestParam(required = false) maxResult: Int = DEFAULT_MAX_RESULT,
        @RequestParam(required = false) test: Boolean = false
    ) : ResponseEntity<Any>  {
        return executeSafely(
            DocumentTemplateCode.ONETIME_PAYMENT_REFUND,
            startDateTime,
            endDateTime,
            timezone,
            companyCode) { from,to,code,context->
            onetimeDocService.processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(code), from, to, maxResult)
        }
    }
    //------------------------------------------------------------------------------------------------------------------
    //                                      일시불 반품 관련
    //------------------------------------------------------------------------------------------------------------------
    @PostMapping("/CTRT010")
    fun postingCTRT010(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode,
        @RequestParam(required = false) maxResult: Int = DEFAULT_MAX_RESULT,
        @RequestParam(required = false) test: Boolean = false
    ) : ResponseEntity<Any>  {
        return executeSafely(
            DocumentTemplateCode.ONETIME_RETURN_RECEIVED,
            startDateTime,
            endDateTime,
            timezone,
            companyCode) { from,to,code,context->
            onetimeDocService.processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(code), from, to, maxResult)
        }
    }
    @PostMapping("/CTRT020")
    fun postingCTRT020(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode,
        @RequestParam(required = false) maxResult: Int = DEFAULT_MAX_RESULT,
        @RequestParam(required = false) test: Boolean = false
    ) : ResponseEntity<Any>  {
        return executeSafely(
            DocumentTemplateCode.ONETIME_RETURN_SALES_CANCELLED,
            startDateTime,
            endDateTime,
            timezone,
            companyCode) { from,to,code,context->
            onetimeDocService.processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(code), from, to, maxResult)
        }
    }
    @PostMapping("/CTRT030")
    fun postingCTRT030(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode,
        @RequestParam(required = false) maxResult: Int = DEFAULT_MAX_RESULT,
        @RequestParam(required = false) test: Boolean = false
    ) : ResponseEntity<Any>  {
        return executeSafely(
            DocumentTemplateCode.ONETIME_RETURN_PAYMENT_REFUND,
            startDateTime,
            endDateTime,
            timezone,
            companyCode) { from,to,code,context->
            onetimeDocService.processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(code), from, to, maxResult)
        }
    }
    @PostMapping("/CTLO030")
    fun postingCTLO030(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode,
        @RequestParam(required = false) maxResult: Int = DEFAULT_MAX_RESULT,
        @RequestParam(required = false) test: Boolean = false
    ) : ResponseEntity<Any>  {
        return executeSafely(
            DocumentTemplateCode.ONETIME_PRODUCT_RECEIVED_GRADE_B,
            startDateTime,
            endDateTime,
            timezone,
            companyCode) { from,to,code,context->
            onetimeDocService.processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(code), from, to, maxResult)
        }
    }
    @PostMapping("/CTRT005")
    fun postingCTRT005(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode,
        @RequestParam(required = false) maxResult: Int = DEFAULT_MAX_RESULT,
        @RequestParam(required = false) test: Boolean = false
    ) : ResponseEntity<Any>  {
        return executeSafely(
            DocumentTemplateCode.ONETIME_RETURN_PAYMENT_RECEIVED,
            startDateTime,
            endDateTime,
            timezone,
            companyCode) { from,to,code,context->
            onetimeDocService.processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(code), from, to, maxResult)
        }
    }
    @PostMapping("/CTLO040")
    fun postingCTLO040(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode,
        @RequestParam(required = false) maxResult: Int = DEFAULT_MAX_RESULT,
        @RequestParam(required = false) test: Boolean = false
    ) : ResponseEntity<Any>  {
        return executeSafely(
            DocumentTemplateCode.ONETIME_INVENTORY_DISPOSED_GRADE_B,
            startDateTime,
            endDateTime,
            timezone,
            companyCode) { from,to,code,context->
            onetimeDocService.processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(code), from, to, maxResult)
        }
    }
    //------------------------------------------------------------------------------------------------------------------
    //                                     일시불 교환 관련
    //------------------------------------------------------------------------------------------------------------------
    @PostMapping("/CTRP010")
    fun postingCTRP010(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode,
        @RequestParam(required = false) maxResult: Int = DEFAULT_MAX_RESULT,
        @RequestParam(required = false) test: Boolean = false
    ) : ResponseEntity<Any>  {
        return executeSafely(
            DocumentTemplateCode.ONETIME_REPLACEMENT_RECEIVED,
            startDateTime,
            endDateTime,
            timezone,
            companyCode) { from,to,code,context->
            onetimeDocService.processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(code), from, to, maxResult)
        }
    }
    @PostMapping("/CTRP020")
    fun postingCTRP020(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode,
        @RequestParam(required = false) maxResult: Int = DEFAULT_MAX_RESULT,
        @RequestParam(required = false) test: Boolean = false
    ) : ResponseEntity<Any>  {
        return executeSafely(
            DocumentTemplateCode.ONETIME_REPLACEMENT_PRODUCT_SHIPPED,
            startDateTime,
            endDateTime,
            timezone,
            companyCode) { from,to,code,context->
            onetimeDocService.processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(code), from, to, maxResult)
        }
    }
    @PostMapping("/CTRP030")
    fun postingCTRP030(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode,
        @RequestParam(required = false) maxResult: Int = DEFAULT_MAX_RESULT,
        @RequestParam(required = false) test: Boolean = false
    ) : ResponseEntity<Any>  {
        return executeSafely(
            DocumentTemplateCode.ONETIME_REPLACEMENT_INSTALLATION_COMPLETED,
            startDateTime,
            endDateTime,
            timezone,
            companyCode) { from,to,code,context->
            onetimeDocService.processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(code), from, to, maxResult)
        }
    }
    //------------------------------------------------------------------------------------------------------------------
    //                                     일시불 AS 관련
    //------------------------------------------------------------------------------------------------------------------
    @PostMapping("/CTAS010")
    fun postingCTAS010(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode,
        @RequestParam(required = false) maxResult: Int = DEFAULT_MAX_RESULT,
        @RequestParam(required = false) test: Boolean = false
    ) : ResponseEntity<Any>  {
        return executeSafely(
            DocumentTemplateCode.ONETIME_AS_REPAIR,
            startDateTime,
            endDateTime,
            timezone,
            companyCode) { from,to,code,context->
            onetimeDocService.processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(code), from, to, maxResult)
        }
    }
    @PostMapping("/CTAS020")
    fun postingCTAS020(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode,
        @RequestParam(required = false) maxResult: Int = DEFAULT_MAX_RESULT,
        @RequestParam(required = false) test: Boolean = false
    ) : ResponseEntity<Any>  {
        return executeSafely(
            DocumentTemplateCode.ONETIME_AS_RELOCATION,
            startDateTime,
            endDateTime,
            timezone,
            companyCode) { from,to,code,context->
            onetimeDocService.processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(code), from, to, maxResult)
        }
    }
    @PostMapping("/CTAS030")
    fun postingCTAS030(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode,
        @RequestParam(required = false) maxResult: Int = DEFAULT_MAX_RESULT,
        @RequestParam(required = false) test: Boolean = false
    ) : ResponseEntity<Any>  {
        return executeSafely(
            DocumentTemplateCode.ONETIME_AS_REINSTALL,
            startDateTime,
            endDateTime,
            timezone,
            companyCode) { from,to,code,context->
            onetimeDocService.processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(code), from, to, maxResult)
        }
    }
    @PostMapping("/CTAS040")
    fun postingCTAS040(
        @RequestParam("startDateTime") startDateTime: LocalDateTime,
        @RequestParam("endDateTime") endDateTime: LocalDateTime,
        @RequestParam("timezone", required = false, defaultValue = "America/Chicago") timezone: String,
        @RequestParam("companyCode", required = false, defaultValue = "N200") companyCode: CompanyCode,
        @RequestParam(required = false) maxResult: Int = DEFAULT_MAX_RESULT,
        @RequestParam(required = false) test: Boolean = false
    ) : ResponseEntity<Any>  {
        return executeSafely(
            DocumentTemplateCode.ONETIME_AS_DISMANTLING,
            startDateTime,
            endDateTime,
            timezone,
            companyCode) { from,to,code,context->
            onetimeDocService.processOnetimeBatchWithTemplateCodes(context, companyCode, listOf(code), from, to, maxResult)
        }
    }
}