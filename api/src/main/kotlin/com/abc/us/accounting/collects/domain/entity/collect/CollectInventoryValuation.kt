package com.abc.us.accounting.collects.domain.entity.collect

import com.abc.us.accounting.logistics.domain.type.InventoryAssetGradeType
import com.abc.us.accounting.logistics.domain.type.InventoryAssetGradeTypeConverter
import com.abc.us.accounting.logistics.domain.type.MovementType
import com.abc.us.accounting.logistics.domain.type.MovingAverageMethod
import com.abc.us.accounting.supports.entity.annotation.IgnoreHash
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.type.YesNoConverter
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

@Comment("물류 재고 원가 데이터")
@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CollectInventoryValuation (
    @Id
    @IgnoreHash
    @Comment("entity 고유 식별자")
    var hashCode: String? = null,

//    @Comment("기준 시간")
//    var baseTime: OffsetDateTime,

    @Comment("수불부 발생 시간")
    var issuedTime: OffsetDateTime,

//    @Comment("원가 기록 시간")
//    var recordTime: OffsetDateTime,

    @Comment("자재 ID")
    var materialId: String,

    @Comment("통화")
    var currency: Currency,

//    @Embedded
//    var relation : EmbeddableRelation,

//    @Comment("자재의 이름")
//    var materialName: String,
//
//    @Comment("자재의 모델명")
//    var modelName: String? = null,

    @Comment("자재 수불 유형")
    @Enumerated(EnumType.STRING)
    var movementType: MovementType,

//    @Comment("자재의 구분")
//    var materialProductType: String,

    @Comment("자재등급")
//    @Enumerated(EnumType.STRING)
    @Convert(converter = InventoryAssetGradeTypeConverter::class)
    var gradeType: InventoryAssetGradeType,

    @Comment("재고 단가")
    var stockAvgUnitPrice: BigDecimal,

    @Comment("재고 원가 계산 방법")
    @Enumerated(EnumType.STRING)
    var movingAvgMethod: MovingAverageMethod = MovingAverageMethod.MONTHLY,

    @Comment("적요")
    var remark: String? = null,

    @IgnoreHash
    @Comment("회계 시스템 내 생성 시간")
    var createTime: OffsetDateTime = OffsetDateTime.now(),

    @IgnoreHash
    @Comment("회계 시스템 내 업데이트 시간")
    var updateTime: OffsetDateTime = OffsetDateTime.now(),

    @IgnoreHash
    @Comment("활성화 여부")
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true

) {

}