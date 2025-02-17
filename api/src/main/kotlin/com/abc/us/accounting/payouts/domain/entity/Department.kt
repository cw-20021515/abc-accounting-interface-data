package com.abc.us.accounting.payouts.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.Comment
import java.time.OffsetDateTime

@Entity
@Table(name = "department")
class Department(

    @Id
    @Column(name = "id", nullable = false, length = 255)
    @Comment("부서 ID")
    val id: String,

    @Column(name = "company_code", nullable = false, length = 255)
    @Comment("부서가 속한 회사코드")
    val companyCode: String,

    @Column(name = "code", nullable = false, length = 255)
    @Comment("식별 코드")
    val code: String,

    @Column(name = "parent_id", length = 255)
    @Comment("상위 부서 ID")
    val parentId: String? = null,

    @Column(name = "kor_name", nullable = false, length = 255)
    @Comment("부서 이름")
    val korName: String,

    @Column(name = "eng_name", nullable = false, length = 255)
    @Comment("부서 이름(영문)")
    val engName: String,

    @Column(name = "level")
    @Comment("조직 레벨")
    val level: Int? = null,

    @Column(name = "description", nullable = false, length = 255)
    @Comment("부서 설명")
    val description: String,

    @Column(name = "is_active", length = 1)
    @Comment("활성화 여부")
    val isActive: String = "Y",

    @Column(name = "create_time", columnDefinition = "TIMESTAMP")
    @Comment("생성 날짜")
    val createTime: OffsetDateTime? = null,

    @Column(name = "update_time", columnDefinition = "TIMESTAMP")
    @Comment("갱신 날짜")
    val updateTime: OffsetDateTime? = null
)
