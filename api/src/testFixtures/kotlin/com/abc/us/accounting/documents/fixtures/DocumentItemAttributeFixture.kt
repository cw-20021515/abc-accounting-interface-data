package com.abc.us.accounting.documents.fixtures

import com.abc.us.accounting.documents.domain.entity.DocumentItemAttribute
import com.abc.us.accounting.documents.domain.entity.DocumentItemAttributeId
import com.abc.us.accounting.documents.domain.type.DocumentAttributeType
import com.abc.us.accounting.rentals.master.domain.type.MaterialType
import com.github.javafaker.Faker

// 정합성은 확인 안함
// 랜덤으로 생성할 목적
object DocumentItemAttributeFixture {
    private val faker = Faker()
    fun createDocumentItemAttributes(
        count: Int,
        docItemId: String? = null,
        attributeType: DocumentAttributeType? = null,
        attributeValue: String? = null,
    ): List<DocumentItemAttribute> {


        val attributeTypes:List<DocumentAttributeType> = DocumentAttributeType.entries.shuffled()
        val modifiedCount = if (count > attributeTypes.size) attributeTypes.size else count

        return (1..modifiedCount).map {
            createDocumentItemAttribute(
                docItemId = docItemId ?: faker.number().digits(10),
                attributeType = attributeType ?: attributeTypes[it-1],
                extraValue = attributeValue ?: randomAttributeValue(attributeTypes[it-1]),
            )
        }
    }

    fun createDocumentItemAttribute(
        docItemId: String,
        attributeType:DocumentAttributeType?= null,
        extraValue: String?= null,
    ): DocumentItemAttribute {
        val category = attributeType ?: DocumentAttributeType.entries.random()
        val attributeId = DocumentItemAttributeId(
            docItemId = docItemId,
            attributeType = category,
        )
        return DocumentItemAttribute(
            attributeId = attributeId,
            value = extraValue ?: randomAttributeValue(category),
        )
    }


    fun randomAttributeValue(attributeType: DocumentAttributeType): String {
        return when (attributeType) {
            DocumentAttributeType.COST_CENTER -> faker.number().digits(10)
            DocumentAttributeType.PROFIT_CENTER -> faker.number().digits(10)
            DocumentAttributeType.BUSINESS_AREA -> faker.number().digits(10)
            DocumentAttributeType.SEGMENT -> faker.lorem().sentence()
            DocumentAttributeType.PROJECT -> faker.lorem().sentence()
            DocumentAttributeType.CUSTOMER_ID -> faker.number().digits(10)
            DocumentAttributeType.VENDOR_ID -> faker.number().digits(10)

            DocumentAttributeType.ORDER_ID -> faker.lorem().sentence()
            DocumentAttributeType.ORDER_ITEM_ID -> faker.lorem().sentence()
            DocumentAttributeType.MATERIAL_ID -> faker.lorem().sentence()
            DocumentAttributeType.MATERIAL_TYPE -> randomMaterialType()
            else -> faker.number().digits(10)
        }
    }


    fun randomMaterialType(): String {
        return MaterialType.values().random().name
    }
}