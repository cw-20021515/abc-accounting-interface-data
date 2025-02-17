package com.abc.us.accounting.documents.domain.repository

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.type.DocumentStatus
import com.abc.us.accounting.documents.fixtures.DocumentFixture
import com.abc.us.accounting.documents.fixtures.DocumentItemAttributeFixture
import com.abc.us.accounting.documents.fixtures.DocumentItemFixture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class BasicDocumentRepositoryTests(
    private val documentRepository: DocumentRepository,
    private val documentItemRepository: DocumentItemRepository,
    private val documentItemAttributeRepository: DocumentItemAttributeRepository,
) : AnnotationSpec(){

    @Test
    fun `basic docHash test`() {
        val list:MutableList<String> = mutableListOf()
        val document1 = DocumentFixture.createDocument()
        val docHash1 = document1.docHash
        list.add(docHash1)
        logger.info("docHash: $docHash1, document:$document1")
        DocumentFixture.createDocuments(10).forEach {
            logger.info("docHash: ${it.docHash}, document:$it")
            it.docHash shouldNotBe docHash1

            list.add(it.docHash)
        }
        list.distinct().size shouldBe 11
    }

    @Test
    fun `basic test`() {
        var results = documentRepository.findAll()
        logger.info("results: $results")

        results.size shouldBe 0

        val document = DocumentFixture.createDocument()
        logger.info("before saved document:$document")
        document.version shouldBe Constants.DEFAULT_VERSION


        val saved = documentRepository.save(document)
        logger.info("after saved document:$document")
        saved.version shouldBe Constants.DEFAULT_VERSION

        results = documentRepository.findAll()
        logger.info("results: $results")
        results.size shouldBe 1

        // docItems 수정하고 업데이트가 되는지 확인
        val docItems = DocumentItemFixture.createDocumentItems(3, document = document)
        documentItemRepository.saveAll(docItems)

        for (docItem in docItems) {
            val attributes = DocumentItemAttributeFixture.createDocumentItemAttributes(3, docItemId = docItem.id)
            documentItemAttributeRepository.saveAll(attributes)
        }

        val updated = documentRepository.save(saved)

        updated.version shouldBe Constants.DEFAULT_VERSION + 1
        updated.updateTime shouldBeGreaterThanOrEqualTo document.updateTime

        val itemresults  = documentItemRepository.findAll()
        logger.info("itemresults: $itemresults")
        itemresults.size shouldBe 3

        documentRepository.delete(updated)
    }

    @Test
    fun `basic update test`() {
        val document = DocumentFixture.createDocument(docStatus = DocumentStatus.INITIAL)

        var docItems = DocumentItemFixture.createDocumentItems(2, document = document)
        logger.info("before saved document:$document")
        document.version shouldBe Constants.DEFAULT_VERSION
        val saved = documentRepository.save(document)
        logger.info("after saved document:$document")
        saved.version shouldBe Constants.DEFAULT_VERSION

        val modified = saved.copy("test").copy(DocumentStatus.NORMAL)
        var updated = documentRepository.save(modified)
        logger.info("after updated 1 document:$document")
        updated.version shouldBe Constants.DEFAULT_VERSION + 1
        updated.docType shouldBe document.docType

        updated.updateTime shouldBeGreaterThan saved.updateTime
        updated.text shouldBe "test"
        updated.docStatus shouldBe DocumentStatus.NORMAL
        updated.docType shouldBe document.docType

        updated = documentRepository.saveAndFlush(updated)
        logger.info("after updated 2, document:$updated")
        updated.version shouldBe Constants.DEFAULT_VERSION + 2
        updated.docType shouldBe document.docType

        val found = documentRepository.findById(updated.id).get()
        logger.info("found, document:$found")
        found.docType shouldBe document.docType

        val copied = found.copy("test2")
        updated = documentRepository.save(copied)
        logger.info("after updated 3, document:$updated")
        updated.version shouldBe Constants.DEFAULT_VERSION + 3


        documentRepository.delete(updated)
    }


    @Test
    fun `basic update test with bulk operation`() {
        val document = DocumentFixture.createDocument(docStatus = DocumentStatus.INITIAL)

        var docItems = DocumentItemFixture.createDocumentItems(2, document = document)
        logger.info("before saved document:$document")
        document.version shouldBe Constants.DEFAULT_VERSION
        val saved = documentRepository.bulkInsert(listOf(document))
        logger.info("after saved document:$document")
        saved.first().version shouldBe Constants.DEFAULT_VERSION

        val modified = saved.first().copy("test").copy(DocumentStatus.NORMAL)
        var updated = documentRepository.bulkUpdate(listOf( modified))
        logger.info("after updated 1 document:$document")
        updated.first().version shouldBe Constants.DEFAULT_VERSION + 1
        updated.first().docType shouldBe document.docType

        updated.first().updateTime shouldBeGreaterThan saved.first().updateTime
        updated.first().text shouldBe "test"
        updated.first().docStatus shouldBe DocumentStatus.NORMAL
        updated.first().docType shouldBe document.docType

        val exception = shouldThrow<ObjectOptimisticLockingFailureException> {
            documentRepository.bulkUpdate(listOf(modified))
        }
        exception.message shouldContain  "Row was updated or deleted by another transaction"

        updated = documentRepository.bulkUpdate(updated)
        logger.info("after updated 2, document:$updated")
        updated.first().version shouldBe Constants.DEFAULT_VERSION + 2
        updated.first().docType shouldBe document.docType

        val found = documentRepository.findById(updated.first().id).get()
        logger.info("found, document:$found")
        found.docType shouldBe document.docType

        val copied = found.copy("test2")
        updated = documentRepository.bulkUpdate(listOf(copied))
        logger.info("after updated 2, document:$updated")
        updated.first().version shouldBe Constants.DEFAULT_VERSION + 3


        documentRepository.delete(updated.first())
    }

    @Test
    fun `basic delete test`() {
        val document = DocumentFixture.createDocument()
        logger.info("before saved document:$document")
        document.version shouldBe Constants.DEFAULT_VERSION
        var saved = documentRepository.save(document)
        logger.info("after saved document:$saved")
        saved.version shouldBe Constants.DEFAULT_VERSION

        val copied = saved.copy("test")
        logger.info("copied document:$copied")
        saved = documentRepository.save(copied)
                logger.info("after saved2 document:$saved")
        saved.version shouldBe Constants.DEFAULT_VERSION + 1
        saved = documentRepository.save(saved)
        logger.info("after saved3 document:$saved")
        saved.version shouldBe Constants.DEFAULT_VERSION + 2

        documentRepository.delete(saved)
        documentRepository.findAll().size shouldBe 0
    }


    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}
