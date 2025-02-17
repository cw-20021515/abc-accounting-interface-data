package com.abc.us.accounting.rentals.master.domain.entity

import java.time.LocalDateTime
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.annotations.CreationTimestamp

@Entity
@Table(name = "RENTAL_FINANCIAL_INTEREST_MASTER")
class RentalFinancialInterestMasterEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("고유 식별자")
    @Column(nullable = false, updatable = false)
    val id: Long? = null, // 기본 키

    @Comment("기준월")
    val targetMonth: String? = null, // 기준월

    @Comment("이자율")
    val interestRate: Double? = null,

    @Comment("등록일시")
    @CreationTimestamp
    val createTime: LocalDateTime = LocalDateTime.now() // 등록일시
)
