package com.abc.us.oms.domain.common.config

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

@MappedSuperclass
open class AuditEntity(
    @CreatedDate
    @Column(name = "create_time")
    var createTime: LocalDateTime? = null,
    @CreatedBy
    @Column(name = "create_user")
    var createUser: String = "test",
    @LastModifiedDate
    @Column(name = "update_time")
    var updateTime: LocalDateTime? = null,
    @LastModifiedBy
    @Column(name = "update_user")
    var updateUser: String = "test",
) : AbstractEntity()

@MappedSuperclass
open class AuditTimeEntity(
    @CreatedDate
    @Column(name = "create_time", nullable = false)
    var createTime: LocalDateTime? = null,
    @LastModifiedDate
    @Column(name = "update_time")
    var updateTime: LocalDateTime? = null,
) : AbstractEntity()

@MappedSuperclass
open class AuditTimeOnlyEntity(
    @CreatedDate
    @Column(name = "create_time", nullable = false)
    var createTime: LocalDateTime? = null,
    @LastModifiedDate
    @Column(name = "update_time")
    var updateTime: LocalDateTime? = null,
)
