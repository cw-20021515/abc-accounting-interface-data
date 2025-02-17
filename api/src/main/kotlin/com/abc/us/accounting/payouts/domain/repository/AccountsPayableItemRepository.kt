package com.abc.us.accounting.payouts.domain.repository

import com.abc.us.accounting.payouts.domain.entity.AccountsPayableItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AccountsPayableItemRepository : JpaRepository<AccountsPayableItem, String> {
    fun findByTxId(txId:String): Optional<List<AccountsPayableItem>>
    @Query("SELECT COUNT(p) FROM AccountsPayableItem p WHERE p.id IN :ids")
    fun countByIdIn(@org.springframework.data.repository.query.Param("ids") ids: List<String?>): Long
}
