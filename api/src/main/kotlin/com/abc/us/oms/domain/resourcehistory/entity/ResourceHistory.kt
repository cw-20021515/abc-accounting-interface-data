package com.abc.us.oms.domain.resourcehistory.entity

import com.fasterxml.jackson.annotation.JsonInclude
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Type
import java.time.LocalDateTime

@Entity
@Table(name = "oms_resource_history")
@JsonInclude(JsonInclude.Include.NON_NULL)
class ResourceHistory(
    @Id
    val id: String = "",//SnowflakeId.generateId().toBase62(),
    @Column(name = "resource_id")
    val resourceId: String,
    @Column(name = "http_method")
    val httpMethod: String,
    @Column(name = "uri")
    val uri: String,
    @Column(name = "entity_class_name")
    val entityClassName: String,
    val operation: String, // INSERT, UPDATE, DELETE
    @Type(JsonType::class)
    @Column(name = "new_value")
    val newValue: String? = null,
    @Type(JsonType::class)
    @Column(name = "difference")
    var difference: String? = null,
    @Column(name = "create_user")
    val createUser: String,
    @Column(name = "create_time")
    val createTime: LocalDateTime? = null,
)
