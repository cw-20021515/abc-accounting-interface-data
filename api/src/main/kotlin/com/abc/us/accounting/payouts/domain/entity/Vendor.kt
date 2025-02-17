package com.abc.us.accounting.payouts.domain.entity

import com.abc.us.accounting.payouts.domain.type.VendorCategory
import com.fasterxml.jackson.databind.JsonNode
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.annotations.Type
import org.hibernate.type.YesNoConverter
import java.time.OffsetDateTime

@Entity
@Table(name = "vendor")
class Vendor(

    @Id
    @Column(name = "id", nullable = false, length = 255)
    @Comment("객체 식별 ID")
    val id: String,

    @Column(name = "company_code", nullable = false, length = 255)
    @Comment("회사코드")
    val companyCode: String,

    @Column(name = "vendor_id", nullable = false, length = 255)
    @Comment("vendor ID")
    val vendorId: String,

    @Column(name = "category", length = 255)
    @Comment("거래처 유형")
    @Enumerated(EnumType.STRING)
    val category: VendorCategory = VendorCategory.CORPORATION,

    @Column(name = "first_name", length = 255)
    @Comment("이름")
    val firstName: String? = null,

    @Column(name = "middle_name", length = 255)
    @Comment("vendor name")
    val middleName: String? = null,

    @Column(name = "last_name", length = 255)
    @Comment("성")
    val lastName: String? = null,

    @Column(name = "family_name", length = 255)
    @Comment("가족 이름")
    val familyName: String? = null,

    @Column(name = "name_suffix", length = 255)
    @Comment("이름 앞에 붙는 수식 (예 : sir)")
    val nameSuffix: String? = null,

    @Column(name = "phone", length = 255)
    @Comment("유선 연락처")
    val phone: String? = null,

    @Column(name = "mobile", length = 255)
    @Comment("휴대폰 정보(SP(개인사업자)일 경우의 개인 휴대폰 번호)")
    val mobile: String? = null,

    @Column(name = "fax", length = 255)
    @Comment("fax 번호")
    val fax: String? = null,

    @Column(name = "email", length = 255)
    @Comment("첫번째 이메일 주소")
    val email: String? = null,

    @Column(name = "web_addr", length = 255)
    @Comment("web site 주소")
    val webAddr: String? = null,

    @Column(name = "address_jsonb", columnDefinition = "jsonb")
    @Type(JsonBinaryType::class)
    @Comment("주소")
    val addressJsonb: MutableMap<String,Any> = mutableMapOf(),

    @Column(name = "description", length = 255)
    @Comment("vendor 상세 설명")
    val description: String? = null,

    @Column(name = "create_time", columnDefinition = "TIMESTAMP")
    @Comment("vendor 생성일시")
    val createTime: OffsetDateTime? = null,

    @Column(name = "update_time", columnDefinition = "TIMESTAMP")
    @Comment("vendor 갱신일시")
    val updateTime: OffsetDateTime? = null,

    @Comment("활성화 여부")
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true,

    @Column(name = "remark", length = 255)
    @Comment("추가 코멘트 작성용")
    val remark: String? = null
)
