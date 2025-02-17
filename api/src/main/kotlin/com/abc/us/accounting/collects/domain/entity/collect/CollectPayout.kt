package com.abc.us.accounting.collects.domain.entity.collect

import com.abc.us.accounting.supports.entity.annotation.IgnoreHash
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Comment
import org.hibernate.type.YesNoConverter
import java.time.OffsetDateTime

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
class CollectPayout {
    @Id
    @IgnoreHash
    @Comment("entity 비교 위한 code")
    var hashCode: String? = null

    @Comment("AccountsReceivable ID")
    var AP_id: String?=null

//    @Comment("납부 상태")
//    @Enumerated(EnumType.STRING)
//    var paymentStatus: PaymentStatus? = null

    @Comment("설명")
    var description: String? = null

    @Comment("생성시간")
    var createTime: OffsetDateTime?=null

    @Comment("수정시간")
    var updateTime: OffsetDateTime?= null

    @Comment("활성화 여부")
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true
}
