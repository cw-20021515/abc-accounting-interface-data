package com.abc.us.accounting.rentals.lease.domain.entity

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.springframework.data.annotation.CreatedDate
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

@Entity
@Table(
    name = "rental_asset_depreciation_schedule"
)
@JsonInclude(JsonInclude.Include.NON_NULL)
class RentalAssetDepreciationSchedule(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Int? = null,

    @Comment("시리얼번호")
    @Column(name = "serial_number")
    val serialNumber: String,

    @Comment("감가상각회차")
    @Column(name = "depreciation_count")
    val depreciationCount: Int,

    @Comment("감가상각일")
    @Column(name = "depreciation_date")
    val depreciationDate: LocalDate,

    @Comment("통화")
    @Column(name = "currency")
    val currency: String,

    @Comment("기초 장부가액")
    @Column(name = "beginning_book_value")
    val beginningBookValue: BigDecimal,

    @Comment("감가상각비")
    @Column(name = "depreciation_expense")
    val depreciationExpense: BigDecimal,

    @Comment("기말 장부가액")
    @Column(name = "ending_book_value")
    val endingBookValue: BigDecimal,

    @Comment("감가상각누계액")
    @Column(name = "accumulated_depreciation")
    val accumulatedDepreciation: BigDecimal,

    @Comment("생성 일시")
    @CreatedDate
    val createTime: OffsetDateTime = OffsetDateTime.now()
)