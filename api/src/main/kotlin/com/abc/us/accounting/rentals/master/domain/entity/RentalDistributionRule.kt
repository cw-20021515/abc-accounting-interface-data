package com.abc.us.accounting.rentals.master.domain.entity

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.rentals.master.domain.type.LeaseType
import com.abc.us.accounting.rentals.master.domain.type.MaterialCareType
import com.abc.us.accounting.supports.utils.Hashs
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import org.apache.commons.lang3.builder.EqualsBuilder
import org.hibernate.annotations.Comment
import java.time.LocalDate
import java.time.OffsetDateTime


/**
 * 렌탈 안분(재화/서비스) 구분 규칙 (rental_distribution_rule)
 *
 * rental_distribution_master에서 생성되는 데이터
 */
@Entity
@Table (
    name = "rental_distribution_rule"
)
@JsonInclude(JsonInclude.Include.NON_NULL)
class RentalDistributionRule(
    @Id
    val id: Long,

    @Comment("자재ID")
    val materialId: String,

    @Comment("모델대표명")
    val materialModelNamePrefix: String,

    @Comment("렌탈 코드")
    val rentalCode: String,

    @Comment("제품 관리방식")
    @Enumerated(EnumType.STRING)
    @Column(name = "material_care_type")
    val materialCareType: MaterialCareType,

    @Comment("회계처리")
    @Enumerated(EnumType.STRING)
    val leaseType: LeaseType,

    @Comment("약정기간")
    val commitmentDuration: Int,

    @Comment("조정 약정기간")
    val adjustedCommitmentDuration: Int,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "m01", column = Column(name = "dist_value_m01", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)),
        AttributeOverride(name = "r01", column = Column(name = "dist_value_r01", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)),
        AttributeOverride(name = "r02", column = Column(name = "dist_value_r02", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)),
        AttributeOverride(name = "r03", column = Column(name = "dist_value_r03", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)),
        AttributeOverride(name = "s01", column = Column(name = "dist_value_s01", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)),
        AttributeOverride(name = "t01", column = Column(name = "dist_value_t01", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE))
    )
    val distributionValue: Distribution,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "m01", column = Column(name = "dist_ratio_m01", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.RATIO_SCALE)),
        AttributeOverride(name = "r01", column = Column(name = "dist_ratio_r01", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.RATIO_SCALE)),
        AttributeOverride(name = "r02", column = Column(name = "dist_ratio_r02", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.RATIO_SCALE)),
        AttributeOverride(name = "r03", column = Column(name = "dist_ratio_r03", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.RATIO_SCALE)),
        AttributeOverride(name = "s01", column = Column(name = "dist_ratio_s01", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.RATIO_SCALE)),
        AttributeOverride(name = "t01", column = Column(name = "dist_ratio_t01", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.RATIO_SCALE)),
    )
    val distributionRatio: Distribution,


    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "m01", column = Column(name = "dist_price_m01", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)),
        AttributeOverride(name = "r01", column = Column(name = "dist_price_r01", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)),
        AttributeOverride(name = "r02", column = Column(name = "dist_price_r02", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)),
        AttributeOverride(name = "r03", column = Column(name = "dist_price_r03", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)),
        AttributeOverride(name = "s01", column = Column(name = "dist_price_s01", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)),
        AttributeOverride(name = "t01", column = Column(name = "dist_price_t01", precision = Constants.ACCOUNTING_PRECISION, scale = Constants.ACCOUNTING_SCALE)),
    )
    val distributionPrice: Distribution,

    @Comment("효력 시작일")
    val startDate: LocalDate,

    @Comment("생성일시")
    val createTime: OffsetDateTime = OffsetDateTime.now(),

    @Comment("수정일시")
    var updateTime: OffsetDateTime = OffsetDateTime.now(),
)  {

    @PrePersist
    @PreUpdate
    fun prePersist() {
        updateTime = OffsetDateTime.now()
    }

    override fun toString(): String {
        return "RentalDistributionRule{" +
                "id='" + id + '\'' +
                ", materialId='" + materialId + '\'' +
                ", materialModelNamePrefix='" + materialModelNamePrefix + '\'' +
                ", rentalCode='" + rentalCode + '\'' +
                ", materialCareType='" + materialCareType + '\'' +
                ", leaseType='" + leaseType + '\'' +
                ", commitmentDuration=" + commitmentDuration + '\'' +
                ", adjustedCommitmentDuration=" + adjustedCommitmentDuration + '\'' +
                ", distributionValue=" + distributionValue + '\'' +
                ", distributionRatio=" + distributionRatio + '\'' +
                ", distributionPrice=" + distributionPrice + '\'' +
                ", startDate=" + startDate + '\'' +  //                ", endDate=" + endDate+ '\'' +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is RentalDistributionRule) return false

        return EqualsBuilder()
            .append(materialId, other.materialId)
            .append(rentalCode, other.rentalCode)
            .append(materialCareType.name, other.materialCareType.name)
            .append(startDate, other.startDate)
            .isEquals
    }

    override fun hashCode(): Int {
        // kotlin enum은 hash가 변경됨
        return Hashs.hash(materialId, rentalCode, materialCareType, startDate).hashCode()
    }

    fun overwrite(claim: RentalDistributionRule): RentalDistributionRule {
        return RentalDistributionRule(
            id = this.id,
            materialId = claim.materialId,
            materialModelNamePrefix = claim.materialModelNamePrefix,
            rentalCode = claim.rentalCode,
            materialCareType = claim.materialCareType,
            leaseType = claim.leaseType,
            commitmentDuration = claim.commitmentDuration,
            adjustedCommitmentDuration = claim.adjustedCommitmentDuration,
            distributionValue = claim.distributionValue,
            distributionRatio = claim.distributionRatio,
            distributionPrice = claim.distributionPrice,
            startDate = claim.startDate,
            createTime = claim.createTime
        )
    }
}
