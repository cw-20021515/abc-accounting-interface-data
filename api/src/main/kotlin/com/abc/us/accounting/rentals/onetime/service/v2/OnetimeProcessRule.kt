package com.abc.us.accounting.rentals.onetime.service.v2

import com.abc.us.accounting.documents.domain.entity.DocumentTemplate
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.type.DocumentTemplateCode
import com.abc.us.accounting.documents.model.DocumentServiceContext
import com.abc.us.accounting.documents.model.HashableDocumentRequest
import java.time.OffsetDateTime

/**
 * 일시불 전표처리 규칙
 */
interface OnetimeProcessRule {
    val name:String
    val supportedTemplateCodes: List<DocumentTemplateCode>

    fun process(context: DocumentServiceContext, companyCode: CompanyCode, docTemplates: List<DocumentTemplate>, startTime: OffsetDateTime, endTime: OffsetDateTime = OffsetDateTime.now()): List<HashableDocumentRequest>


}