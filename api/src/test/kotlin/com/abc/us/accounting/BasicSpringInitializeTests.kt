package com.abc.us.accounting

import io.kotest.core.spec.style.AnnotationSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class BasicSpringInitializeTests : AnnotationSpec() {

    @Test
    fun `basic spring initialize test`() {

    }
}
