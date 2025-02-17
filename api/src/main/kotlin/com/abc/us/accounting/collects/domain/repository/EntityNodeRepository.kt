package com.abc.us.accounting.collects.domain.repository

import com.abc.us.accounting.collects.domain.entity.node.EntityNode
import io.lettuce.core.dynamic.annotation.Param
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import java.util.*

@Repository
interface EntityNodeRepository : JpaRepository<EntityNode, String> {

    @Query("SELECT e FROM EntityNode e WHERE e.parentNodeId IS NULL")
    fun findRootNodes(): List<EntityNode>

    @Query("SELECT e FROM EntityNode e WHERE e.parentNodeId = :parentNodeId ORDER BY e.orderIndex")
    fun findChildrenByParentId(@Param("parentNodeId") parentNodeId: String): List<EntityNode>

    @Modifying
    @Transactional
    @Query("DELETE FROM EntityNode e WHERE e.parentNodeId = :parentNodeId")
    fun deleteChildrenByParentId(@Param("parentNodeId") parentNodeId: String)

    @Query("SELECT e FROM EntityNode e WHERE e.nodeId = :nodeId")
    fun findByNodeId(@Param("nodeId") nodeId: String): Optional<EntityNode>

    fun findByParentNodeId(parentNodeId: String): List<EntityNode>


    // 복합 조건 검색 메서드 추가
    fun findByEntityNameAndEntityId(entityName: String, entityId: String): EntityNode?

    // 부모 노드를 기준으로 자식 노드를 페이징 처리
    fun findByParentNodeId(parentNodeId: String, pageable: Pageable): Page<EntityNode>

}