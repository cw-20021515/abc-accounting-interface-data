package com.abc.us.accounting.collects.domain.entity.node

import com.abc.us.accounting.collects.domain.type.EntityNodeStatusEnum
import com.abc.us.accounting.supports.entity.annotation.IgnoreHash
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.type.YesNoConverter
import java.time.OffsetDateTime

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
class EntityNode {
    @Id
    @Comment("Node 고유 식별자")
    var nodeId: String?=null

    @Comment("AccountsReceivable ID")
    var parentNodeId: String?=null

    @Comment("연관 테이블 이름")
    var entityName: String?=null

    @Comment("연관 테이블 D")
    var entityId: String?=null

    @Comment("연관 테이블 value")
    @Column(columnDefinition = "TEXT")
    var entityValue: String?=null

    @Comment("자식 노드간 순서")
    var orderIndex: Int = 0

    @Comment("노드 상태 (예: ACTIVE, INACTIVE)")
    @Enumerated(EnumType.STRING)
    var status : EntityNodeStatusEnum = EntityNodeStatusEnum.ACTIVE

    @Comment("설명")
    var description: String? = null

    @Comment("생성시간")
    var createTime: OffsetDateTime?=null

    @Comment("수정시간")
    var updateTime: OffsetDateTime?= null

    @Comment("활성화 여부")
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true

    // 추가된 children 필드 (JPA에 포함되지 않음)
    @IgnoreHash
    @Transient
    var children: MutableList<EntityNode> = mutableListOf()
}