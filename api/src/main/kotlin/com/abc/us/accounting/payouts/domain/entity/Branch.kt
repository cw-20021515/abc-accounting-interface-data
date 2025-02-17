package com.abc.us.accounting.payouts.domain.entity

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Comment
import org.hibernate.annotations.Type

@Entity
@Table(name = "branch")
class Branch (

    @Id
    @Column(name = "id", nullable = false, length = 255)
    @Comment("branch ID")
    val id: String,

    @Column(name = "company_code", nullable = false, length = 255)
    @Comment("branch name")
    val companyCode: String,

    @Column(name = "name", nullable = false, length = 255)
    @Comment("branch name")
    val name: String,

    @Column(name = "description", nullable = false, length = 255)
    @Comment("branch description")
    val description: String,

    @Column(name = "warehouse_id", nullable = false, length = 255)
    @Comment("창고 ID")
    val warehouseId: String,

    @Column(name = "phone", length = 255)
    @Comment("branch description")
    val phone: String,

    @Column(name = "time_zone", nullable = false, length = 255)
    @Comment("branch timeZone")
    val timeZone: String,

    @Column(name = "address_jsonb", columnDefinition = "jsonb")
    @Type(JsonBinaryType::class)
    @Comment("주소")
    val addressJsonb: MutableMap<String,Any> = mutableMapOf(),
)