package com.abc.us.accounting.documents.service

import com.abc.us.accounting.commons.service.SequenceService
import com.abc.us.accounting.documents.domain.type.DocumentType
import com.abc.us.accounting.supports.utils.IdGenerator
import org.springframework.stereotype.Component
import java.time.LocalDate


@Component
class DocumentIdGenerator(private val sequenceService: SequenceService) {
    companion object {
        private const val BASE_YEAR = 2000
    }

    /**
     * Generate a document ID based on the document type and the date.
     * The document ID is generated as follows:
     * - The first two characters are the document type code.
     * - The next two characters are the year code, which is the year since 1970.
     * - The last three characters are the day of the year.
     * - The last five characters are the sequence number for the day.
     */
    fun generateDocumentId(docType: DocumentType, date: LocalDate = LocalDate.now()): String {
        val dateKey:String = IdGenerator.getDateKey(date)
        val seq:Long = sequenceService.getNextValueWithRetry(dateKey)

        return IdGenerator.generateId(docType.code, date, seq)
    }
}