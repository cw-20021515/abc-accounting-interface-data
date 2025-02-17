package com.abc.us.accounting.qbo.service

import com.abc.us.accounting.payouts.domain.entity.CostCenter
import com.abc.us.accounting.payouts.domain.repository.CostCenterRepository
import com.abc.us.accounting.qbo.domain.entity.QboClass
import com.abc.us.accounting.qbo.domain.entity.key.QboClassKey
import com.abc.us.accounting.qbo.domain.repository.QboClassRepository
import com.abc.us.accounting.qbo.domain.type.ClassType
import com.abc.us.accounting.qbo.interact.QBOService
import com.abc.us.accounting.supports.converter.JsonConverter
import com.intuit.ipp.data.Class
import com.intuit.ipp.data.ReferenceType
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.*

@Service
class QboCostCenterService(
    private val qboService : QBOService,
    private val qboClassRepository : QboClassRepository,
    private val costCenterRepository: CostCenterRepository
) {

    companion object {
        private val logger = KotlinLogging.logger {}
        private val converter = JsonConverter()
    }

    @Transactional
    fun saveCostCenter(costCenter: CostCenter): CostCenter {
        return costCenterRepository.save(costCenter)
    }

    fun getCostCenterById(id: String): Optional<CostCenter> {
        return costCenterRepository.findById(id)
    }

    fun getRootCostCenters(): List<CostCenter> {
        return costCenterRepository.findByParentIdIsNull()
    }
    fun getChildCostCenters(parentId: String): List<CostCenter> {
        return costCenterRepository.findByParentId(parentId)
    }
    fun buildSubmittedClass(costCenter: CostCenter, cls : Class ) : QboClass{
        val submitJson = converter.toJson(cls)
        return QboClass( key = QboClassKey(qboId = cls.id, classId =costCenter.id ),
            code = costCenter.code,
            companyCode = costCenter.companyCode,
            type = ClassType.COST_CENTER,
            submitResult = submitJson!! )
    }
    @Transactional
    fun createQuickBooksClass(costCenter: CostCenter, parentClass: Class?): Class? {

        val qbClass = Class().apply {
            name = costCenter.id
            fullyQualifiedName = if (parentClass != null) {
                "${parentClass.fullyQualifiedName}.${costCenter.name}"
            } else {
                costCenter.name
            }
            isSubClass = parentClass != null
            parentRef = parentClass?.let { ReferenceType().apply { value = it.id } }
        }


        try {
            val result = qboService.add(costCenter.companyCode,qbClass) as Class
            result?.let {
                val submitted = buildSubmittedClass(costCenter, result)
                qboClassRepository.save(submitted)
                logger.info("Add QboClass-[${result.name}]-[${result.id}]")
            }
            return result
        }catch (e: Exception) {
            logger.error { "Failure add QboClass-[${e.message}]"}
            return null
        }
    }
    @Transactional
    fun syncup() {
        val rootCenters = getRootCostCenters()
        rootCenters.forEach {
            syncCostCenterHierarchy(it, null)
        }
    }
    private fun syncCostCenterHierarchy(costCenter: CostCenter, parentClass: Class?) {
        // QuickBooks에 현재 비용 센터 추가
        val qbClass = createQuickBooksClass(costCenter, parentClass)

        // 하위 비용 센터 검색 후 재귀적으로 등록
        val childCenters = getChildCostCenters(costCenter.id)
        childCenters.forEach { syncCostCenterHierarchy(it, qbClass) }
    }
}