package com.abc.us.oms.domain.contract.entity

import com.abc.us.oms.domain.taxline.entity.TaxLine
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*

//@Entity
//@Table(name = "contract_payment_information", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class ContractPaymentInformation(
    @Column(name = "contract_id")
    var contractId: String,
    @Column(name = "payment_method")
    var paymentMethod: String,
    @Column(name = "transaction_id")
    var transactionId: String? = null,
    @Column(name = "subscription_payment_day")
    var subscriptionPaymentDay: Int = 1,
    @Column(name = "last_name")
    var lastName: String? = null,
    @Column(name = "first_name")
    var firstName: String? = null,
    @Column(name = "address1")
    var address1: String? = null,
    @Column(name = "address2")
    var address2: String? = null,
    @Column(name = "zipcode")
    var zipcode: String? = null,
    @Column(name = "city")
    var city: String? = null,
    @Column(name = "state")
    var state: String? = null,
    @Column(name = "phone")
    var phone: String? = null,
    @Column(name = "email")
    var email: String? = null,
    @Column(name = "card_number")
    var cardNumber: String? = null,
    @Column(name = "card_type")
    var cardType: String? = null,
    @Column(name = "monthly_total_price")
    var monthlyTotalPrice: Double = 0.0,
    @Column(name = "monthly_discount_price")
    var monthlyDiscountPrice: Double = 0.0,
    @Column(name = "item_monthly_price")
    var itemMonthlyPrice: Double = 0.0,
    @Column(name = "monthly_tax")
    var monthlyTax: Double = 0.0,
    @Column(name = "currency")
    var currency: String = "USD",
    @OneToMany(
        mappedBy = "contractPaymentInformation",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY,
        orphanRemoval = true,
    )
    var taxLines: MutableList<TaxLine> = mutableListOf(),
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "contract_id",
        insertable = false,
        updatable = false,
        referencedColumnName = "id",
    )
    var contract: Contract?,
    @Id
    @Column(name = "id")
    var id: String,
)
