package com.abc.us.accounting.rentals.master.domain.entity

import com.abc.us.accounting.rentals.master.domain.type.LeaseType
import com.abc.us.accounting.rentals.master.domain.type.ContractPricingType
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.hibernate.annotations.Comment
import java.time.OffsetDateTime

@Entity
@Table(
    name = "rental_code_master",
)
@JsonInclude(JsonInclude.Include.NON_NULL)
class RentalCodeMaster (
    @Id
    @Comment("렌탈 코드")
    val rentalCode: String,

    @Comment("렌탈코드 명")
    val rentalCodeName: String,

    @Comment("렌탈코드 설명")
    val rentalCodeDescription: String,

    @Comment("계약회차")
    @Column(name = "current_term")
    val currentTerm: Int,

    @Comment("1회차 계약")
    @Column(name = "term1_period")
    val term1Period: Int,

    @Comment("2회차 계약")
    @Column(name = "term2_period")
    val term2Period: Int? = null,

    @Comment("3회차 계약")
    @Column(name = "term3_period")
    val term3Period: Int? = null,

    @Comment("4회차 계약")
    @Column(name = "term4_period")
    val term4Period: Int? = null,

    @Comment("5회차 계약")
    @Column(name = "term5_period")
    val term5Period: Int? = null,


    @Comment("요금체계")
    @Column(name = "contract_pricing_type")
    @Enumerated(EnumType.STRING)
    val contractPricingType: ContractPricingType,

    @Comment("렌탈기간")
    @Column(name = "contract_duration")
    val contractDuration: Int,

    @Comment("약정기간")
    @Column(name = "commitment_duration")
    val commitmentDuration: Int,

    @Comment("리스유형")
    @Column(name = "lease_type")
    @Enumerated(EnumType.STRING)
    val leaseType: LeaseType,

    @Comment("비고")
    @Column(name = "remark")
    val remark: String? = null,

    @Comment("사용여부")
    @Column(name = "is_active")
    val isActive: Boolean,

    @Comment("생성 일시")
    val createTime: OffsetDateTime = OffsetDateTime.now(),

    @Comment("생성 일시")
    val updateTime: OffsetDateTime = OffsetDateTime.now()
) {

    override fun toString(): String {
        return "RentalCodeMaster{" +
                ", rentalCode='" + rentalCode + '\'' +
                ", rentalCodeName='" + rentalCodeName + '\'' +
                ", rentalCodeDescription='" + rentalCodeDescription + '\'' +
                ", currentTerm=" + currentTerm + '\'' +
                ", term1Period=" + term1Period + '\'' +
                ", term2Period=" + term2Period + '\'' +
                ", term3Period=" + term3Period + '\'' +
                ", term4Period=" + term4Period + '\'' +
                ", contractPricingType=" + contractPricingType + '\'' +
                ", contractDuration=" + contractDuration + '\'' +
                ", commitmentDuration=" + commitmentDuration + '\'' +
                ", leaseType=" + leaseType + '\'' +
                ", isActive=" + isActive + '\'' +
                ", remark='" + remark + '\'' +
                ", createTime=" + createTime + '\'' +
                ", updateTime=" + updateTime + '\'' +
                '}'
    }

    val adjustedContractPeriod: Int
        /**
         * 조정약정기간 계산
         * 운용리스: 0 -> 60개월 환산
         * @return
         */
        get() {
            if (leaseType == LeaseType.OPERATING_LEASE) {
                return ADJUSTED_CONTRACT_PERIOD
            }
            return commitmentDuration
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is RentalCodeMaster) return false

        return EqualsBuilder()
            .append(rentalCode, other.rentalCode)
            .append(rentalCodeName, other.rentalCodeName)
            .append(rentalCodeDescription, other.rentalCodeDescription)
            .append(currentTerm, other.currentTerm)
            .append(term1Period, other.term1Period)
            .append(term2Period, other.term2Period)
            .append(term3Period, other.term3Period)
            .append(term4Period, other.term4Period)
            .append(term5Period, other.term5Period)
            .append(contractPricingType, other.contractPricingType)
            .append(contractDuration, other.contractDuration)
            .append(commitmentDuration, other.commitmentDuration)
            .append(leaseType, other.leaseType)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(rentalCode)
            .append(rentalCodeName)
            .append(rentalCodeDescription)
            .append(currentTerm)
            .append(term1Period)
            .append(term2Period)
            .append(term3Period)
            .append(term4Period)
            .append(term5Period)
            .append(contractPricingType)
            .append(contractDuration)
            .append(commitmentDuration)
            .append(leaseType)
            .toHashCode()
    }

    companion object {
        /**
         * 운용리스 조정약정기간 (렌탈 안분기준 계산시 60개월 간주)
         */
        private const val ADJUSTED_CONTRACT_PERIOD = 60
    }
}
