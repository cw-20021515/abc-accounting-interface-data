package com.abc.us.accounting.payouts.domain.entity

import com.fasterxml.jackson.databind.JsonNode
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.annotations.Type
import org.hibernate.type.YesNoConverter
import java.time.OffsetDateTime

@Entity
@Table(name = "employee")
class Employee(

    @Id
    @Column(name = "id", nullable = false, length = 255)
    @Comment("직원 사번")
    val id: String,

    @Column(name = "company_code", nullable = false, length = 255)
    @Comment("회사코드")
    val companyCode: String,

    @Column(name = "department_id", length = 255)
    @Comment("부서 id")
    val departmentId: String? = null,

    @Column(name = "role_code", length = 255)
    @Comment("직책 코드")
    val roleCode: String? = null,

    @Column(name = "role_name", length = 255)
    @Comment("직책 이름(팀장,팀원,파트장 등)")
    val roleName: String? = null,

    @Column(name = "grade_code", length = 255)
    @Comment("직급 코드")
    val gradeCode: String? = null,

    @Column(name = "grade_name", length = 255)
    @Comment("직급 이름(대리,과장,차장,부장)")
    val gradeName: String? = null,

    @Column(name = "status", length = 255)
    @Comment("직원 상태(재직중 | 휴직중 | 퇴사)")
    val status: String? = null,

    @Column(name = "first_name", length = 255)
    @Comment("이름")
    val firstName: String? = null,

    @Column(name = "middle_name", length = 255)
    @Comment("middle name")
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

    @Column(name = "address_jsonb", columnDefinition = "jsonb")
    @Type(JsonBinaryType::class)
    @Comment("주소")
    val addressJsonb: MutableMap<String,Any> = mutableMapOf(),

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
    @Comment("비고")
    val remark: String? = null
)
