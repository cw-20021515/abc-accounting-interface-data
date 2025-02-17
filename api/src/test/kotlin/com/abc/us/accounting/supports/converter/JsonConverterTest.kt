package com.abc.us.accounting.supports.converter

import com.abc.us.generated.models.Customer
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import org.slf4j.LoggerFactory

class JsonConverterTest: AnnotationSpec(){
//    @Test
//    fun `map convert test`() {
//
//        val testValue = mutableMapOf(
//            "userId" to "lmh123@any.pink",
//            "channelCustomerId" to "gid://shopify/Customer/7760972546264",
//            "email" to "lmh123@any.pink",
//            "phone" to "12345678111",
//            "accountType" to "INDIVIDUAL",
//            "customerStatus" to "ACTIVE",
//            "firstName" to "Qw",
//            "lastName" to "Aa",
//            "createUser" to "test",
//            "updateUser" to "test",
//            "createTime" to "2024-11-18T02:40:21.193844086",
//            "updateTime" to "2024-11-18T02:40:21.19500436",
//            "id" to "dcbf02fef1ff6a3b795bc2402afe0f54"
//        )
//
//        var converter = JsonConverter()
//        val customer = converter.toObj(testValue, Customer::class.java)
//        customer.shouldNotBeNull()
//    }


    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}
