package com.abc.us.oms.domain.contract.entity

import com.abc.us.oms.domain.billing.entity.Charge
import com.abc.us.oms.domain.customer.entity.Customer
import com.abc.us.oms.domain.order.entity.OrderItem
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDate
import java.time.LocalDateTime

//@Entity
//@Table(name = "contract", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class Contract(
    @Column(name = "channel_contract_id")
    var channelContractId: String? = null,
    @Column(name = "is_signed")
    var isSigned: Boolean,
    @Column(name = "signed_time")
    var signedTime: LocalDateTime?,
    @Column(name = "form_id")
    var formId: String? = null,
    @Column(name = "revision")
    var revision: Int? = null,
    @Column(name = "customer_id")
    var customerId: String,
    @Column(name = "order_item_id")
    var orderItemId: String,
    @Column(name = "start_date")
    var startDate: LocalDate? = null,
    @Column(name = "end_date")
    var endDate: LocalDate? = null,
    @Column(name = "duration_in_months")
    var durationInMonths: Int,
    @Column(name = "contract_status_code")
    var contractStatusCode: String,
    @Column(name = "rental_code")
    var rentalCode: String,
    @CreatedDate
    @Column(name = "create_time")
    val createTime: LocalDateTime? = null,
    @LastModifiedDate
    @Column(name = "update_time")
    var updateTime: LocalDateTime? = null,
    @OneToOne(mappedBy = "contract", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var contractPaymentInformation: ContractPaymentInformation?,
    @OneToOne(mappedBy = "contract", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var contractDocument: ContractDocument? = null,
    @OneToOne(mappedBy = "contract", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var contractCharge: ContractCharge? = null,
    @OneToMany(mappedBy = "contract", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("billingCycle ASC")
    var charges: MutableList<Charge> = mutableListOf(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "order_item_id",
        insertable = false,
        updatable = false,
        referencedColumnName = "id",
    )
    var orderItem: OrderItem?,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false, referencedColumnName = "id")
    var customer: Customer? = null,
    @Id
    @Column(name = "id")
    var id: String,
)
