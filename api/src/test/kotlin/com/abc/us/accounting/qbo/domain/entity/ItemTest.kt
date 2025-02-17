package com.abc.us.accounting.qbo.domain.entity

//import com.abc.us.accounting.qbo.domain.entity.associate.AssociatedItem
//import com.abc.us.accounting.qbo.domain.repository.AssociateItemRepository
//import com.abc.us.accounting.supports.entity.toEntityHash
//import io.kotest.core.spec.style.AnnotationSpec
//import io.kotest.matchers.nulls.shouldNotBeNull
//import org.springframework.boot.test.context.SpringBootTest
//import java.time.OffsetDateTime
//
//@SpringBootTest(properties = ["spring.profiles.active=test"])
//@ActiveProfiles("test")
//class ItemTest(private val repository : AssociateItemRepository) : AnnotationSpec() {
//    @Test
//    fun `check auto create id`() {
//
//        val abcItem = AssociatedItem().apply {
//            this.createTime = OffsetDateTime.now()
//            this.isActive = true
//            this.companyCode = companyCode
//        }
//
//
//        val result = repository.save(abcItem)
//        result.shouldNotBeNull()
//    }
//}
