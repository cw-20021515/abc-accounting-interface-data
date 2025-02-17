package com.abc.us.accounting.rentals.lease.domain.entity

import com.abc.us.accounting.rentals.lease.domain.type.RentalAssetEventType
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.springframework.data.annotation.CreatedDate
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

@Entity
@Table(
    name = "rental_asset_history"
)
@JsonInclude(JsonInclude.Include.NON_NULL)
class RentalAssetHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Int? = null,

    @Comment("시리얼번호")
    @Column(name = "serial_number", nullable = false)
    val serialNumber: String,

    @Comment("자재ID")
    @Column(name = "material_id", nullable = false)
    val materialId: String,

    @Comment("감가상각회차")
    @Column(name = "depreciation_count")
    val depreciationCount: Int? = null,

    @Comment("감가상각일")
    @Column(name = "depreciation_date")
    val depreciationDate: LocalDate? = null,

    @Comment("취득원가")
    @Column(name = "acquisition_cost", nullable = false)
    val acquisitionCost: BigDecimal,

    @Comment("감가상각비")
    @Column(name = "depreciation_expense")
    val depreciationExpense: BigDecimal? = null,

    @Comment("감가상각누계액")
    @Column(name = "accumulated_depreciation")
    val accumulatedDepreciation: BigDecimal? = null,

    @Comment("장부가액")
    @Column(name = "book_value", nullable = false)
    val bookValue: BigDecimal,

    @Comment("계약ID")
    @Column(name = "contract_id", nullable = false)
    val contractId: String,

    @Comment("계약일")
    @Column(name = "contract_date")
    val contractDate: LocalDate?,

    @Comment("계약상태")
    @Column(name = "contract_status", nullable = false)
    val contractStatus: String,

    @Comment("주문ID")
    @Column(name = "order_id", nullable = false)
    val orderId: String,

    @Comment("주문아이템ID")
    @Column(name = "order_item_id", nullable = false)
    val orderItemId: String,

    @Comment("고객ID")
    @Column(name = "customer_id", nullable = false)
    val customerId: String,

    @Comment("이벤트 구분")
    @Column(name = "event_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val eventType: RentalAssetEventType,

    @Comment("해시 값(중복체크)")
    @Column(name = "hash", nullable = false)
    val hash: String,

    @Comment("생성 일시")
    @CreatedDate
    val createTime: OffsetDateTime = OffsetDateTime.now()
)