package com.abc.us.accounting.qbo.domain.entity

import com.abc.us.accounting.qbo.domain.entity.key.QboJournalEntryKey
import com.abc.us.accounting.qbo.domain.entity.key.QboVendorKey
import com.abc.us.accounting.qbo.domain.type.ClassType
import com.abc.us.accounting.qbo.domain.type.VendorType
import com.fasterxml.jackson.annotation.JsonInclude
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Type
import org.hibernate.type.YesNoConverter
import java.time.OffsetDateTime

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
class QboVendor (

    @Id
    @EmbeddedId
    var key: QboVendorKey,

    @Comment("vendor type")
    val type: VendorType,

    @Type(JsonType::class)
    @Column(name = "submit_result")
    var submitResult: String?=null,

    @Comment("생성 시간")
    var createTime: OffsetDateTime = OffsetDateTime.now(),

    @Comment("갱신 시간")
    var updateTime: OffsetDateTime = OffsetDateTime.now(),

    @Comment("사용 여부")
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true

//    @Comment("회사 소속의 직원일 경우 고용 회사의 Code")
//    var employerCode: String? = null
//
//    @Comment("소속 부서ID")
//    var departmentCode: String? = null
//
//    @Comment("부서 이름")
//    var departmentName: String? = null
//
//    @Comment("화면에 표시될 공급업체 이름")
//    var displayName: String? = null
//
//    @Comment("회사 코드")
//    var companyCode: String? = null
//
//    @Comment("공급업체 회사 이름")
//    var companyName: String? = null
//
//    @Comment("공급업체 담당자의 타이틀")
//    var title: String? = null
//
//    @Comment("공급업체 담당자 이름")
//    var givenName: String? = null
//
//    @Comment("공급업체 담당자 중간 이름")
//    var middleName: String? = null
//
//    @Comment("공급업체 담당자 성")
//    var familyName: String? = null
//
//    @Comment("공급업체 담당자 이름 뒤에 오는 접미사(예 : Jr, Sr)")
//    var suffix: String? = null
//
//    @Comment("주요 연락처 번호")
//    var office: String? = null
//
//    @Comment("보조 연락처 번호")
//    var alternatePhone: String? = null
//
//    @Comment("휴대폰번호")
//    var mobile: String? = null
//
//    @Comment("팩스 번호")
//    var fax: String? = null
//
//    @Comment("주요 이메일")
//    var email: String? = null

//    @Comment("공급 업체 웹사이트 주소")
//    var webAddr: String? = null

//    @Comment("공급업체에 대한 결제 조건")
//    var terms: String? = null

//    @Comment("공급업체 계좌번호") //TODO : 별도로 확인 필요
//    var acctNum: String? = null
//
//    @Comment("공급업체 세금ID") //TODO : 별도로 확인 필요
//    var taxIdentifier: String? = null
//
//    @Comment("공급업체와 거래하는 통화") //TODO : 별도로 확인 필요
//    var currency: String? = null
//
//    @Comment("공급업체 사업자 번호") //TODO : 별도로 확인 필요
//    var businessNumber: String? = null

//    @Comment("등록 일시")
//    @CreationTimestamp
//    var createTime: OffsetDateTime? = null
//
//    @Comment("수정 일시")
//    @CreationTimestamp
//    var updateTime: OffsetDateTime? = null
//
//    @Comment("Active 여부")
//    @Convert(converter = YesNoConverter::class)
//    var isActive: Boolean = true
//
//    @Comment("공급 업체에 대한 메모 --> QBO 에서는 Memo")
//    var remark: String? = null
//
//    @Comment("공급 업체에 대한 메모 --> QBO 에서는 Memo")
//    var description: String? = null
)