package com.abc.us.accounting.payouts.domain.repository

import com.abc.us.accounting.payouts.domain.entity.PayoutAttachment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AttachmentsRepository : JpaRepository<PayoutAttachment, String> {
    fun findByIdAndIsDeleted(id: String, isDeleted: Boolean): Optional<PayoutAttachment?>?
    fun findByTxIdAndIsDeletedFalse(txId: String): MutableList<PayoutAttachment?>?

}