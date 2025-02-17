package com.abc.us.accounting.iface.domain.entity.oms

import com.abc.us.accounting.iface.domain.type.oms.IfCustomerStatus
import com.abc.us.accounting.iface.domain.type.oms.IfCustomerType
import java.time.OffsetDateTime
import jakarta.persistence.*
import org.hibernate.annotations.Comment

/**
 * 고객 정보 테이블
 */
@Entity
@Table(name = "if_customer")
@Comment("고객 정보")
class IfCustomer(

    @Comment("고객ID")
    @Id
    @Column(name = "customer_id", nullable = false)
    val customerId: String,

    @Comment("이메일")
    @Column(name = "email", nullable = false)
    val email: String,

    @Comment("전화번호")
    @Column(name = "phone")
    val phone: String? = null,

    @Comment("AMS ID")
    @Column(name = "user_id")
    val userId: String? = null,

    @Comment("성")
    @Column(name = "last_name")
    val lastName: String? = null,

    @Comment("이름")
    @Column(name = "first_name")
    val firstName: String? = null,

    @Comment("고객유형(CORPORATE/INDIVIDUAL/STAFF/ACADEMY/OTHERS)")
    @Column(name = "account_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val accountType: IfCustomerType,

    @Comment("고객상태(ACTIVE/BANKRUPT/DECEASED)")
    @Column(name = "customer_status", nullable = false)
    @Enumerated(EnumType.STRING)
    val customerStatus: IfCustomerStatus,

    @Comment("레퍼럴코드")
    @Column(name = "referrer_code", nullable = false)
    val referrerCode: String,

    @Comment("생성일시")
    @Column(name = "create_time", nullable = false)
    val createTime: OffsetDateTime,

    @Comment("수정일시")
    @Column(name = "update_time", nullable = false)
    val updateTime: OffsetDateTime
)
