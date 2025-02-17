package com.abc.us.accounting.collects.domain.entity.collect

import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableLocation
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableName
import com.abc.us.accounting.supports.entity.annotation.IgnoreHash
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.Convert
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Comment
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.type.YesNoConverter
import java.time.OffsetDateTime

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CollectEmployee(
    @Id
    @IgnoreHash
    @Comment("entity 비교 위한 code")
    var hashCode: String? = null,

    @Comment("직원 ID")
    var employeeId: String,

    @Comment("소속 회사 코드")
    var companyCode: String,

    @Comment("소속 부서ID")
    var departmentId: String,

    @Embedded
    var name : EmbeddableName,

    @Embedded
    var location : EmbeddableLocation,

    @Comment("직원 지급 계좌번호") //TODO : 별도로 확인 필요
    var paymentAccountId: String? = null,

    @Comment("지급시 사용되는 통화") //TODO : 별도로 확인 필요
    var currency: String? = null,

    @Comment("비고")
    var remark: String? = null,

    @IgnoreHash
    @Comment("등록 일시")
    var createTime: OffsetDateTime? = null,

    @IgnoreHash
    @Comment("수정 일시")
    var updateTime: OffsetDateTime? = null,

    @IgnoreHash
    @Comment("활성화 여부")
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true
) {

}