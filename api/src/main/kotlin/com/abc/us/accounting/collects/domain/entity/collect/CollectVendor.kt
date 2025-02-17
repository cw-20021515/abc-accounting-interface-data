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
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.type.YesNoConverter
import java.time.OffsetDateTime

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
class CollectVendor {
    @Id
    @IgnoreHash
    @Comment("entity 비교 위한 code")
    var hashCode: String? = null

    @IgnoreHash
    @Comment("등록 일시")
    var createTime: OffsetDateTime? = null

    @IgnoreHash
    @Comment("수정 일시")
    var updateTime: OffsetDateTime? = null

    @IgnoreHash
    @Comment("활성화 여부")
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true

    @Embedded
    var relation : EmbeddableRelation? = null

    @Comment("공급업체 코드")
    var vendorId: String? = null

    @Comment("회사 소속의 직원일 경우 고용 회사의 Code")
    var employerCode: String? = null

    @Comment("소속 부서ID")
    var departmentCode: String? = null

    @Comment("부서 이름")
    var departmentName: String? = null


    @Comment("회사 코드")
    var companyId: String? = null

    @Embedded
    var name : EmbeddableName? = null

    @Embedded
    var location : EmbeddableLocation? = null


    @Comment("공급업체에 대한 결제 조건")
    var terms: String? = null

    @Comment("공급업체 계좌번호") //TODO : 별도로 확인 필요
    var acctNum: String? = null

    @Comment("공급업체 세금ID") //TODO : 별도로 확인 필요
    var taxIdentifier: String? = null

    @Comment("공급업체와 거래하는 통화") //TODO : 별도로 확인 필요
    var currency: String? = null

    @Comment("공급업체 사업자 번호") //TODO : 별도로 확인 필요
    var businessNumber: String? = null

    @Comment("공급 업체에 대한 메모 --> QBO 에서는 Memo")
    var remark: String? = null

    @Comment("공급 업체에 대한 메모 --> QBO 에서는 Memo")
    var description: String? = null
}