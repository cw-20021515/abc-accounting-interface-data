package com.abc.us.accounting.supports.client

//import com.abc.us.generated.models.GetResourceHistoryResourceHistorySearchParameter
//import com.abc.us.generated.models.ResourceHistoryOperation
//import com.abc.us.generated.models.ResourceName
//import io.kotest.core.spec.style.AnnotationSpec
//import io.kotest.matchers.equals.shouldBeEqual
//import io.kotest.matchers.nulls.shouldNotBeNull
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.data.domain.Sort
//import org.springframework.http.HttpStatus
//import java.time.LocalDate
//
//@SpringBootTest(properties = ["spring.profiles.active=test"])
//@ActiveProfiles("test")
//class OmsClientTest(
//    @Value("\${abc-sdk.api-key}")
//    private val xAbcSdkApikey: String,
//    @Value("\${collects.read.page.sort-by:createTime}")
//    private val sortProperty: String,
//    @Value("\${collects.read.page.max-size:10}")
//    private val pageSize: Int = 0,
//    private val omsClient : OmsClient
//) : AnnotationSpec() {
//    @Test
//    fun `collects all customers`() {
//
//        val responseEntity = omsClient.getCustomers(
//            xAbcSdkApikey = xAbcSdkApikey,
//            current = 1,
//            size = 10,
//            direction = Sort.Direction.DESC.name,
//            sortBy = "createTime"
//        )
//
//        responseEntity.shouldNotBeNull()
//        responseEntity.statusCode.shouldBeEqual(HttpStatus.OK)
//    }
//
//    @Test
//    fun `collects all material`() {
//
//        val responseEntity = omsClient.getMaterials(
//            xAbcSdkApikey = xAbcSdkApikey,
//            current = 1,
//            size = 10,
//            direction = Sort.Direction.DESC.name,
//            sortBy = "createTime",
//            startDate = LocalDate.now().minusDays(10),
//            endDate = LocalDate.now()
//        )
//
//        responseEntity.shouldNotBeNull()
//        responseEntity.statusCode.shouldBeEqual(HttpStatus.OK)
//    }
//
//    @Test
//    fun `collects all resource-histories`() {
//        val searchParameter = GetResourceHistoryResourceHistorySearchParameter(
//            resourceNames = listOf(ResourceName.CUSTOMER),
//            operation = ResourceHistoryOperation.INSERT
//        )
//
//        val responseEntity = omsClient.getResourceHistory(
//            xAbcSdkApikey = xAbcSdkApikey,
//            current = 1,
//            size = 10,
//            direction = Sort.Direction.DESC.name,
//            sortBy = "createTime",
//            resourceHistorySearch = searchParameter
//        )
//
//        responseEntity.shouldNotBeNull()
//        responseEntity.statusCode.shouldBeEqual(HttpStatus.OK)
//    }
//}