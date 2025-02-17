package com.abc.us.accounting.qbo.syncup.branch

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.commons.domain.type.TimeZoneCode
import com.abc.us.accounting.payouts.domain.entity.Branch
import com.abc.us.accounting.payouts.domain.repository.BranchRepository
import com.abc.us.accounting.qbo.domain.entity.QboDepartment
import com.abc.us.accounting.qbo.domain.entity.key.QboDepartmentKey
import com.abc.us.accounting.qbo.domain.repository.QboDepartmentRepository
import com.abc.us.accounting.qbo.interact.QBOCertifier
import com.abc.us.accounting.qbo.interact.QBOService
import com.abc.us.accounting.qbo.service.QboAccountService
import com.abc.us.accounting.qbo.service.QboAccountService.Companion
import com.abc.us.accounting.supports.converter.JsonConverter
import com.intuit.ipp.data.Account
import com.intuit.ipp.data.Department
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime


@Service
class SyncUpBranch (
    private val qboService : QBOService,
    private val qboCertifier : QBOCertifier,
    private val branchRepository : BranchRepository,
    private val qboDepartmentRepository : QboDepartmentRepository
) {

    companion object {
        private val logger = KotlinLogging.logger {}
        private val converter = JsonConverter()
    }
    fun syncup(trailer: AsyncEventTrailer){
        val from = trailer.queries().get("startDateTime") as LocalDateTime
        val to = trailer.queries().get("endDateTime") as LocalDateTime
        val timezone = trailer.queries().get("timezone") as TimeZoneCode
        val reversing = trailer.reversing()
        logger.info { "SUBMIT-START[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }

        val fromDateTime = timezone.convertTime(from, TimeZoneCode.UTC)
        val toDateTime = timezone.convertTime(to, TimeZoneCode.UTC)

        val branches = branchRepository.findAll()
        val branchMap: Map<String, MutableList<Branch>> = branches
            .filter { it.id != null } // userId가 null이 아닌 경우만 포함
            .groupBy { it.companyCode } // userId를 Key로 그룹핑
            .mapValues { entry -> entry.value.toMutableList() }

        branchMap.forEach { companyCode , branchList ->
            try {
                qboService.selectAll(companyCode, Department::class) { department ->
                    val branchKey = QboDepartmentKey(qboId = department.id, companyCode = companyCode)
                    val submitBranch = qboDepartmentRepository.findByKey(branchKey)
                    if (submitBranch == null) {
                        val resultJson = converter.toJson(department)
                        val masterBranch = branchRepository.findByCompanyCodeAndName(companyCode,department.name)
                        if (masterBranch == null) {
                            logger.error { "NotFound MasterBranch(${companyCode}-${department.name})" }
                            return@selectAll
                        }
                        val qboBranch = QboDepartment(
                            key = branchKey,
                            name = department.name,
                            branchId = masterBranch.id,
                            warehouseId = masterBranch.warehouseId,
                            submitResult = resultJson!!
                        )
                        qboDepartmentRepository.save(qboBranch)
                        logger.info { "QBO Add Department: $resultJson" }
                    }
                }
            }
            catch (e: Exception) {
                logger.error("Error during sync up-[${companyCode}]-[${e.message}]")
            }
        }

        logger.info { "SUBMIT-END[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }
    }
}