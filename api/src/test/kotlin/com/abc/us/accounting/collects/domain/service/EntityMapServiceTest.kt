//package com.abc.us.accounting.collects.domain.service
//
//import com.abc.us.accounting.collects.domain.entity.node.EntityNode
//import com.abc.us.accounting.collects.domain.repository.EntityNodeRepository
//import com.abc.us.accounting.collects.domain.type.EntityNodeStatus
//import org.junit.jupiter.api.Assertions.*
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.data.domain.PageRequest
//import org.springframework.test.context.ActiveProfiles
//import org.springframework.transaction.annotation.Transactional
//import java.time.OffsetDateTime
//
//@SpringBootTest(properties = ["spring.profiles.active=test"])
//@ActiveProfiles("test")
//class EntityMapServiceTest @Autowired constructor(
//    private val entityMapService: EntityMapService,
//    private val entityNodeRepository: EntityNodeRepository
//) {
//
//    @BeforeEach
//    fun setup() {
//        // 기존 데이터 삭제
//        entityNodeRepository.deleteAll()
//
//        // 테스트 데이터 생성
//        val root1 = EntityNode().apply {
//            nodeId = "root1"
//            entityName = "RootNode1"
//            orderIndex = 0
//        }
//
//        val root2 = EntityNode().apply {
//            nodeId = "root2"
//            entityName = "RootNode2"
//            orderIndex = 1
//        }
//
//        val child1 = EntityNode().apply {
//            nodeId = "child1"
//            parentNodeId = "root1"
//            entityName = "Child1"
//            orderIndex = 0
//        }
//
//        val child2 = EntityNode().apply {
//            nodeId = "child2"
//            parentNodeId = "root1"
//            entityName = "Child2"
//            orderIndex = 1
//        }
//
//        val child3 = EntityNode().apply {
//            nodeId = "child3"
//            parentNodeId = "root2"
//            entityName = "Child3"
//            orderIndex = 0
//        }
//
//        entityNodeRepository.saveAll(listOf(root1, root2, child1, child2, child3))
//    }
//
//    @Test
//    fun `getTree - 트리 구조 반환 테스트`() {
//        val tree = entityMapService.getTree()
//
//        assertEquals(2, tree.size) // 루트 노드 2개 (root1, root2)
//
//        val root1Children = tree.first { it.nodeId == "root1" }.children
//        assertEquals(2, root1Children.size) // root1의 자식 노드 수
//
//        val root2Children = tree.first { it.nodeId == "root2" }.children
//        assertEquals(1, root2Children.size) // root2의 자식 노드 수
//    }
//
//    @Test
//    fun `addNode - 새로운 노드 추가 테스트`() {
//        val newNode = EntityNode().apply {
//            nodeId = "newChild"
//            entityName = "NewChild"
//            orderIndex = 2
//        }
//
//        val addedNode = entityMapService.addNode(newNode, "root1")
//        assertEquals("root1", addedNode.parentNodeId)
//
//        val root1Children = entityMapService.findChildren("root1")
//        assertTrue(root1Children.any { it.nodeId == "newChild" }) // root1의 자식으로 추가
//    }
//
//    @Test
//    fun `updateNode - 노드 상태 수정 테스트`() {
//        val updatedNode = EntityNode().apply {
//            entityName = "UpdatedChild1"
//            entityId = "updated-entity-id"
//            entityValue = "new-value"
//            orderIndex = 0
//            status = EntityNodeStatus.ACTIVE
//            isActive = true
//            updateTime = OffsetDateTime.now()
//        }
//
//        val result = entityMapService.updateNode("child1", updatedNode)
//        assertEquals("UpdatedChild1", result.entityName)
//        assertEquals("updated-entity-id", result.entityId)
//        assertEquals("new-value", result.entityValue)
//        assertTrue(result.isActive)
//    }
//
//    @Test
//    fun `findNodeById - 특정 노드 조회 테스트`() {
//        val node = entityMapService.findNodeById("child1")
//        assertNotNull(node)
//        assertEquals("child1", node?.nodeId)
//    }
//
//    @Test
//    fun `findChildren - 특정 부모의 자식 노드 조회 테스트`() {
//        val children = entityMapService.findChildren("root1")
//        assertEquals(2, children.size)
//        assertEquals("child1", children[0].nodeId)
//        assertEquals("child2", children[1].nodeId)
//    }
//
//    @Test
//    fun `findByEntityNameAndEntityId - 엔티티 이름과 ID로 노드 검색`() {
//        val node = entityMapService.findByEntityNameAndEntityId("Child1", "child1")
//        assertNotNull(node)
//        assertEquals("Child1", node?.entityName)
//    }
//
//    @Test
//    fun `findChildrenWithPaging - 페이징 처리된 자식 노드 검색 테스트`() {
//        val pageable = PageRequest.of(0, 1) // 첫 번째 페이지, 한 페이지 크기 1
//        val page = entityMapService.findChildrenWithPaging("root1", 0, 1)
//
//        assertEquals(1, page.size) // 한 페이지에 하나의 결과
//        assertEquals("child1", page[0].nodeId)
//    }
//
//    @Test
//    @Transactional
//    fun `moveNode - 노드 이동 테스트`() {
//        entityMapService.moveNode("child1", "root2")
//
//        val movedNode = entityMapService.findNodeById("child1")
//        assertEquals("root2", movedNode?.parentNodeId)
//
//        val root2Children = entityMapService.findChildren("root2")
//        assertTrue(root2Children.any { it.nodeId == "child1" }) // root2의 자식으로 이동
//    }
//
//    @Test
//    @Transactional
//    fun `reorderNodes - 자식 노드 순서 변경 테스트`() {
//        entityMapService.reorderNodes("root1", listOf("child2", "child1"))
//
//        val reorderedNodes = entityMapService.findChildren("root1").sortedBy { it.orderIndex }
//        assertEquals("child2", reorderedNodes[0].nodeId)
//        assertEquals("child1", reorderedNodes[1].nodeId)
//    }
//}
