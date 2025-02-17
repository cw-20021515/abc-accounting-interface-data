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
class CollectLocation {

    @Id
    @IgnoreHash
    @Comment("entity 비교 위한 code")
    var hashCode: String? = null

    var locationId : String? = null

    @Embedded
    var relation : EmbeddableRelation? = null

    @Embedded
    var name : EmbeddableName? = null

    @Embedded
    var location : EmbeddableLocation? = null

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
}