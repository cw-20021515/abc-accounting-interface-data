package com.abc.us.accounting.qbo.syncup.cctr

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.payouts.domain.repository.CostCenterRepository
import com.abc.us.accounting.qbo.service.QboCostCenterService
//import com.abc.us.accounting.qbo.service.QboCostCenterService
import com.abc.us.accounting.supports.converter.JsonConverter
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime

import com.intuit.ipp.data.Class

@Service
class SyncUpCostCenter(
    private val costCenterService: QboCostCenterService,
    private val costCenterRepository: CostCenterRepository
) {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val converter = JsonConverter()
    }

    fun syncup(trailer: AsyncEventTrailer) {
        val from = trailer.queries().get("startDateTime") as LocalDateTime
        val to = trailer.queries().get("endDateTime") as LocalDateTime

        logger.info { "QBO-START[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }
        val costCenters = costCenterRepository.findAll()
        costCenterService.syncup()
        logger.info { "QBO-END[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }
    }
}