package com.abc.us.accounting.qbo.domain.entity

import com.abc.us.accounting.documents.domain.entity.FiscalYearMonth
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.type.YesNoConverter
import java.time.OffsetDateTime

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table (name = "qbo_company")
data class QboCompany (

    @Id
    @Comment("사업장 ID")
    val code: String,

    @Comment("사업장 이름")
    val name: String,

    @Comment("설명")
    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Comment("사업장 소속 국가")
    val country: String,

    @Comment("사업장에서 사용하는 통화")
    val currency: String = "USD",

    @Comment("영문 회사명")
    val engName: String? = null,

    @Comment("국문 회사명")
    val korName: String? = null,

//    @Comment("회사 이메일")
//    val email: String? = null,

//    @Comment("첫번째 phone 번호" )
//    val primaryPhone: String? = null,

//    @Comment("대체 phone 번호" )
//    var alternatePhone : String?=null,

//    @Comment("web site 주소" )
//    var webAddr : String?=null,

//    @Comment("회사 시간대")
//    @Column(name = "timezone")
//    @Convert(converter = TimeZoneCodeConverter::class)
//    val timeZone: TimeZoneCode,

//    @Embedded
//    var location : EmbeddableLocation? = null,

//    @Comment("EIN 세금 ID 또는 고용주 식별 번호 (미국 기준) " )
//    var taxId : String?=null,

//    @Comment("부가가치세 번호" )
//    var VATNumber : String?=null,

//    @Comment("사업자 등록번호" )
//    var businessRegistrationNumber : String?=null,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "fiscal_year", column = Column(name = "fiscal_year")),
        AttributeOverride(name = "fiscal_month", column = Column(name = "fiscal_month"))
    )
    val fiscalYearMonth: FiscalYearMonth,

//    @Comment("세금 신고 주기 (월별, 분기별 등)" )
//    var taxFilingFrequency : Int?=null,


    @Comment("생성 일시")
    @CreationTimestamp
    val createTime: OffsetDateTime = OffsetDateTime.now(),

    @Comment("수정 일시")
    @CreationTimestamp
    val updateTime: OffsetDateTime = OffsetDateTime.now(),

    @Comment("Active 여부")
    @Convert(converter = YesNoConverter::class)
    val isActive: Boolean = true,

    @Transient
    // Map<activeProfile,Credential>
    var credentials : MutableMap<String,QboCredential> = mutableMapOf()
)