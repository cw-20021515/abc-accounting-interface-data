package com.abc.us.oms.domain.promotiontarget.entity

import com.abc.us.oms.domain.common.config.AuditTimeOnlyEntity
import com.abc.us.oms.domain.promotion.entity.Promotion
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.*
import org.hibernate.annotations.Type

data class Rule(
    val field: String? = null,
    val operator: String? = null,
    val valueSource: String? = null,
    val value: String? = null,
    val combinator: String? = null,
    val rules: List<Rule>? = null,
    val not: Boolean = false,
)

//@Entity
//@Table(name = "promotion_target", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class PromotionTarget(
    @Id
    val id: String = "",//SnowflakeId.generateId().toBase62(),
    @Column(name = "promotion_id")
    val promotionId: String? = null,
    @Type(JsonBinaryType::class)
    @Column(name = "target_data", columnDefinition = "jsonb")
    var targetData: Rule? = null,
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "promotion_id", referencedColumnName = "id", updatable = false, insertable = false)
    val promotion: Promotion? = null,
) : AuditTimeOnlyEntity()
