package com.abc.us.oms.domain.channel.entity

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.Column
import jakarta.persistence.Id
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

//@Entity
//@Table(name = "channel", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class Channel(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: String,
    @Column(name = "channel_type", nullable = false)
    val channelType: String,
    @Column(name = "channel_name", nullable = false)
    val channelName: String,
    @Column(name = "channel_detail", nullable = true)
    val channelDetail: String? = null,
    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false)
    val createTime: LocalDateTime? = null,
    @LastModifiedDate
    @Column(name = "update_time", nullable = false)
    var updateTime: LocalDateTime? = null
)
