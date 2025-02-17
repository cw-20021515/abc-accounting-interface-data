package com.abc.us.accounting.collects.domain.entity.collect

import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableLocation
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableName
import com.abc.us.accounting.supports.entity.annotation.IgnoreHash
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Comment

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CollectDepartment(
    @Id
    @IgnoreHash
    @Comment("entity 비교 위한 code")
    var hashCode: String? = null,

    @Comment("부서ID")
    var departmentId: String,

    @Comment("상위 부서ID")
    var parentDepartmentId: String,

    @Embedded
    @Comment("부서 이름 정보")
    var name : EmbeddableName,

    @Embedded
    @Comment("부서 위치 정보")
    var location : EmbeddableLocation,

    @IgnoreHash
    @Transient
    val parents : List<CollectDepartment> = emptyList()
) {

}