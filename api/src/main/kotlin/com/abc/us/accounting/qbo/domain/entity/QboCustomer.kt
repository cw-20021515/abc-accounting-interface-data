package com.abc.us.accounting.qbo.domain.entity

import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableName
import com.abc.us.accounting.collects.domain.type.ChannelTypeEnum
import com.abc.us.accounting.qbo.domain.entity.key.QboCustomerKey
import com.abc.us.accounting.iface.domain.type.oms.IfCustomerStatus
import com.abc.us.accounting.iface.domain.type.oms.IfCustomerType
import com.fasterxml.jackson.annotation.JsonInclude
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.annotations.Type
import org.hibernate.type.YesNoConverter
import java.time.OffsetDateTime


@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
class QboCustomer(
    @Id
    @EmbeddedId
    var key: QboCustomerKey,

    var customerType: IfCustomerType? = null,
    var customerStatus: IfCustomerStatus? = null,
    var channelType: ChannelTypeEnum? = null,

    @Embedded
    var name : EmbeddableName,

    @Type(JsonType::class)
    @Column(name = "submit_result")
    val submitResult: String,

    @Comment("생성 시간")
    var createTime: OffsetDateTime? = null,

    @Comment("갱신 시간")
    var updateTime: OffsetDateTime? = null,

    @Comment("사용 여부")
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true
){

}