//package com.abc.us.accounting.collects.works.customer
//
//import com.abc.us.accounting.collects.domain.repository.CollectCustomerRepository
//import com.abc.us.accounting.collects.works.OmsClientStub
//import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
//import com.abc.us.accounting.supports.converter.EpochToOffsetDateTime
//import com.abc.us.accounting.supports.entity.BulkDistinctInserter
//import io.kotest.matchers.collections.shouldNotBeEmpty
//import io.kotest.matchers.ints.shouldBeGreaterThan
//import io.mockk.MockKAnnotations
//import io.mockk.mockk
//import org.junit.jupiter.api.BeforeEach
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.context.ApplicationEventPublisher
//import org.springframework.test.context.ActiveProfiles
//import java.net.URI
//import java.net.http.HttpClient
//import java.net.http.HttpRequest
//import java.net.http.HttpResponse
//import kotlin.test.Test
//
//@SpringBootTest(properties = ["spring.profiles.active=test"])
//@ActiveProfiles("test")
//class CollectsCustomerWorkTest @Autowired constructor (
//    private val customerRepository : CollectCustomerRepository,
//    private val bulkInserter : BulkDistinctInserter,
//){
//    private val eventPublisher = mockk<ApplicationEventPublisher>()
//    private val omsClient = OmsClientStub()
//    private lateinit var collectsCustomerItemWork: CollectsCustomerWork
//
//
//    @BeforeEach
//    fun setUp() {
//        MockKAnnotations.init(this)
//        collectsCustomerItemWork = CollectsCustomerWork(
//            xAbcSdkApikey = "test-api-key",
//            sortProperty = "createTime",
//            pageSize = 100,
//            omsClient = omsClient,
//            customerRepository = customerRepository,
//            eventPublisher = eventPublisher,
//            bulkInserter = bulkInserter
//        )
//    }
//
//    @Test
//    fun `collect customer TEST`() {
//
//        val trailer = AsyncEventTrailer.Builder()
//            .listener("collects/customer")
//            .addQuery("fromCreateTime", EpochToOffsetDateTime.convert(1704095221000))
//            .addQuery("toCreateTime", EpochToOffsetDateTime.convert(1729831325000))
//            .build(this)
//
//        collectsCustomerItemWork.collects(trailer)
//
//        val saveCustomer = customerRepository.findAll()
//
//        saveCustomer.shouldNotBeEmpty()
//        saveCustomer.size.shouldBeGreaterThan(1)
//
//
//    }
//
//    @Test
//    fun `entry location TEST`() {
//        val accessToken = "eyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiYWxnIjoiZGlyIn0..eznnazOAloc7y865EMoQCg.T6ByzPlzkCe_8y5IrkzP19kJuqU4pTpvjEEWBDFLpLSmgKYKZbD3iuvuCrS2p9TOB0D63PrTk5JvYxeVeUWTuWB_K6oCa6SRh1KUKe5xJGrDfVqzk_f1uENxMs8dXuhE9_2Yt9Vz78CFUGFo_F_bOx4Q0OjVpbIaqRwGwqvU6ZwSk4T3qRjYrKom8TzwZbPpwnpcKsMU-6M2Kp5hnCMiFtooSGuudHVzYiwNZtHWhOyGStdLEZc4lGgfhnyTa6GVVpUvsIHYpOdlbacyCwbK6VhYSUnLs2sR_d184f-fVU8EHmF_JO0soNnxUeSOOktxjIIirXD1nW_IoZtzQyNzytaVJ6ATLafCRo0BP_3LqyA1XFYotTIdmqiXIVmfTjkWzRi6DC39XsKP_RMIKZ1LIqcpglpaFYAgAiuJhK20JILxZCz4EElxf6ILNf2s7zDPzv3bPfSIwAM1IqjrMEGplMNMsHvjaDW3Gnefwc6DN1vwAj3k5Uf10TV65Isot8spzOvzk4lxcJ1UuEI-YoHVhM5b4OCXbxh-2V4Ti05NAcf9I274ELUOENmHWIyjPMDKZLun9LhJaEPMPbeSxyHzAPllsOQA35CpbZQgQ5_PF1UMKpEtAWL8SaAG6mAlb-gXmhrr4ygpbw3ZCfWZaxSYVcyoJoHa_qWfWpfQCUWByC9aeoftJ-m0SLn65F67-kpAtmxAduBx6uT91a-OKlEhpbIvom5CWTcgafNYQo2hSlbpPYyhJcl1OXTq4S088LfAXat0rwV9Z2pYZN_8NsR79LRmAJ7hVi1nmovMLr3g_IlOAMnXLu5KxMbKfwoQCdbxGY1Lx7wlHi6ZwdbkcT1hLRoZsj24p09KHge2aJ5ZXeQBXTgdhdDIYKhisppU_PKOD06cVDZxSZj7O63BO75o8g.buUO39AIjcpj4QV_6s8YNQ" // OAuth 2.0 토큰
//        val realmId = "9341453583543323" // QuickBooks 회사 파일 ID
//
//        // API 요청 본문
//        val json = """
//        {
//            "Name": "New York Office",
//            "Active": true,
//            "SubLocation": false
//        }
//
//        """.trimIndent()
//
//        // API 호출
//        val client: HttpClient = HttpClient.newHttpClient()
//        val request: HttpRequest = HttpRequest.newBuilder()
//            .uri(URI.create("https://quickbooks.api.intuit.com/v3/company/$realmId/location"))
//            .header("Authorization", "Bearer $accessToken")
//            .header("Content-Type", "application/json")
//            .POST(HttpRequest.BodyPublishers.ofString(json))
//            .build()
//
//        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
//
//        // 결과 출력
//        System.out.println(response.body())
//    }
//}