package com.abc.us.accounting.rentals.onetime.service.v2

import com.abc.us.accounting.config.OnetimeConfig
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.type.DocumentTemplateCode
import com.abc.us.accounting.documents.domain.type.SalesType
import com.abc.us.accounting.documents.model.CreateDocumentRequest
import com.abc.us.accounting.documents.model.DocumentResult
import com.abc.us.accounting.documents.model.DocumentServiceContext
import com.abc.us.accounting.documents.service.CompanyService
import com.abc.us.accounting.documents.service.DocumentMasterService
import com.abc.us.accounting.documents.service.DocumentServiceable
import com.abc.us.accounting.documents.service.DocumentTemplateServiceable
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.OffsetDateTime


interface OnetimeProcessServiceable {
    fun getSupportedProcessRules():List<OnetimeProcessRule>

    fun processOnetimeBatch (context: DocumentServiceContext, companyCode: CompanyCode, startTime: OffsetDateTime, endTime: OffsetDateTime = OffsetDateTime.now() ) :List<DocumentResult>

    fun processOnetimeBatchWithTemplateCodes(context:DocumentServiceContext, companyCode: CompanyCode, templateCodes: List<DocumentTemplateCode>, startTime:OffsetDateTime, endTime:OffsetDateTime = OffsetDateTime.now()):List<DocumentResult>

}

@Service
class OnetimeProcessService(
    private val onetimeConfig: OnetimeConfig,
    private val documentServiceable: DocumentServiceable,
    private val documentTemplateServiceable: DocumentTemplateServiceable,
    private val onetimeProcessRules: MutableList<OnetimeProcessRule> = mutableListOf(),
): OnetimeProcessServiceable {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun getSupportedProcessRules():List<OnetimeProcessRule> {
        return onetimeProcessRules
    }

    override fun processOnetimeBatch(context:DocumentServiceContext, companyCode:CompanyCode, startTime:OffsetDateTime, endTime:OffsetDateTime):List<DocumentResult> {
        val templateCodes = DocumentTemplateCode.findAllBySalesType(SalesType.ONETIME).sortedBy { it.ordinal }

        return processOnetimeBatchWithTemplateCodes(context,companyCode,templateCodes, startTime, endTime)
    }

    override fun processOnetimeBatchWithTemplateCodes(context:DocumentServiceContext, companyCode: CompanyCode, templateCodes: List<DocumentTemplateCode>, startTime:OffsetDateTime, endTime:OffsetDateTime):List<DocumentResult> {
        logger.info("processOnetimeBatchWithTemplateCodes: companyCode:$companyCode, templateCodes:${templateCodes}, startTime: $startTime, endTime: $endTime, maxResult: $context.maxResult")

        val docTemplates = documentTemplateServiceable.findDocTemplates(companyCode, templateCodes)
        require(docTemplates.isNotEmpty()) { "docTemplates is empty by templateCodes: $templateCodes" }
        logger.info("processOnetimeBatchWithTemplateCodes: docTemplates:${docTemplates.size}, rules:${onetimeProcessRules.size}")

        val requests = onetimeProcessRules
            .map { rule -> rule.process(context, companyCode, docTemplates, startTime, endTime) }
            .flatten()
            .take(context.maxResult)


        val postingRequests = requests.filterIsInstance<CreateDocumentRequest>()
        if ( postingRequests.isNotEmpty()) {
            return documentServiceable.posting(context, postingRequests)
        }
        logger.info("processOnetimeBatchWithTemplateCodes: posting ignored by posting request is empty, companyCode:$companyCode, templateCodes:${templateCodes}, startTime: $startTime, endTime: $endTime, maxResult: $context.maxResult")
        return emptyList()
    }

}