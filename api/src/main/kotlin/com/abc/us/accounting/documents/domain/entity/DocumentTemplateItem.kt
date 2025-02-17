package com.abc.us.accounting.documents.domain.entity

import com.abc.us.accounting.documents.domain.type.*
import jakarta.persistence.*
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.hibernate.annotations.Comment
import org.springframework.data.annotation.CreatedDate
import java.time.OffsetDateTime

@Entity
@Table(
    name = "document_template_item",
)
class DocumentTemplateItem (
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Comment("id")
    var id: Int? = null,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "company_code", column = Column(name = "company_code")),
        AttributeOverride(name = "doc_template_code", column = Column(name = "doc_template_code")),
    )
    val docTemplateKey: DocumentTemplateKey,

    @Comment("전표항목순서")
    @Column(name = "line_order", nullable = false)
    val lineOrder:Int,

    @Comment("차대구분")
    @Column(name = "account_side", nullable = false)
    @Enumerated(EnumType.STRING)
    val accountSide: AccountSide,

    @Comment("계정코드")
    @Column(name = "account_code", nullable = false)
    val accountCode: String,

    @Comment("참조전표템플릿ID")
    @Column(name = "ref_doc_template_code")
//    @Convert(converter = DocumentTemplateCodeListConverter::class)
    @Enumerated(EnumType.STRING)
    val refDocTemplateCode: DocumentTemplateCode? = null,


//    @OneToMany(mappedBy = "documentTemplateItem", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
//    val refDocTemplateCodes: MutableList<DocumentTemplateItemRef> = mutableListOf(),

    @Comment("한글 텍스트")
    @Column(name = "item_text_kor", nullable = false)
    val korText:String?,

    @Comment("영문 텍스트")
    @Column(name = "item_text_eng", nullable = false)
    val engText: String?,

    @Comment("필수여부")
    @Column(name="requirement_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val fieldRequirement: FieldRequirement,

    @Comment("비용센터")
    @Column(name="cost_center", nullable = false)
    val costCenter:String,

    @Comment("손익센터")
    @Column(name="profit_center")
    val profitCenter:String,

    @Comment("세그먼트")
    @Column(name="segment")
    val segment:String,

    @Comment("프로젝트")
    @Column(name="project")
    val project:String,

    @Comment("생성 일시")
    @CreatedDate
    var createTime: OffsetDateTime = OffsetDateTime.now(),

    ) {

    override fun toString(): String {
        return this.javaClass.simpleName + "{" +
            "id='" + id + '\'' +
            ", companyCode='" + docTemplateKey.companyCode + '\'' +
            ", docTemplateCode='" + docTemplateKey.docTemplateCode + '\'' +
            ", lineOrder=" + lineOrder + '\'' +
            ", accountSide=" + accountSide + '\'' +
            ", accountCode='" + accountCode + '\'' +
            ", refDocTemplateCode='" + refDocTemplateCode + '\'' +
            ", textKor='" + korText + '\'' +
            ", engText='" + engText + '\'' +
            ", requirementType=" + fieldRequirement + '\'' +
            ", costCenter=" + costCenter + '\'' +
            ", profitCenter=" + profitCenter + '\'' +
            ", segment=" + segment + '\'' +
            ", project=" + project + '\'' +
            ", createTime=" + createTime + '\'' +
            '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DocumentTemplateItem) return false

        return EqualsBuilder()
            .append(id, other.id)
            .append(docTemplateKey, other.docTemplateKey)
            .append(lineOrder, other.lineOrder)
            .append(accountSide, other.accountSide)
            .append(accountCode, other.accountCode)
            .append(refDocTemplateCode, other.refDocTemplateCode)
            .append(korText, other.korText)
            .append(engText, other.engText)
            .append(fieldRequirement, other.fieldRequirement)
            .append(costCenter, other.costCenter)
            .append(profitCenter, other.profitCenter)
            .append(segment, other.segment)
            .append(project, other.project)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(id)
            .append(docTemplateKey)
            .append(lineOrder)
            .append(accountSide)
            .append(accountCode)
            .append(refDocTemplateCode)
            .append(korText)
            .append(engText)
            .append(fieldRequirement)
            .append(costCenter)
            .append(profitCenter)
            .append(segment)
            .append(project)
            .toHashCode()
    }

}

//
//@Entity
//@Table(name = "document_template_item_ref")
//class DocumentTemplateItemRef(
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    val id: Long = 0,
//
//    @ManyToOne
//    @JoinColumn(name = "doc_template_item_id", nullable = false)
//    val documentTemplateItem: DocumentTemplateItem,
//
//    @Column(name = "ref_doc_template_code", nullable = false)
//    val refDocTemplateCode: String
//)