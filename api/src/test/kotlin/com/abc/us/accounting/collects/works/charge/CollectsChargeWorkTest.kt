package com.abc.us.accounting.collects.works.charge

import com.abc.us.accounting.collects.domain.repository.*
import com.abc.us.accounting.collects.works.OmsClientStub
import com.abc.us.accounting.collects.works.orderitem.CollectsOrderItemWork
//import com.abc.us.accounting.supports.converter.EpochToISO8856
//import com.abc.us.accounting.supports.converter.ISO8856ToLocalDate
import com.abc.us.accounting.supports.entity.BulkDistinctInserter
import io.mockk.MockKAnnotations
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class CollectsChargeWorkTest @Autowired constructor(
    private val work: CollectsChargeWork,
    private val orderRepository : CollectOrderRepository,
    private val chargeRepository : CollectChargeRepository,
    private val chargeItemRepository : CollectChargeItemRepository,
    private val receiptRepository : CollectReceiptRepository,
    //private val priceRepository : CollectPriceRepository,
    private val taxLineRepository : CollectTaxLineRepository,
    private val contractRepository : CollectContractRepository,
    private val orderItemRepository : CollectOrderItemRepository,
    private val installationRepository: CollectInstallationRepository,
    private val depositRepository: CollectDepositRepository,
    private val customerRepository : CollectCustomerRepository,
    private val channelRepository : CollectChannelRepository,
    private val shippingRepository : CollectShippingRepository,
    private val materialRepository : CollectMaterialRepository,
    private val promotionRepository : CollectPromotionRepository,
    private val bulkInserter : BulkDistinctInserter
) {
    private val omsClient = OmsClientStub()

    private lateinit var collectsOrderItemWork: CollectsOrderItemWork
    private lateinit var collectsChargeWork: CollectsChargeWork

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        collectsChargeWork = CollectsChargeWork(
            xAbcSdkApikey = "test-api-key",
            sortProperty = "createTime",
            pageSize = 100,
            omsClient = omsClient,
            chargeRepository = chargeRepository,
            chargeItemRepository = chargeItemRepository,
            receiptRepository = receiptRepository,
            //priceRepository= priceRepository,
            taxLineRepository = taxLineRepository,
            contractRepository = contractRepository,
            depositRepository = depositRepository,
            bulkInserter = bulkInserter
        )
        collectsOrderItemWork = CollectsOrderItemWork(
            xAbcSdkApikey = "test-api-key",
            sortProperty = "createTime",
            pageSize = 100,
            omsClient = omsClient,
            orderRepository=orderRepository,
            orderItemRepository=orderItemRepository,
            contractRepository=contractRepository,
            installationRepository=installationRepository,
            taxLineRepository=taxLineRepository,
            receiptRepository=receiptRepository,
            channelRepository=channelRepository,
            customerRepository=customerRepository,
            shippingRepository=shippingRepository,
            materialRepository=materialRepository,
            promotionRepository=promotionRepository,
            bulkInserter = bulkInserter
            //chargeRepository=chargeRepository
        )
    }

//    @Test
//    fun `collect 테스트`() {
//        val trailer = AsyncEventTrailer.Builder()
//            .listener("collects/customer")
//            // 2024-12-01 ~ 2024-12-03
//            .addQuery("fromCreateTime", EpochToOffsetDateTime.convert(1733011200000))
//            .addQuery("toCreateTime", EpochToOffsetDateTime.convert(1733187599000))
//            .build(this)
//
//        collectsOrderItemWork.collects(trailer)
//        collectsChargeWork.collects(trailer)
//
//        val charge = chargeRepository.findByChargeIdAndIsActive("0105433700038-0101-50000A-60")
//        charge.shouldNotBeNull()
//
//        val chargeItem = chargeItemRepository.findByChargeItemIdAndIsActive("1Z1oJaMEXzz")
//        chargeItem.shouldNotBeNull()
//        chargeItem.chargeItemId.shouldNotBeNull()
//
////        val priceDetail = priceDetailRepository.findByRelation(CollectChargeItem::class.simpleName!!,"charge_item_id",chargeItem.chargeItemId!!)
////        priceDetail.shouldNotBeNull()
//    }
//
//    @Test
//    fun `makeMockupReceipt test`() {
//
//        val startISO8856 = EpochToISO8856.convert(1733011200000)
//        val endISO8856 = EpochToISO8856.convert(1733187599000)
//
//        val trailer = AsyncEventTrailer.Builder()
//            .listener("collects/customer")
//            // 2024-12-01 ~ 2024-12-03
//            .addQuery("fromCreateTime", startISO8856)
//            .addQuery("toCreateTime", endISO8856)
//            .build(this)
//        collectsOrderItemWork.collects(trailer)
//        collectsChargeWork.collects(trailer)
//
//        val fromTime = ISO8856ToLocalDate.convert(startISO8856)
//        val toTime = ISO8856ToLocalDate.convert(endISO8856)
//
//        val receipts = mutableListOf<CollectReceipt>()
//        val deposits = mutableListOf<CollectDeposit>()
//
//        contractRepository.findActiveContractsWithinCreateTimeRange(fromTime,toTime)?.let { contracts ->
//            contracts.forEach { contract ->
//                val omsCharges = collectsChargeWork.collectCharges(contract.contractId!!)
//
//                val mockupReceipt = CollectReceiptFixture.makeMockupReceipts(omsCharges)
//                val mockupDeposit = CollectReceiptFixture.makeMockupDeposits(omsCharges)
//                receipts.addAll(mockupReceipt)
//                deposits.addAll(mockupDeposit)
//
//            }
//        }
//
//        SaveDistinct(receiptRepository).execute(receipts)
//        SaveDistinct(depositRepository).execute(deposits)
//
//        receipts.size shouldBeGreaterThan 0
//        deposits.size shouldBeGreaterThan 0
//    }

//    @Test
//    fun `jsonToCharge 테스트`() {
//
//        val charges = work.jsonToCharge(jsonData)
//        charges.shouldNotBeNull()
//        charges.size shouldBeGreaterThan 0
//        charges[0].chargeId shouldBeEqual "chargeId-001"
//        charges[0].chargeItems.size?.shouldBeGreaterThan( 0)
//        charges[0].chargeItems[0].chargeItemId shouldBeEqual "chargeItemId-001"
//        charges[0].chargeItems[0].priceDetail.shouldNotBeNull()
//        charges[0].chargeItems[0].priceDetail.currency shouldBeEqual "USD"
//
//        charges[0].payment.shouldNotBeNull()
//        charges[0].payment?.chargeItems.shouldNotBeNull()
//        charges[0].payment?.chargeItems?.size?.shouldBeGreaterThan(0)
//
////        charges[0].payment?.chargeItems?.get(0)?.chargeItemId?.shouldBeEqual("chargeItemId-001")
////        charges[0].payment?.chargeItems?.get(0)?.priceDetail.shouldNotBeNull()
////        charges[0].payment?.chargeItems?.get(0)?.priceDetail?.currency?.shouldBeEqual( "USD")
//    }
//
//    @Test
//    fun `convert toCharge 테스트`() {
//
//        val charges = work.jsonToCharge(jsonData)
//        charges.shouldNotBeNull()
//        val convertedCharges = work.toCharges(charges)
//        convertedCharges.shouldNotBeNull()
//        convertedCharges.size shouldBeGreaterThan 0
//        convertedCharges.get(0).chargeId?.shouldBeEqual("chargeId-001")
//        convertedCharges.get(0).chargeStatus?.shouldBeEqual(ChargeStatusEnum.CREATED)
//    }
//
//    @Test
//    fun `collectChargeItems 테스트`() {
//
//        val omsCharges = work.jsonToCharge(jsonData)
//        omsCharges.shouldNotBeNull()
//
//        val charges = work.toCharges(omsCharges)
//
//        val chargeItems = work.collectChargeItems(omsCharges)
//        chargeItems.size shouldBeEqual 2
//
//        val chargeItemMap = chargeItems.associateBy { it.chargeItemId!! }.toMutableMap()
//        chargeItemMap.get("chargeItemId-001").shouldNotBeNull()
//        chargeItemMap.get("chargeItemId-002").shouldNotBeNull()
//
//        val chargeItem001 = chargeItemMap.get("chargeItemId-001")
//        chargeItem001!!.relation.shouldNotBeNull()
//        chargeItem001!!.relation!!.entity.shouldNotBeNull()
//        chargeItem001!!.relation!!.field.shouldNotBeNull()
//        chargeItem001!!.relation!!.value.shouldNotBeNull()
//
//        chargeItem001!!.relation!!.entity!!.shouldBeEqual (CollectCharge::class.simpleName.toString())
//        chargeItem001!!.relation!!.field!!.shouldBeEqual ("charge_id")
//        chargeItem001!!.relation!!.value!!.shouldBeEqual (charges.get(0).chargeId!!)
//
//
//        chargeItem001!!.chargeId!!.shouldBeEqual("chargeId-001")
//        chargeItem001.chargeItemType!!.shouldBeEqual(ChargeItemEnum.SERVICE_FEE)
//
//        val chargeItem002 = chargeItemMap.get("chargeItemId-002")
//        chargeItem002!!.chargeId!!.shouldBeEqual("chargeId-001")
//        chargeItem002.chargeItemType!!.shouldBeEqual(ChargeItemEnum.SERVICE_FEE)
//
//        chargeItem002!!.relation!!.entity!!.shouldBeEqual (CollectCharge::class.simpleName.toString())
//        chargeItem002!!.relation!!.field!!.shouldBeEqual ("charge_id")
//        chargeItem002!!.relation!!.value!!.shouldBeEqual (charges.get(0).chargeId!!)
//    }
//    @Test
//    fun `collectPayments 테스트`() {
//        val omsCharges = work.jsonToCharge(jsonData)
//        val charges = work.toCharges(omsCharges)
//
//        omsCharges.shouldNotBeNull()
//        charges.shouldNotBeNull()
//        val payments = work.collectPayments(omsCharges)
//        payments.shouldNotBeNull()
//        payments.size shouldBeEqual 1
//        payments[0].paymentId!!.shouldBeEqual("invoiceId-001")
//
//        payments[0]!!.relation!!.entity!!.shouldBeEqual (CollectCharge::class.simpleName.toString())
//        payments[0]!!.relation!!.field!!.shouldBeEqual ("charge_id")
//        payments[0]!!.relation!!.value!!.shouldBeEqual (charges.get(0).chargeId!!)
//    }
//
//    @Test
//    fun `saveCharges 테스트`() {
//        val omsCharges = work.jsonToCharge(jsonData)
//        omsCharges.shouldNotBeNull()
//
//        val result = work.saveCharges(omsCharges)
//        result.shouldNotBeNull()
//
//        val charge = service.access<Charge,CollectCharge> {
//            findChargeById("chargeId-001")
//        }
//
//        //val charge = service.accessCharge().findChargeById("chargeId-001")
//        charge.shouldNotBeNull()
//        charge.chargeId?.shouldBeEqual("chargeId-001")
//        charge.chargeStatus?.shouldBeEqual(ChargeStatusEnum.CREATED)
//    }
//
//    @Test
//    fun `saveChargeItems 테스트`() {
//        val omsCharges = work.jsonToCharge(jsonData)
//        val charges = work.toCharges(omsCharges)
//        charges.shouldNotBeNull()
//        omsCharges.shouldNotBeNull()
//
//        val result = work.saveChargeItems(omsCharges)
//        result.shouldNotBeNull()
//        result.size shouldBeEqual 2
//
//        val chargeItem001 = service.access<Charge,CollectChargeItem?> {
//            findChargeItemById("chargeItemId-001")
//        }
//
//        //val chargeItem001 = service.findChargeItemById("chargeItemId-001")
//        chargeItem001!!.relation.shouldNotBeNull()
//        chargeItem001!!.relation!!.entity.shouldNotBeNull()
//        chargeItem001!!.relation!!.field.shouldNotBeNull()
//        chargeItem001!!.relation!!.value.shouldNotBeNull()
//
//        chargeItem001!!.relation!!.entity!!.shouldBeEqual (CollectCharge::class.simpleName.toString())
//        chargeItem001!!.relation!!.field!!.shouldBeEqual ("charge_id")
//        chargeItem001!!.relation!!.value!!.shouldBeEqual (charges.get(0).chargeId!!)
//
//        chargeItem001!!.chargeId!!.shouldBeEqual("chargeId-001")
//    }
//    @Test
//    fun `savePayment 테스트`() {
//        val omsCharges = work.jsonToCharge(jsonData)
//        val charges = work.toCharges(omsCharges)
//        charges.shouldNotBeNull()
//        omsCharges.shouldNotBeNull()
//
//        val result = work.savePayments(omsCharges)
//        result.shouldNotBeNull()
//        result.size shouldBeEqual 1
//
//        val payment001 = service.findPaymentById("invoiceId-001")
//        payment001!!.relation.shouldNotBeNull()
//        payment001!!.relation!!.entity.shouldNotBeNull()
//        payment001!!.relation!!.field.shouldNotBeNull()
//        payment001!!.relation!!.value.shouldNotBeNull()
//
//        payment001!!.relation!!.entity!!.shouldBeEqual (CollectCharge::class.simpleName.toString())
//        payment001!!.relation!!.field!!.shouldBeEqual ("charge_id")
//        payment001!!.relation!!.value!!.shouldBeEqual (charges.get(0).chargeId!!)
//    }

//    @Test
//    fun `savePriceDetail 테스트`() {
//        val omsCharges = work.jsonToCharge(jsonData)
//        val charges = work.toCharges(omsCharges)
//        val chargeItems = work.saveChargeItems(omsCharges)
//
//        charges.shouldNotBeNull()
//        omsCharges.shouldNotBeNull()
//        chargeItems.shouldNotBeNull()
//
//
//
//        val result = work.savePriceDetails(omsCharges)
//        result.shouldNotBeNull()
//        result.size shouldBeEqual 2
//
//        val allPriceDetails = service.findAllPriceDetail()
//        allPriceDetails.shouldNotBeNull()
//        allPriceDetails.size shouldBeEqual 2
//
//        allPriceDetails.forEach { detail ->
//            val chargeItems = detail.relation?.let {r-> service.findRelation(r, CollectChargeItem::class) }
//            chargeItems.shouldNotBeEmpty()
//        }
//    }

//    @Test
//    fun `saveTaxLines 테스트`() {
//        val omsCharges = work.jsonToCharge(jsonData)
//        val charges = work.toCharges(omsCharges)
//        val chargeItems = work.saveChargeItems(omsCharges)
//        charges.shouldNotBeNull()
//        omsCharges.shouldNotBeNull()
//        chargeItems.shouldNotBeNull()
//
//
//
//        val result = work.saveTaxLines(omsCharges)
//        result.shouldNotBeNull()
//        result.size shouldBeEqual 2
//
//        val allTaxLine = service.findAllTaxLine()
//        allTaxLine.shouldNotBeNull()
//        allTaxLine.size shouldBeEqual 2
//
//        allTaxLine.forEach { detail ->
//            val priceDetail = detail.relation?.let {r-> service.findRelation(r, CollectPrice::class) }
//            priceDetail.shouldNotBeEmpty()
//        }
//    }

//    @Test
//    fun `saveLocations 테스트`() {
//        val omsCharges = work.jsonToCharge(jsonData)
//        val charges = work.toCharges(omsCharges)
//        val chargeItems = work.saveChargeItems(omsCharges)
//        charges.shouldNotBeNull()
//        omsCharges.shouldNotBeNull()
//        chargeItems.shouldNotBeNull()
//
//
//
////        val result = work.saveLocations(omsCharges)
////        result.shouldNotBeNull()
////        result.size shouldBeEqual 1
////
////        val allLocation = service.findAllLocation()
////        allLocation.shouldNotBeNull()
//
////        allLocation.forEach { detail ->
////            val payment = detail.relation?.let {r-> service.findRelation(r, CollectPayment::class) }
////            payment.shouldNotBeEmpty()
////        }
//    }

//    @Test
//    fun `saveFromJson 테스트`() {
//        work.saveFromJson(jsonData)
//        val charge = service.findChargeById("chargeId-001")
//        val chargeItem = service.findChargeItemById("chargeItemId-001")
//        val payment = service.findPaymentById("paymentId-001")
//        val taxLine = service.findAllTaxLine()
//        val priceDetails = service.findAllPriceDetail()
////        val allLocation = service.findAllLocation()
//
//        charge.shouldNotBeNull()
//        chargeItem.shouldNotBeNull()
//        payment.shouldNotBeNull()
//        payment.shouldNotBeNull()
//        taxLine.shouldNotBeNull()
//        priceDetails.shouldNotBeNull()
////        allLocation.shouldNotBeNull()
//    }

}