//
//import com.abc.us.accounting.ApiMain
//import com.abc.us.accounting.collects.domain.repository.*
//import com.abc.us.accounting.collects.works.OmsClientStub
//import com.abc.us.accounting.collects.works.order.CollectsOrderWork
//import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
//import com.abc.us.accounting.supports.converter.EpochToOffsetDateTime
//import com.abc.us.accounting.supports.entity.BulkDistinctInserter
//import io.kotest.matchers.collections.shouldNotBeEmpty
//import io.mockk.MockKAnnotations
//import org.junit.jupiter.api.BeforeEach
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.test.context.ActiveProfiles
//import kotlin.test.Test
//
//@SpringBootTest(classes = [ApiMain::class], properties = ["spring.profiles.active=test"])
//@ActiveProfiles("test")
//class CollectsOrderWorkTest @Autowired constructor (
//    private var orderRepository: CollectOrderRepository,
//    private var channelRepository: CollectChannelRepository,
//    private var locationRepository: CollectLocationRepository,
//    private val taxLineRepository: CollectTaxLineRepository,
//    private val receiptRepository: CollectReceiptRepository,
//    private val orderItemRepository : CollectOrderItemRepository,
//    private val contractRepository : CollectContractRepository,
//    private val installationRepository: CollectInstallationRepository,
//    private val customerRepository : CollectCustomerRepository,
//    private val shippingRepository : CollectShippingRepository,
//    private val materialRepository : CollectMaterialRepository,
//    private val promotionRepository : CollectPromotionRepository,
//    private val bulkInserter : BulkDistinctInserter
//) {
//
////    @MockBean
////    private lateinit var omsClient: OmsClient
//    //val omsClient = mockk<OmsClient>(relaxed = true)
//    private val omsClient = OmsClientStub() // Stub 인스턴스 생성
//
//    private lateinit var collectsOrderWork: CollectsOrderWork
//    @BeforeEach
//    fun setUp() {
//        MockKAnnotations.init(this, relaxUnitFun = true) // Mock 초기화
//
//        collectsOrderWork = CollectsOrderWork(
//            xAbcSdkApikey = "test-api-key",
//            sortProperty = "createTime",
//            pageSize = 10000,
//            omsClient = omsClient,
//            orderRepository=orderRepository,
//            taxLineRepository=taxLineRepository,
//            receiptRepository=receiptRepository,
//            customerRepository=customerRepository,
//            bulkInserter = bulkInserter
//        )
//    }
//    @Test
//    fun `collects method should save orders, channels, and contacts`() {
//
//        val trailer = AsyncEventTrailer.Builder()
//            .listener("collects/order")
//            .addQuery("fromCreateTime", EpochToOffsetDateTime.convert(1704095221000))
//            .addQuery("toCreateTime", EpochToOffsetDateTime.convert(1729831325000))
//            .build(this)
//
//        // Act
//        collectsOrderWork.collects(trailer)
//
//        // Assert
//        val savedOrders = orderRepository.findAll()
//        val savedChannels = channelRepository.findAll()
//        val savedContacts = locationRepository.findAll()
//
//        savedOrders.shouldNotBeEmpty()
//        savedChannels.shouldNotBeEmpty()
//        savedContacts.shouldNotBeEmpty()
////        savedChannels.size.shouldBeGreaterThan(1)
////        savedChannels[0].channelId!!.shouldBeEqual("mall001")
//    }
//}