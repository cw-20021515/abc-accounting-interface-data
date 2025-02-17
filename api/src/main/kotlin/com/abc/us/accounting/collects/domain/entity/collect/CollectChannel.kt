package com.abc.us.accounting.collects.domain.entity.collect

import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableRelation
import com.abc.us.accounting.collects.domain.type.ChannelTypeEnum
import com.abc.us.accounting.supports.entity.annotation.IgnoreHash
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.type.YesNoConverter
import java.time.OffsetDateTime

@Entity
class CollectChannel {

    @Id
    @IgnoreHash
    @Comment("entity 고유 식별자")
    var hashCode: String? = null

    @Embedded
    var relation : EmbeddableRelation? = null

    var channelId : String? = null

    @Enumerated(EnumType.STRING)
    var channelType : ChannelTypeEnum? = null

    var channelName: String? = null

    var channelDetail: String? = null

    @IgnoreHash
    @Comment("생성일시")
    @Column(name = "create_time")
    var createTime: OffsetDateTime? = null

    @IgnoreHash
    @Comment("갱신일시")
    @Column(name = "update_time")
    var updateTime: OffsetDateTime? = null

    @IgnoreHash
    @Comment("활성화 여부")
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true
}