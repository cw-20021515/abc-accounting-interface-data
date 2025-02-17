package com.abc.us.accounting.documents.domain.type

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.shouldBe

class AccountClassTests: AnnotationSpec(){

    @Test
    fun `account code range test`() {
        com.abc.us.accounting.documents.domain.type.AccountClass.values().forAll {
            val codeRange = it.getAccountCodeRange()
            codeRange.first shouldBe it.codePrefix * 1000000
            codeRange.last shouldBe (it.codePrefix + 1) * 1000000 - 1
        }

        com.abc.us.accounting.documents.domain.type.AccountClass.ASSET.getAccountCodeRange() shouldBe IntRange(1000000, 1999999)
        com.abc.us.accounting.documents.domain.type.AccountClass.LIABILITY.getAccountCodeRange() shouldBe IntRange(2000000, 2999999)
        com.abc.us.accounting.documents.domain.type.AccountClass.EQUITY.getAccountCodeRange() shouldBe IntRange(3000000, 3999999)
        com.abc.us.accounting.documents.domain.type.AccountClass.REVENUE.getAccountCodeRange() shouldBe IntRange(4000000, 4999999)
        com.abc.us.accounting.documents.domain.type.AccountClass.EXPENSE.getAccountCodeRange() shouldBe IntRange(5000000,  5999999)
    }
}