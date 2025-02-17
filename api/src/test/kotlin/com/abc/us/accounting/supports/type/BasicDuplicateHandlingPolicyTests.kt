package com.abc.us.accounting.supports.type

import com.abc.us.accounting.commons.domain.type.DuplicateHandlingPolicy
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

class BasicDuplicateHandlingPolicyTests: AnnotationSpec(){

    @Test
    fun `DuplicateHandlingPolicy Tests`() {
        DuplicateHandlingPolicy.PRESERVE_EXISTING.description shouldBe "기존 데이터를 유지합니다."
        DuplicateHandlingPolicy.ALWAYS_OVERWRITE.description shouldBe "기존 데이터를 새로운 데이터로 대체합니다."
        DuplicateHandlingPolicy.VERSION_BASED.description shouldBe "버전을 비교하여 최신 데이터로 갱신합니다."

        DuplicateHandlingPolicy.PRESERVE_EXISTING.shouldOverwrite() shouldBe false
        DuplicateHandlingPolicy.ALWAYS_OVERWRITE.shouldOverwrite() shouldBe true
        DuplicateHandlingPolicy.VERSION_BASED.shouldOverwrite(1, 2) shouldBe true
        DuplicateHandlingPolicy.VERSION_BASED.shouldOverwrite(2, 1) shouldBe false
        DuplicateHandlingPolicy.VERSION_BASED.shouldOverwrite(1, 1) shouldBe false
    }
}