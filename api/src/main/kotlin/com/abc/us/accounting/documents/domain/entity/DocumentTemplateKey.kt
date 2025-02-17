package com.abc.us.accounting.documents.domain.entity

import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.documents.domain.type.DocumentTemplateCode
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import org.hibernate.annotations.Comment

@Embeddable
class DocumentTemplateKey(
    @Comment("회사코드")
    @Column(name = "company_code")
    @Enumerated(EnumType.STRING)
    val companyCode: CompanyCode,

    @Column(name = "doc_template_code", nullable = false)
    @Enumerated(EnumType.STRING)
    val docTemplateCode: DocumentTemplateCode,
){
    companion object{
        fun of (companyCode: CompanyCode, code: DocumentTemplateCode):DocumentTemplateKey {
            return DocumentTemplateKey(companyCode, code)
        }
    }

    override fun toString(): String {
        return this.javaClass.simpleName + "{" +
                " companyCode='" + companyCode + '\'' +
                " docTemplateCode='" + docTemplateCode + '\'' +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DocumentTemplateKey) return false

        return this.companyCode == other.companyCode && this.docTemplateCode == other.docTemplateCode
    }

    override fun hashCode(): Int {
        var result = companyCode.hashCode()
        result = 31 * result + docTemplateCode.hashCode()
        return result
    }
}
