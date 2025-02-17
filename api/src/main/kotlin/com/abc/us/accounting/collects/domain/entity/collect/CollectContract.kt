package com.abc.us.accounting.collects.domain.entity.collect

import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableRelation
import com.abc.us.accounting.supports.entity.annotation.IgnoreHash
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.Convert
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Comment
import org.hibernate.type.YesNoConverter
import java.time.OffsetDateTime

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CollectContract(

    @Id
    @IgnoreHash
    @Comment("entity 비교 위한 code")
    var hashCode: String? = null,

    @Embedded
    var relation : EmbeddableRelation? = null,

    @Comment("계약 아이디")
    var contractId: String,

    @Comment("접수채널에서 생성된 계약 아이디")
    var channelContractId: String? = null,

    @Comment("판매채널 주문상세 아이디")
    var channelOrderItemId: String? = null,

    @Comment("서명여부")
    @Convert(converter = YesNoConverter::class)
    var isSigned: Boolean?=null,

    @Comment("서명시간")
    var signedTime: OffsetDateTime? = null,

    @Comment("계약서 양식 아이디")
    var formId: String? = null,

    @Comment("계약서 리비전")
    var revision: Int?=null,

    @Comment("렌탈 코드")
    var rentalCode: String?=null,

    @Comment("고객아이디")
    var customerId: String?=null,

    @Comment("주문아이디")
    var orderId: String?=null,

    @Comment("주문상세 아이디")
    var orderItemId: String?=null,

    @Comment("자재 ID")
    var materialId: String?=null,

    @Comment("약정 시작")
    var startDate: java.time.LocalDate? = null,

    @Comment("약정 끝 날짜")
    var endDate: java.time.LocalDate? = null,

    @Comment("약정기간")
    var durationInMonths: Int?=null,

    @Comment("계약상태 code")
    var contractStatus: String?=null,

    @IgnoreHash
    @Comment("생성시간")
    var createTime: OffsetDateTime?=null,

    @IgnoreHash
    @Comment("수정시간")
    var updateTime: OffsetDateTime?= null,

    @IgnoreHash
    @Comment("활성화 여부")
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true,

    @IgnoreHash
    @Transient
    var customer : CollectCustomer? = null,

    @IgnoreHash
    @Transient
    var charges : CollectCharge? = null
) {

}