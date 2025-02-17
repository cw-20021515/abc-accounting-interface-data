//package com.abc.us.accounting.collects.works.orderitem
//
//import com.abc.us.accounting.collects.domain.repository.*
//import com.abc.us.accounting.collects.helper.OmsEntityOrderItemMutableList
//import com.abc.us.accounting.collects.works.OmsClientStub
//import com.abc.us.accounting.collects.works.orderitem.CollectsOrderItemWork.Builder
//import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
//import com.abc.us.accounting.supports.converter.EpochToOffsetDateTime
//import com.abc.us.accounting.supports.entity.BulkDistinctInserter
//import com.abc.us.generated.models.ResourceHistoryOperation
//import io.kotest.matchers.collections.shouldBeEmpty
//import io.kotest.matchers.collections.shouldNotBeEmpty
//import io.mockk.MockKAnnotations
//import jakarta.persistence.EntityManager
//import jakarta.persistence.Query
//import org.junit.jupiter.api.BeforeEach
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.test.context.ActiveProfiles
//import java.time.LocalDate
//import kotlin.test.Test
//
//@SpringBootTest(properties = ["spring.profiles.active=test"])
//@ActiveProfiles("test")
//class CollectsOrderItemWorkTest @Autowired constructor (
//    private val entityManager: EntityManager,
//    private val orderRepository : CollectOrderRepository,
//    private val orderItemRepository : CollectOrderItemRepository,
//    private val contractRepository : CollectContractRepository,
//    private val installationRepository: CollectInstallationRepository,
//    private val taxLineRepository : CollectTaxLineRepository,
//    private val receiptRepository : CollectReceiptRepository,
//    private val channelRepository : CollectChannelRepository,
//    private val customerRepository : CollectCustomerRepository,
//    private val shippingRepository : CollectShippingRepository,
//    private val materialRepository : CollectMaterialRepository,
//    private val promotionRepository : CollectPromotionRepository,
//    private val bulkInserter : BulkDistinctInserter
//){
//
//    private val omsClient = OmsClientStub()
//    private lateinit var collectsOrderItemWork: CollectsOrderItemWork
//    @BeforeEach
//    fun setUp() {
//        MockKAnnotations.init(this)
//        collectsOrderItemWork = CollectsOrderItemWork(
//            xAbcSdkApikey = "test-api-key",
//            sortProperty = "createTime",
//            pageSize = 100,
//            omsClient = omsClient,
//            orderRepository=orderRepository,
//            orderItemRepository=orderItemRepository,
//            contractRepository=contractRepository,
//            installationRepository=installationRepository,
//            taxLineRepository=taxLineRepository,
//            receiptRepository=receiptRepository,
//            channelRepository=channelRepository,
//            customerRepository=customerRepository,
//            shippingRepository=shippingRepository,
//            materialRepository=materialRepository,
//            promotionRepository=promotionRepository,
//            bulkInserter = bulkInserter
//        )
//    }
////    fun jsonToOrderItemsView(jsonData: String): List<OrderItemView> {
////        val converter = JsonConverter()
////        try {
////            val response = converter.toObj(jsonData, OrderItemsViewResponse::class.java)
////            return response?.data?.items?: emptyList()
////            //return response?.items ?: emptyList()
////        } catch (e: Exception) {
////            throw IllegalArgumentException("Failed to parse JSON: ${e.message}", e)
////        }
////    }
////
////    fun collectOrdersView() : MutableMap<String, OrderItemView> {
////        val jsonResponse = JsonHelper.readFromFile("order_items.json", ChannelBuilderTest::class)
////        val ordersView = jsonToOrderItemsView(jsonResponse)
////
////        val orderItems = mutableMapOf<String, OrderItemView>()
////        ordersView.forEach { view ->orderItems[view.orderItemId] = view}
////        return orderItems
////    }
//
//    fun findCustomerIdsOnlyInOrders(): List<String?> {
//        val queryStr = """
//        SELECT DISTINCT o.customer_id
//        FROM collect_order o
//        LEFT JOIN collect_customer c
//        ON o.customer_id = c.customer_id
//        WHERE c.customer_id IS NULL
//    """.trimIndent()
//
//        val query: Query = entityManager.createNativeQuery(queryStr)
//        return query.resultList as List<String?>
//    }
//    @Test
//    fun `CollectOrderItem customer 갯수 검증`() {
//
//        val fromDate = LocalDate.of(2024, 12, 1)
//        val toDate = LocalDate.of(2024, 12, 2)
//
//
//        var insertCustomers = 0
//        collectsOrderItemWork.collectOrderItems(fromDate,toDate, ResourceHistoryOperation.INSERT ){ orderItems ->
//            collectsOrderItemWork.bulkInsert(ResourceHistoryOperation.INSERT,
//                Builder().execute( OmsEntityOrderItemMutableList(orderItems)))
//            true
//        }
//
//        var updateCustomers = 0
//        collectsOrderItemWork.collectOrderItems(fromDate,toDate, ResourceHistoryOperation.UPDATE ){ orderItems ->
//            collectsOrderItemWork.bulkInsert(ResourceHistoryOperation.UPDATE,
//                Builder().execute( OmsEntityOrderItemMutableList(orderItems)))
//            true
//        }
//
//        val customerIds = findCustomerIdsOnlyInOrders()
//        customerIds.shouldBeEmpty()
//    }
//}
