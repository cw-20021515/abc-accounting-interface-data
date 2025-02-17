package com.abc.us.accounting.collects.domain.entity.collect

import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableLocation
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableName
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
class CollectInstallation{

    @Id
    @IgnoreHash
    @Comment("entity 비교 위한 code")
    var hashCode: String? = null

    @Embedded
    var relation : EmbeddableRelation? = null
    @Embedded
    var name : EmbeddableName? = null
    @Embedded
    var location : EmbeddableLocation? = null
    @Comment("")
    var installId: String? = null
    @Comment("")
    var orderItemId: String? = null
    @Comment("시리얼 넘버")
    var serialNumber: String? = null
    @Comment("작업자 아이디")
    var technicianId: String? = null
    @Comment("서비스플로우 아이디")
    var serviceFlowId: String? = null
    @Comment("설치일")
    var installationTime: OffsetDateTime? = null
    @Comment("워런티 시작")
    var warrantyStartTime: OffsetDateTime? = null
    @Comment("워런티 종료")
    var warrantyEndTime: OffsetDateTime? = null
    @Comment("설치 환경 정보 (수도)")
    var waterType: String? = null

    @IgnoreHash
    @Comment("생성시간")
    var createTime: OffsetDateTime?=null

    @IgnoreHash
    @Comment("수정시간")
    var updateTime: OffsetDateTime?= null

    @IgnoreHash
    @Comment("활성화 여부")
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true


//    @Comment("제품사용자")
//    val customers: List<OrderItemCustomer>? = null,
//
//
//    @Comment("필터 정보")
//    val filters: List<InstallationFilter>? = null,
}