package com.abc.us.accounting.documents.fixtures

import com.abc.us.accounting.config.SpringContext
import com.abc.us.accounting.documents.domain.type.AccountType
import com.abc.us.accounting.documents.domain.type.DocumentAttributeType
import com.abc.us.accounting.documents.domain.type.FieldRequirement
import com.abc.us.accounting.documents.model.DocumentItemAttributeRequest
import com.abc.us.accounting.documents.service.AccountServiceable
import com.abc.us.accounting.documents.service.DocumentMasterServiceable
import com.github.javafaker.Faker
import mu.KotlinLogging

object CreateDocumentItemAttributeRequestFixture {
    private val logger = KotlinLogging.logger {}
    private val faker = Faker()

    private var accountService: AccountServiceable
    private var documentMasterService: DocumentMasterServiceable
    init {
        // AccountRepository 빈을 직접 주입
        SpringContext.getBean(AccountServiceable::class.java).let {
            accountService = it
        }
        // DocumentMasterService 빈을 직접 주입
        SpringContext.getBean(DocumentMasterServiceable::class.java).let {
            documentMasterService = it
        }
    }

    fun generates(
        count: Int,
        accountType: AccountType,
        attributeType: DocumentAttributeType? = null,
        attributeValue: String? = null
    ): List<DocumentItemAttributeRequest> {

        val attributeMasters = documentMasterService.getAllByAccountTypeIn(listOf( accountType) )
        val requiredTypes = attributeMasters.filter { it.fieldRequirement == FieldRequirement.REQUIRED }
        val optionalTypes = attributeMasters.filter { it.fieldRequirement == FieldRequirement.OPTIONAL }

        val requiredAttributeTypes = requiredTypes.map { it.attributeType }
        val optionalAttributeTypes = optionalTypes.map { it.attributeType }

        val attributeTypes = ( requiredTypes.map { it.attributeType }.shuffled() ) + ( optionalTypes.map { it.attributeType }.shuffled() )
        if ( attributeType == null ) {

            val modifiedCount = if (count > attributeTypes.size) attributeTypes.size else count
            return (1..modifiedCount).map {
                generate(
                    accountType = accountType,
                    attributeType = attributeTypes[it-1],
                    attributeValue = DocumentItemAttributeFixture.randomAttributeValue(attributeTypes[it-1]),
                )
            }
        }

        val modifiedCount = if (count > attributeTypes.size) attributeTypes.size else count

        return (1..modifiedCount).map {
            val index = it-1
            val candidateAttributeType = attributeTypes[index]
            if ( candidateAttributeType == attributeType ) {
                generate(
                    accountType = accountType,
                    attributeType = attributeType,
                    attributeValue = attributeValue ?: DocumentItemAttributeFixture.randomAttributeValue(attributeType),
                )
            } else {
                val attributeType = attributeTypes[index]

                if ( attributeType == null ) {
                    logger.error("attribute type $attributeType not found by index: $index, attributeTypes: $attributeTypes")
                }

                generate(
                    accountType = accountType,
                    attributeType = attributeType,
                    attributeValue = DocumentItemAttributeFixture.randomAttributeValue(attributeType),
                )
            }
        }
    }

    fun generate(
        accountType: AccountType,
        attributeType: DocumentAttributeType?= null,
        attributeValue: String?= null,
    ): DocumentItemAttributeRequest {
        val attributeType = attributeType ?: DocumentAttributeType.entries.random()

        return DocumentItemAttributeRequest(
            attributeType = attributeType,
            attributeValue = attributeValue ?: DocumentItemAttributeFixture.randomAttributeValue(attributeType),
        )
    }


}