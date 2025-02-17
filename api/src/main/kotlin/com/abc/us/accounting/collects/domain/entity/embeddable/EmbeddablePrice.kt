package com.abc.us.accounting.collects.domain.entity.embeddable

import com.abc.us.accounting.supports.entity.Hashable
import com.abc.us.accounting.supports.utils.Hashs
import jakarta.persistence.Convert
import jakarta.persistence.Embeddable
import org.hibernate.annotations.Comment
import org.hibernate.type.YesNoConverter
import java.math.BigDecimal

@Embeddable
data class EmbeddablePrice(

    @Comment("실제 결제 총액")
    var totalPrice: BigDecimal,

    @Comment("할인 금액")
    var discountPrice: BigDecimal?=null,

    @Comment("주문 Item 가격")
    var itemPrice: BigDecimal?=null,

    @Comment("선불금액(포인트, 마일리지, 기 납부금액 등)")
    var prepaidAmount: BigDecimal?=null,

    @Comment("세액")
    var tax: BigDecimal?=null,

    @Comment("등록비")
    var registrationPrice : BigDecimal?=null,

    @Comment("통화")
    var currency: String,

    @Comment("면세여부")
    @Convert(converter = YesNoConverter::class)
    var isTaxExempt: Boolean = false

): Hashable {
    override fun hashValue(): String {
        val builder = StringBuilder()
        val inputStr = builder
            .append(totalPrice).append("|")
            .append(discountPrice).append("|")
            .append(itemPrice).append("|")
            .append(prepaidAmount).append("|")
            .append(tax).append("|")
            .append(currency).append("|")
            .append(registrationPrice)
            .toString()
        return Hashs.sha256Hash(inputStr)
    }
}