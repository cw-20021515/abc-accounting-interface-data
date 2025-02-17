package com.abc.us.accounting.documents.domain.type

import com.abc.us.accounting.documents.exceptions.DocumentException
import com.abc.us.accounting.commons.domain.type.ResultCode
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

class DocumentStatusTests : AnnotationSpec(){

    @Test
    fun `basic status test`() {
        val status = DocumentStatus.INITIAL
        status.code shouldBe "IN"
        status.value shouldBe "Initial"
        status.engText shouldBe "Initial Document"
        status.korText shouldBe "초기상태 전표"
    }


    @Test
    fun `basic transit test`() {
        val initial = DocumentStatus.INITIAL

        initial.canTransit(DocumentStatus.DRAFT) shouldBe true
        initial.canTransit(DocumentStatus.NORMAL) shouldBe true
        initial.canTransit(DocumentStatus.REVIEW) shouldBe true
        initial.canTransit(DocumentStatus.REVERSAL) shouldBe true

        // normal case
        val normal = initial.transit(DocumentStatus.NORMAL)
        normal shouldBe DocumentStatus.NORMAL


        // exception case
        val exception = shouldThrow<DocumentException.DocumentStatusTransitionException> {
            initial.transit(DocumentStatus.REVERSED)
        }
        exception.errorCode shouldBe ResultCode.DOCUMENT_STATUS_TRANSITION

        val reversed = normal.transit(DocumentStatus.REVERSED)
        reversed shouldBe DocumentStatus.REVERSED
    }
}