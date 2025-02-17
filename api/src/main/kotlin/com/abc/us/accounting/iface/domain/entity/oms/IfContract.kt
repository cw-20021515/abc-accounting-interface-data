package com.abc.us.accounting.iface.domain.entity.oms


import com.abc.us.accounting.iface.domain.type.oms.IfContractStatus
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * 계약 정보 테이블 (HISTORY)
 */
@Entity
@Table(name = "if_contract")
@Comment("계약 정보(HISTORY)")
class IfContract(

    @Id
    @Comment("ID")
    @Column(name = "id", nullable = false)
    val id: String,

    @Comment("계약ID")
    @Column(name = "contract_id", nullable = false)
    val contractId: String,

    @Comment("계약상태")
    @Column(name = "contract_status", nullable = false)
    @Enumerated(EnumType.STRING)
    val contractStatus: IfContractStatus,

    @Comment("직전 계약상태")
    @Column(name = "last_contract_status")
    @Enumerated(EnumType.STRING)
    val lastContractStatus: IfContractStatus? = null,

    @Comment("렌탈코드")
    @Column(name = "rental_code", nullable = false, length = 10)
    val rentalCode: String,

    @Comment("주문항목ID")
    @Column(name = "order_item_id", nullable = false)
    val orderItemId: String,

    @Comment("고객ID")
    @Column(name = "customer_id", nullable = false)
    val customerId: String,

    @Comment("계약시작일")
    @Column(name = "start_date")
    val startDate: LocalDate? = null,

    @Comment("계약종료일")
    @Column(name = "end_date")
    val endDate: LocalDate? = null,

    @Comment("계약기간")
    @Column(name = "duration_in_months")
    val durationInMonths: Int? = null,

    @Comment("결제일")
    @Column(name = "payment_day", nullable = false)
    val paymentDay: Int,

    @Comment("생성일시")
    @Column(name = "create_time", nullable = false)
    val createTime: OffsetDateTime,

    @Comment("수정일시")
    @Column(name = "update_time", nullable = false)
    val updateTime: OffsetDateTime
)
