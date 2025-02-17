package com.abc.us.accounting.iface.domain.entity.logistics

import com.abc.us.accounting.iface.domain.type.logistics.IfIncotermsType
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import jakarta.persistence.*
import org.hibernate.annotations.Comment

/**
 * 발주 Invoice Header 정보 테이블
 */
@Entity
@Table(name = "if_purchase_order_invoice")
@Comment("발주 Invoice Header 정보")
class IfPurchaseOrderInvoice(

    @Comment("고유 식별자")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long = 0L,

    @Comment("발주 ID(SAP BSTKD)")
    @Column(name = "purchase_order_id", nullable = false)
    val purchaseOrderId: String,

    @Comment("Invoice 번호(SAP CINO)")
    @Column(name = "invoice_no", nullable = false, unique = true)
    val invoiceNo: String,

    @Comment("B/L 번호(SAP BLNO)")
    @Column(name = "bl_no")
    val blNo: String? = null,

    @Comment("국제상업거래조건(SAP INCO1)")
    @Column(name = "incoterms", length = 3)
    @Enumerated(EnumType.STRING)
    val incoterms: IfIncotermsType? = null,

    @Comment("수취 회사명(SAP CNAME)")
    @Column(name = "consignee_name", nullable = false)
    val consigneeName: String,

    @Comment("수취인 상세 주소(SAP CSTREET)")
    @Column(name = "consignee_address")
    val consigneeAddress: String? = null,

    @Comment("수취인 도시(SAP CCITY1)")
    @Column(name = "consignee_city")
    val consigneeCity: String? = null,

    @Comment("수취인 우편번호(SAP CPSTLZ)")
    @Column(name = "consignee_postal_code")
    val consigneePostalCode: String? = null,

    @Comment("수취 회사 담당자 이름(SAP CPNAME)")
    @Column(name = "consignee_manager", nullable = false)
    val consigneeManager: String,

    @Comment("수취인 연락처(SAP CPHONE)")
    @Column(name = "consignee_phone")
    val consigneePhone: String? = null,

    @Comment("수취인 이메일(SAP CEMAIL)")
    @Column(name = "consignee_email")
    val consigneeEmail: String? = null,

    @Comment("발송 회사명(SAP NAME2)")
    @Column(name = "shipper_name", nullable = false)
    val shipperName: String,

    @Comment("발송인 상세 주소(SAP STREET)")
    @Column(name = "shipper_address")
    val shipperAddress: String? = null,

    @Comment("발송인 도시(SAP CITY1)")
    @Column(name = "shipper_city")
    val shipperCity: String? = null,

    @Comment("발송인 우편번호(SAP PSTLZ)")
    @Column(name = "shipper_postal_code")
    val shipperPostalCode: String? = null,

    @Comment("발송 회사 담당자(SAP NAME2)")
    @Column(name = "shipper_manager", nullable = false)
    val shipperManager: String,

    @Comment("발송인 연락처(SAP PHONE)")
    @Column(name = "shipper_phone")
    val shipperPhone: String? = null,

    @Comment("발송인 이메일(SAP EMAIL)")
    @Column(name = "shipper_email")
    val shipperEmail: String? = null,

    @Comment("화물 도착 시 통지 회사명(SAP NNAME)")
    @Column(name = "notify_party_name")
    val notifyPartyName: String? = null,

    @Comment("화물 도착 시 통지 회사 상세 주소(SAP NSTREET)")
    @Column(name = "notify_party_address")
    val notifyPartyAddress: String? = null,

    @Comment("화물 도착 시 통지 회사 도시(SAP NCITY1)")
    @Column(name = "notify_party_city")
    val notifyPartyCity: String? = null,

    @Comment("화물 도착 시 통지 회사 우편번호(SAP NPSTLZ)")
    @Column(name = "notify_party_postal_code")
    val notifyPartyPostalCode: String? = null,

    @Comment("화물 도착 시 통지 회사 담당자(SAP NPNAME)")
    @Column(name = "notify_party_manager")
    val notifyPartyManager: String? = null,

    @Comment("화물 도착 시 통지 회사 이메일(SAP NEMAIL)")
    @Column(name = "notify_party_email")
    val notifyPartyEmail: String? = null,

    @Comment("화물 도착 시 통지 회사 전화번호(SAP NPHONE)")
    @Column(name = "notify_party_phone")
    val notifyPartyPhone: String? = null,

    @Comment("출발 항구(SAP KNATX)")
    @Column(name = "port_of_loading", nullable = false)
    val portOfLoading: String,

    @Comment("도착 항구(SAP KNETX)")
    @Column(name = "port_of_discharge", nullable = false)
    val portOfDischarge: String,

    @Comment("선박 이름(SAP VESLI)")
    @Column(name = "ocean_vessel")
    val oceanVessel: String? = null,

    @Comment("항해 번호(SAP VOYNO)")
    @Column(name = "voyage_no")
    val voyageNo: String? = null,

    @Comment("출항일(SAP ONBDT)")
    @Column(name = "onboard_date")
    val onboardDate: LocalDate? = null,

    @Comment("Invoice 발행일(SAP IVDAT)")
    @Column(name = "invoice_date", nullable = false)
    val invoiceDate: LocalDate,

    @Comment("비고(SAP REMARK)")
    @Column(name = "remark")
    val remark: String? = null,

    @Comment("총수량(SAP TOTCNT)")
    @Column(name = "total_quantity", nullable = false)
    val totalQuantity: Int,

    @Comment("총 금액(SAP TOTAMT)")
    @Column(name = "total_amount", nullable = false, precision = 38, scale = 4)
    val totalAmount: BigDecimal,

    @Comment("통화 코드(SAP WAERK)")
    @Column(name = "currency", nullable = false, length = 3)
    val currency: String,

    @Comment("SAP 조회 일시")
    @Column(name = "create_time", nullable = false)
    val createTime: OffsetDateTime
)
