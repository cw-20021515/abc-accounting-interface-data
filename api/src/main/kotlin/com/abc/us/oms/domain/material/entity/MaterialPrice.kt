@file:Suppress("JpaDataSourceORMInspection")

package com.abc.us.oms.domain.material.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDate
import java.time.LocalDateTime

//@Entity
//@Table(name = "material_price", catalog = "abc_oms", schema = "public")
@JsonInclude(JsonInclude.Include.NON_NULL)
class MaterialPrice(
    @Id
    @Column(name = "id")
    val id: String,
    @Column(name = "material_id")
    val materialId: String,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "material_id",
        referencedColumnName = "id",
        updatable = false,
        insertable = false,
    )
    var material: Material? = null,
    @Column(name = "type")
    var type: String,
    @Column(name = "tag")
    var tag: String? = null,
    @Column(name = "is_active")
    var isActive: Boolean? = false,
    @Column(name = "currency")
    var currency: String,
    @Column(name = "purchase_registration_fee")
    var purchaseRegistrationFee: Double? = 0.0,
    @Column(name = "rental_registration_fee")
    var rentalRegistrationFee: Double? = 0.0,
    @Column(name = "retail_price")
    var retailPrice: Double? = 0.0,
    @Column(name = "service_price")
    var servicePrice: Double? = 0.0,
    @Column(name = "rental_code")
    var rentalCode: String? = null,
    @Column(name = "rental_code_name")
    var rentalCodeName: String? = null,
    @Column(name = "rental_contract_period")
    var rentalContractPeriod: Int? = 0,
    @Column(name = "rental_contract_cycle")
    var rentalContractCycle: Int? = 0,
    @Column(name = "effective_start_date")
    var effectiveStartDate: LocalDate,
    @Column(name = "effective_end_date")
    var effectiveEndDate: LocalDate,
    @CreatedBy
    @Column(name = "create_user")
    var createUser: String? = "Administrator",
    @Column(name = "create_time")
    @CreatedDate
    var createTime: LocalDateTime? = null,
    @LastModifiedBy
    @Column(name = "update_user")
    var updateUser: String? = createUser,
    @Column(name = "update_time")
    @LastModifiedDate
    var updateTime: LocalDateTime? = null,
)
