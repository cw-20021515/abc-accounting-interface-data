package com.abc.us.accounting.payouts.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.Comment
import java.time.OffsetDateTime

@Entity
@Table(name = "segment")
class Segment(

    @Id
    @Column(name = "id", nullable = false, length = 255)
    @Comment("segment ID")
    val id: String,

    @Column(name = "company_code", nullable = false, length = 255)
    @Comment("회사코드")
    val companyCode: String,

    @Column(name = "code", nullable = false, length = 255)
    @Comment("segment 식별 코드")
    val code: String,

    @Column(name = "description", nullable = false, length = 255)
    @Comment("상세 설명")
    val description: String,

    @Column(name = "valid_from_time", columnDefinition = "TIMESTAMP")
    @Comment("유효 시작일")
    val validFromTime: OffsetDateTime? = null,

    @Column(name = "valid_to_time", columnDefinition = "TIMESTAMP")
    @Comment("유효 종료일")
    val validToTime: OffsetDateTime? = null,

    @Column(name = "create_time", columnDefinition = "TIMESTAMP")
    @Comment("생성 날짜")
    val createTime: OffsetDateTime? = null,

    @Column(name = "update_time", columnDefinition = "TIMESTAMP")
    @Comment("갱신 날짜")
    val updateTime: OffsetDateTime? = null
)
