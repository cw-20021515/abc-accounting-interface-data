package com.abc.us.accounting.rentals.lease.domain.entity

import com.abc.us.accounting.rentals.lease.domain.type.RentalAssetDepreciationMethod
import com.abc.us.accounting.rentals.lease.utils.RentalAssetDepreciationMethodConverter
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(
    name = "rental_asset_depreciation_master"
)
@JsonInclude(JsonInclude.Include.NON_NULL)
class RentalAssetDepreciationMaster(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Int? = null,

    @Comment("자재ID")
    @Column(name = "material_id")
    val materialId: String,

    @Comment("내용연수")
    @Column(name = "useful_life")
    val usefulLife: Int,

    @Comment("잔존가치")
    @Column(name = "salvage_value")
    val salvageValue: BigDecimal,

    @Comment("통화")
    @Column(name = "currency")
    val currency: String,

    @Comment("상각방법")
    @Column(name = "depreciation_method")
    @Convert(converter = RentalAssetDepreciationMethodConverter::class)
    val depreciationMethod: RentalAssetDepreciationMethod,

    @Comment("시작일")
    @Column(name = "start_date")
    val startDate: LocalDate
)