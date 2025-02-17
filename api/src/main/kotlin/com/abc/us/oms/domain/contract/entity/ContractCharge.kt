package com.abc.us.oms.domain.contract.entity

import com.abc.us.oms.domain.common.config.AuditTimeEntity
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import org.hibernate.annotations.Type

//@Entity
//@Table(name = "contract_charge", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class ContractCharge(
    @Column(name = "contract_id")
    var contractId: String,
    @Type(JsonType::class)
    @Column(name = "data")
    var data: List<MonthlyCharge>? = null,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "contract_id",
        insertable = false,
        updatable = false,
        referencedColumnName = "id",
    )
    var contract: Contract? = null,
) : AuditTimeEntity()

data class MonthlyCharge(
    val billingCycle: Int,
    val startDate: String,
    val endDate: String,
    val monthlyTotalPrice: String,
    val monthlyOriginPrice: String,
    val monthlyTotalDiscountPrice: String,
    val promotions: List<DiscountInfo>,
) {
    data class DiscountInfo(val promotionId: String, val discountPrice: String)
}
