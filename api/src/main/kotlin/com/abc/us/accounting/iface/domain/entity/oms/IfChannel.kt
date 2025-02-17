package com.abc.us.accounting.iface.domain.entity.oms

import com.abc.us.accounting.iface.domain.type.oms.IfChannelType
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import java.time.OffsetDateTime

/**
 * 채널 정보 테이블
 */
@Entity
@Table(name = "if_channel")
@Comment("채널 정보")
class IfChannel(

    @Id
    @Comment("채널ID")
    @Column(name = "channel_id", nullable = false)
    val channelId: String,

    @Comment("채널타입")
    @Column(name = "channel_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val channelType: IfChannelType,

    @Comment("채널이름")
    @Column(name = "channel_name", nullable = false)
    val channelName: String,

    @Comment("채널상세")
    @Column(name = "channel_detail", nullable = false)
    val channelDetail: String,

    @Comment("생성시간")
    @Column(name = "create_time", nullable = false)
    val createTime: OffsetDateTime,

    @Comment("수정시간")
    @Column(name = "update_time", nullable = false)
    val updateTime: OffsetDateTime
)
