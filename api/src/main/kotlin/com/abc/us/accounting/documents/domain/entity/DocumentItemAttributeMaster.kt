package com.abc.us.accounting.documents.domain.entity

import com.abc.us.accounting.documents.domain.type.*
import jakarta.persistence.*
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.hibernate.annotations.Comment
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.OffsetDateTime
import java.time.Year


@Entity
@Table (
    name = "document_item_attribute_master",
    uniqueConstraints = [
        UniqueConstraint(
            name = "unique_account_type_check",
            columnNames = ["code", "attribute_category", "attribute_type", "field_requirement"]
        )
    ]
)
class DocumentItemAttributeMaster (
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int? = null,

    @Comment("계정유형코드")
    @Column(name="account_type", nullable = false)
    @Convert(converter = AccountTypeNameConverter::class)
    val accountType: AccountType,

    @Comment("부가 카테고리")
    @Column(name="attribute_category", nullable = false)
    @Convert(converter = DocumentAttributeCategoryConverter::class)
    val attributeCategory: DocumentAttributeCategory,

    @Comment("부가 유형")
    @Column(name="attribute_type", nullable = false)
    @Convert(converter = DocumentAttributeTypeConverter::class)
    val attributeType: DocumentAttributeType,

    @Comment("부가 유형 요구조건")
    @Convert(converter = FieldRequirementNameConverter::class)
    @Column(name="field_requirement", nullable = false)
    val fieldRequirement: FieldRequirement,

    @Comment("부가 유형 요구조건")
    @Column(name="condition_logic", nullable = true)
    val conditionLogic: String?= null,

    @Comment("Active 여부")
    @Convert(converter = YesNoConverter::class)
    val isActive: Boolean,

    @Comment("생성 일시")
    @CreatedDate
    var createTime: OffsetDateTime = OffsetDateTime.now(),

    @Comment("수정 일시")
    @LastModifiedDate
    var updateTime: OffsetDateTime? = OffsetDateTime.now()
) {

    @PrePersist
    @PreUpdate
    fun prePersist() {
        updateTime = OffsetDateTime.now()
    }


    override fun toString(): String {
        Year.MAX_VALUE
        return  this.javaClass.simpleName+ "{" +
                ", accountCode='" + accountType + '\'' +
                ", extraCategory='" + attributeCategory + '\'' +
                ", extraType=" + attributeType + '\'' +
                ", requirementType=" + fieldRequirement + '\'' +
                ", isActive=" + isActive + '\'' +
                ", createTime=" + createTime + '\'' +
                ", updateTime=" + updateTime + '\'' +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is DocumentItemAttributeMaster) return false

        return EqualsBuilder()
            .append(accountType, other.accountType)
            .append(attributeCategory, other.attributeCategory)
            .append(attributeType, other.attributeType)
            .append(fieldRequirement, other.fieldRequirement)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(accountType)
            .append(attributeCategory)
            .append(attributeType)
            .append(fieldRequirement)
            .toHashCode()
    }
}