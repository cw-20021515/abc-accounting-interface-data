package com.abc.us.accounting.documents.domain.entity

import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.supports.utils.Range
import com.abc.us.accounting.supports.utils.toStringByReflection
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.*
import kotlinx.serialization.json.Json
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.hibernate.annotations.Type
import org.hibernate.type.YesNoConverter
import org.postgresql.util.PGobject
import java.math.BigDecimal

@Entity
@Table(name = "document_approval_rule")
class DocumentApprovalRule (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name="name", nullable = false)
    val name: String,

    @Column(name="description", nullable = false)
    val description: String,

    @Column(name="priority", nullable = false)
    val priority: Int,

    @Column(name="company_code")
    @Enumerated(EnumType.STRING)
    val companyCode: CompanyCode? = null,

    @Type(JsonBinaryType::class)
    @Column(name="conditions", nullable = false, columnDefinition = "jsonb")
    @Convert(converter = RuleJsonConverter::class)
    val conditions: List<ApprovalRuleCondition>,

    @Column(name="requires_approval", nullable = false)
    @Convert(converter = YesNoConverter::class)
    val requiresApproval: Boolean,

    @Column(name="is_active")
    @Convert(converter = YesNoConverter::class)
    val isActive: Boolean,
){

    fun toJsonMessage(): String {
        val map = linkedMapOf(
            "ruleId" to this.id,
            "name" to this.name,
            "description" to this.description,
            "companyCode" to this.companyCode,
            "conditions" to this.conditions,
        )
        val json = ObjectMapper().writeValueAsString(map)
        return json
    }

    override fun toString(): String {
        return toStringByReflection()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is DocumentApprovalRule) return false

        if (name != other.name) return false
        if (companyCode != other.companyCode) return false
        if (conditions != other.conditions) return false

        return true
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(name)
            .append(description)
            .append(companyCode)
            .append(conditions)
            .toHashCode()
    }
}

enum class ApprovalRuleResultCode {
    OK,
    BIZ_SYSTEM_TYPE,
    DOC_TEMPLATE_CODE,
    DOCUMENT_TYPE,
    ACCOUNT_TYPE,
    ACCOUNT_CODE_RANGE,
    AMOUNT_RANGE,
    DEPARTMENT,
    COST_CENTER,
    USER,
}

// 규칙 조건 데이터 클래스
@JsonInclude(JsonInclude.Include.NON_NULL) // null 값 제외
data class ApprovalRuleCondition(
    val bizSystemTypes: List<String>? = null,
    val docTemplateCodes: List<String>? = null,
    val documentTypes: List<String>? = null,
    val accountTypes: List<String>? = null,
    val accountCodeRange: Range<String>? = null,
    val amountRange: Range<BigDecimal>? = null,
    val departments: List<String>? = null,
    val costCenters: List<String>? = null,
    val users: List<String>? = null,
){
    override fun toString(): String {
        return toStringByReflection()
    }
}

// JSON 변환기
@Converter
class RuleJsonConverter : AttributeConverter<List<ApprovalRuleCondition>, Any> {
    private val objectMapper = ObjectMapper().apply {
        registerModule(KotlinModule.Builder().build())
        registerModule(JavaTimeModule())
    }

    private val typeReference = object : TypeReference<List<ApprovalRuleCondition>>() {}

    override fun convertToDatabaseColumn(attribute: List<ApprovalRuleCondition>): Any = {
        PGobject().apply {
            type = "jsonb"
            value = objectMapper.writeValueAsString(attribute)
        }
    }


    override fun convertToEntityAttribute(dbData: Any): List<ApprovalRuleCondition> =
        objectMapper.readValue(
            when (dbData) {
                is PGobject -> dbData.value
                else -> dbData.toString()
            }
            , typeReference
        )
}
