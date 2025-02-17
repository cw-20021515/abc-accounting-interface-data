package com.abc.us.accounting.iface.domain.entity.logistics

import java.time.OffsetDateTime
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.type.YesNoConverter

/**
 * 에이징 범위 정보 테이블 (History)
 */
@Entity
@Table(name = "if_aging_range")
@Comment("에이징 범위 정보(History)")
class IfAgingRange(

    @Comment("고유 식별자")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long = 0L,

    @Comment("범위 고유 ID")
    @Column(name = "range_id", nullable = false)
    val rangeId: String,

    @Comment("에이징 범위를 나타내는 레이블")
    @Column(name = "label", nullable = false)
    val label: String,

    @Comment("에이징 범위 시작 일자")
    @Column(name = "min_aging_days", nullable = false)
    val minAgingDays: Int,

    @Comment("에이징 범위 종료 일자")
    @Column(name = "max_aging_days")
    val maxAgingDays: Int? = null,

    @Comment("에이징 범위 사용 여부 (Y/N)")
    @Column(name = "is_active", nullable = false, length = 1)
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true,

    @Comment("원본 레코드가 최초 생성 시 create_time, 수정 시 update_time 입력")
    @Column(name = "create_time", nullable = false)
    val createTime: OffsetDateTime
)
