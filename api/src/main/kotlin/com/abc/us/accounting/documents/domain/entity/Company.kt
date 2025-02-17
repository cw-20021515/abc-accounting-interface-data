package com.abc.us.accounting.documents.domain.entity

import com.abc.us.accounting.commons.domain.type.*
import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.type.*
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.type.YesNoConverter
import java.time.LocalDate
import java.time.OffsetDateTime

@Entity
@Table (name = "company")
class Company (
    @Id
    @Comment("회사코드")
    @Column(name = "code", columnDefinition = "varchar(4)")
    @Enumerated(EnumType.STRING)
    val code: CompanyCode,

    @Comment("회사명")
    @Column(name = "name")
    val name: String,

    @Comment("회사 설명")
    @Column(name = "description")
    val description: String,

    @Comment("국가")
    @Column(name = "country")
    @Convert(converter = CountryCodeConverter::class)
    val country: CountryCode,

    @Comment("통화")
    @Column(name = "currency")
    @Convert(converter = CurrencyCodeConverter::class)
    val currency: CurrencyCode,

    @Comment("회사 시간대")
    @Column(name = "timezone")
    @Convert(converter = TimeZoneCodeConverter::class)
    val timeZone: TimeZoneCode,

    @Embedded
    @Comment("회계시작월")
    //@Column(name = "fiscal_rule")
    @AttributeOverrides(
        AttributeOverride(
            name = "startMonth",
            column = Column(name = "fiscal_start_month")
        )
    )
    val fiscalRule: FiscalRule,     // 회계 시작을 언제로 볼지


    @Comment("사용여부")
    @Column(name = "is_active")
    @Convert(converter = YesNoConverter::class)
    val isActive:Boolean,

    @Comment("생성일시")
    @Column(name = "create_time")
    val createTime: OffsetDateTime = OffsetDateTime.now(),

    @Comment("생성자")
    @Column(name = "created_by")
    val createdBy: String = Constants.APP_NAME,

    @Comment("수정일시")
    @Column(name = "update_time")
    val updateTime: OffsetDateTime = OffsetDateTime.now(),

    @Comment("수정자")
    @Column(name = "updated_by")
    val updatedBy: String = Constants.APP_NAME
){
    fun companyCode(): CompanyCode
        = code

    fun fiscalYearMonth(localDate: LocalDate = LocalDate.now()): FiscalYearMonth {
        return FiscalYearMonth.from(localDate, fiscalRule)
    }

    fun fiscalYear(localDate: LocalDate = LocalDate.now()): Int {
        return fiscalRule.getFiscalYear(localDate)
    }

    fun fiscalMonth(localDate: LocalDate = LocalDate.now()): Int {
        return fiscalRule.getFiscalMonth(localDate)
    }

    override fun toString(): String {
        return this.javaClass.simpleName + "{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", country=" + country + '\'' +
                ", currency=" + currency + '\'' +
                ", timeZone=" + timeZone + '\'' +
                ", fiscalRule=" + fiscalRule + '\'' +
                ", isActive=" + isActive + '\'' +
                ", createTime=" + createTime + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", updateTime=" + updateTime + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        other as Company

        if (code != other.code) return false

        return true
    }

    override fun hashCode(): Int {
        return code.hashCode()
    }
}