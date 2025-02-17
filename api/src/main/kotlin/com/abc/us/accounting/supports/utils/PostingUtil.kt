package com.abc.us.accounting.supports.utils

import com.abc.us.accounting.documents.model.*
import com.abc.us.accounting.documents.service.DocumentService
import mu.KotlinLogging
import java.math.BigDecimal

class PostingUtil {

    companion object {
        private val logger = KotlinLogging.logger {}

        fun posting(
            context: DocumentServiceContext,
            requests: List<CreateDocumentRequest>,
            documentService: DocumentService
        ): List<DocumentResult> {
            // 중복 hash 검증
            val dupHashRequests = requests.groupBy {
                it.docHash
            }.filter {
                it.value.size > 1
            }
            if (dupHashRequests.isNotEmpty()) {
                logger.error("Duplicate doc_hash, dupHashRequests:${dupHashRequests}")
                throw IllegalArgumentException("Duplicate doc_hash, dupHashRequests:${dupHashRequests}")
            }
            // 금액이 0인 documentItem 제외
            var newRequests = requests.map {
                it.copy(
                    docItems = it.docItems.filter { docItem ->
                        docItem.txAmount.stripTrailingZeros() != BigDecimal.ZERO
                    }.toMutableList()
                )
            }
            val maxSize = 100
            val res: MutableList<DocumentResult> = mutableListOf()
            while (newRequests.isNotEmpty()) {
                val size = newRequests.size.coerceAtMost(maxSize)
                val subRequests = newRequests.subList(0, size)
                newRequests = newRequests.subList(size, newRequests.size)
                val result = documentService.posting(
                    context,
                    subRequests
                )
                res.addAll(result)
            }
            return res
        }
    }
}