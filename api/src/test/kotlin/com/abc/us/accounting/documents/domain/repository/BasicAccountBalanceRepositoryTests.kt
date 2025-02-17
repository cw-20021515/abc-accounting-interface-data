package com.abc.us.accounting.documents.domain.repository

import com.abc.us.accounting.documents.domain.type.BalanceRecordType
import com.abc.us.accounting.documents.fixtures.AccountModelsFixture
import com.abc.us.accounting.documents.service.AccountServiceable
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class BasicAccountBalanceRepositoryTests(
    private val accountBalanceRecordRepository: AccountBalanceRecordRepository,
    private val fiscalClosingBalanceSnapshotRepository: FiscalClosingBalanceSnapshotRepository,
    private val accountServiceable: AccountServiceable
) : AnnotationSpec() {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    @Test
    fun `opening balance test`() {
        val count = 5

        val records = AccountModelsFixture.randomOpeningBalanceRecord(count, BalanceRecordType.OPENING_BALANCE, LocalDate.now())
        records.size shouldBe count

        val saved = accountBalanceRecordRepository.saveAll(records)
        for (record in records) {
            logger.info("record: $record")
        }

        val founded = accountBalanceRecordRepository.findAllById(saved.map { it.id })

        saved.size shouldBe records.size
        founded.size shouldBe saved.size
        founded shouldBe saved

        accountBalanceRecordRepository.deleteAllById(saved.map { it.id })
    }

}
