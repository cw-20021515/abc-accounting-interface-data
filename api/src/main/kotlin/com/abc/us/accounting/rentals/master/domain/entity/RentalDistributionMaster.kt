package com.abc.us.accounting.rentals.master.domain.entity

import com.abc.us.accounting.rentals.master.domain.type.RentalDistributionType
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.hibernate.annotations.Comment
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table (
    name = "rental_distribution_master",
)
@JsonInclude(JsonInclude.Include.NON_NULL)
class RentalDistributionMaster (
    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "rental_distribution_master_seq_generator"
    )
    val id: Int? = null,

    @Comment("모델대표명")
    @Column(name = "material_model_name_prefix", nullable = false)
    val materialModelNamePrefix: String,

    @Comment("분할유형")
    @Column(name = "rental_distribution_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val rentalDistributionType: RentalDistributionType,

    @Comment("일시불 가격")
    @Column(name = "onetime_price", nullable = false)
    val onetimePrice: BigDecimal,

    @Comment("멤버십 가격")
    @Column(name = "membership_price", nullable = false)
    val membershipPrice: BigDecimal,

    @Comment("멤버십 2년 약정시 할인")
    @Column(name = "membership_dcprice_c24", nullable = false)
    val membershipDiscountPriceC24: BigDecimal,

    @Comment("무상 서비스 기간")
    @Column(name = "free_service_duration")
    val freeServiceDuration: Int,

    @Comment("효력 시작일")
    @Column(name = "start_date", nullable = false)
    val startDate: LocalDate
) {


    override fun toString(): String {
        return "RentalDistributionMaster{" +
                ", id='" + id + '\'' +
                ", materialModelNamePrefix='" + materialModelNamePrefix + '\'' +
                ", rentalDistributionType='" + rentalDistributionType + '\'' +
                ", onetimePrice='" + onetimePrice + '\'' +
                ", membershipPrice='" + membershipPrice + '\'' +
                ", membershipDiscountPriceC24='" + membershipDiscountPriceC24 + '\'' +
                ", freeServiceDuration=" + freeServiceDuration + '\'' +
                ", startDate=" + startDate + '\'' +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is RentalDistributionMaster) return false

        return EqualsBuilder()
            .append(id, other.id)
            .append(materialModelNamePrefix, other.materialModelNamePrefix)
            .append(rentalDistributionType, other.rentalDistributionType)
            .append(onetimePrice, other.onetimePrice)
            .append(membershipPrice, other.membershipPrice)
            .append(membershipDiscountPriceC24, other.membershipDiscountPriceC24)
            .append(freeServiceDuration, other.freeServiceDuration)
            .append(startDate, other.startDate)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(id)
            .append(materialModelNamePrefix)
            .append(rentalDistributionType)
            .append(onetimePrice)
            .append(membershipPrice)
            .append(membershipDiscountPriceC24)
            .append(freeServiceDuration)
            .append(startDate)
            .toHashCode()
    }
}
