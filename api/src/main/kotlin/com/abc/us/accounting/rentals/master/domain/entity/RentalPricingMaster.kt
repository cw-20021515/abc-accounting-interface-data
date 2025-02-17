package com.abc.us.accounting.rentals.master.domain.entity

import com.abc.us.accounting.rentals.master.domain.type.MaterialCareType
import com.abc.us.accounting.rentals.master.domain.type.PeriodType
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.hibernate.annotations.Comment
import org.hibernate.type.YesNoConverter
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*


@Entity
@Table (
    name = "rental_pricing_master",
)
@JsonInclude(JsonInclude.Include.NON_NULL)
class RentalPricingMaster(
    @Id
    val id: Int? = null,

    @Comment("모델대표명")
    val materialModelNamePrefix: String? = null,

    @Comment("렌탈코드")
    val rentalCode: String? = null,

    @Comment("제품 관리방식")
    @Enumerated(EnumType.STRING)
    val materialCareType: MaterialCareType? = null,

    @Comment("가격")
    @Column(precision = 38, scale = 4)
    val price: BigDecimal? = null,

    @Comment("통화")
    val currency: Currency? = null,

    @Comment("세금포함")
    @Convert(converter = YesNoConverter::class)
    val taxIncluded: Boolean = false,

    @Comment("기준")
    @Enumerated(EnumType.STRING)
    val periodType: PeriodType? = null,

    @Comment("생성 일시")
    val startDate: LocalDate? = null
){

    override fun toString(): String {
        return "RentalPriceMaster{" +
                ", id='" + id + '\'' +
                ", materialModelNamePrefix='" + materialModelNamePrefix + '\'' +
                ", rentalCode='" + rentalCode + '\'' +
                ", materialCareType='" + materialCareType + '\'' +
                ", price='" + price + '\'' +
                ", currency=" + currency + '\'' +
                ", taxIncluded=" + taxIncluded + '\'' +
                ", periodType=" + periodType + '\'' +
                ", startDate=" + startDate + '\'' +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is RentalPricingMaster) return false

        return EqualsBuilder()
            .append(id, other.id)
            .append(materialModelNamePrefix, other.materialModelNamePrefix)
            .append(rentalCode, other.rentalCode)
            .append(materialCareType, other.materialCareType)
            .append(price, other.price)
            .append(currency, other.currency)
            .append(taxIncluded, other.taxIncluded)
            .append(periodType, other.periodType)
            .append(startDate, other.startDate)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(id)
            .append(materialModelNamePrefix)
            .append(rentalCode)
            .append(materialCareType)
            .append(price)
            .append(currency)
            .append(taxIncluded)
            .append(periodType)
            .append(startDate)
            .toHashCode()
    }
}
