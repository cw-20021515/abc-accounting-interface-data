package com.abc.us.accounting.collects.service

//import com.abc.us.accounting.collects.domain.entity.node.EntityNode
//import com.abc.us.accounting.collects.domain.repository.EntityNodeRepository
//import jakarta.transaction.Transactional
//import org.springframework.dao.DataIntegrityViolationException
//import org.springframework.data.domain.PageRequest
//import org.springframework.data.domain.Pageable
//import org.springframework.jdbc.core.JdbcTemplate
//import org.springframework.stereotype.Service
//import java.time.OffsetDateTime
//
//@Service
//open class EntityMapService(
//    private val entityNodeRepository: EntityNodeRepository,
//    private val jdbcTemplate: JdbcTemplate
//) {
//
//    // 루트 노드와 하위 트리 전체를 가져오기
//    fun getTree(): List<EntityNode> {
//        val allNodes = entityNodeRepository.findAll()
//        val nodeMap = allNodes.associateBy { it.nodeId }
//        val roots = mutableListOf<EntityNode>()
//
//        allNodes.forEach { node ->
//            if (node.parentNodeId == null) {
//                roots.add(node)
//            } else {
//                // @Transient children을 직접 관리
//                nodeMap[node.parentNodeId]?.children?.add(node)
//            }
//        }
//
//        return roots
//    }
//
//    private fun fetchEntityData(entityName: String, entityId: String): Map<String, Any?>? {
//        val query = "SELECT * FROM $entityName WHERE id = ?"
//        return try {
//            jdbcTemplate.queryForMap(query, entityId)
//        } catch (e: Exception) {
//            null
//        }
//    }
//
//    fun visit(nodeId: String, action: (EntityNode, Map<String, Any?>?) -> Unit) {
//        val node = entityNodeRepository.findById(nodeId).orElseThrow {
//            IllegalArgumentException("Node with id $nodeId not found")
//        }
//
//        val entityData = if (!node.entityName.isNullOrEmpty() && !node.entityId.isNullOrEmpty()) {
//            fetchEntityData(node.entityName!!, node.entityId!!)
//        } else {
//            null
//        }
//
//        action(node, entityData)
//
//        val childNodes = entityNodeRepository.findByParentNodeId(node.nodeId!!)
//        childNodes.forEach { child ->
//            visit(child.nodeId!!, action)
//        }
//    }
//
//    fun addNode(node: EntityNode, parentNodeId: String? = null): EntityNode {
//        try {
//            if (parentNodeId != null) {
//                val parent = entityNodeRepository.findByNodeId(parentNodeId).orElseThrow {
//                    IllegalArgumentException("Parent node not found")
//                }
//                node.parentNodeId = parent.nodeId
//            }
//            return entityNodeRepository.save(node)
//        } catch (e: DataIntegrityViolationException) {
//            throw IllegalArgumentException("Duplicate entity_name and entity_id combination")
//        }
//    }
//
//    fun updateNode(nodeId: String, updatedNode: EntityNode): EntityNode {
//        val node = entityNodeRepository.findByNodeId(nodeId).orElseThrow {
//            IllegalArgumentException("Node not found")
//        }
//        node.apply {
//            entityName = updatedNode.entityName
//            entityId = updatedNode.entityId
//            entityValue = updatedNode.entityValue
//            orderIndex = updatedNode.orderIndex
//            status = updatedNode.status
//            isActive = updatedNode.isActive
//            updateTime = OffsetDateTime.now()
//        }
//        return entityNodeRepository.save(node)
//    }
//
//    fun findNodeById(nodeId: String): EntityNode? {
//        return entityNodeRepository.findByNodeId(nodeId).orElse(null)
//    }
//
//    fun findChildren(parentNodeId: String): List<EntityNode> {
//        return entityNodeRepository.findChildrenByParentId(parentNodeId)
//    }
//    fun findByEntityNameAndEntityId(entityName: String, entityId: String): EntityNode? {
//        return entityNodeRepository.findByEntityNameAndEntityId(entityName, entityId)
//    }
//
//    fun findChildrenWithPaging(parentNodeId: String, page: Int, size: Int): List<EntityNode> {
//        // PageRequest는 Pageable을 구현하고 있으며, 타입 불일치 문제를 해결하기 위한 명시적 타입 캐스팅
//        val pageable: Pageable = PageRequest.of(page, size)
//        return entityNodeRepository.findByParentNodeId(parentNodeId, pageable).content
//    }
//    // 노드 이동
//    @Transactional
//    open fun moveNode(nodeId: String, newParentNodeId: String?) {
//        val node = entityNodeRepository.findByNodeId(nodeId).orElseThrow {
//            IllegalArgumentException("Node not found")
//        }
//        if (newParentNodeId != null) {
//            val newParent = entityNodeRepository.findByNodeId(newParentNodeId).orElseThrow {
//                IllegalArgumentException("New parent node not found")
//            }
//            node.parentNodeId = newParent.nodeId
//        } else {
//            node.parentNodeId = null // 루트로 이동
//        }
//        entityNodeRepository.save(node)
//    }
//
//    // 노드 순서 조정
//    @Transactional
//    open fun reorderNodes(parentNodeId: String, reorderedNodeIds: List<String>) {
//        val children = entityNodeRepository.findChildrenByParentId(parentNodeId)
//        val childrenMap = children.associateBy { it.nodeId }
//        reorderedNodeIds.forEachIndexed { index, nodeId ->
//            val node = childrenMap[nodeId] ?: throw IllegalArgumentException("Node $nodeId not found in children of $parentNodeId")
//            node.orderIndex = index
//            entityNodeRepository.save(node)
//        }
//    }
//}
