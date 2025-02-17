//package com.abc.us.accounting.qbo.service
//
//import com.abc.us.accounting.payouts.domain.type.CostCenterCategory
//import com.abc.us.accounting.qbo.domain.entity.key.QboClassKey
//import com.abc.us.accounting.qbo.domain.entity.QboClass
//import com.abc.us.accounting.qbo.domain.repository.QboClassRepository
//import com.abc.us.accounting.qbo.interact.QBOCertifier
//import com.abc.us.accounting.qbo.interact.QBOService
//import com.abc.us.accounting.supports.converter.JsonConverter
//import jakarta.transaction.Transactional
//import mu.KotlinLogging
//import org.springframework.stereotype.Service
//import java.time.OffsetDateTime
//
//
//@Service
//class QboBranchService(
//    private val qboCertifier: QBOCertifier,
//    private val qboService: QBOService,
//    private val qboClassRepository: QboClassRepository
//) {
//    companion object {
//        private val logger = KotlinLogging.logger {}
//        private val converter = JsonConverter()
//    }
//    fun getAllQboLocations(companyCode : String): List<Any>? {
//        val query = "SELECT * FROM Location"
//        val results = qboService.executeQuery(companyCode,query)
//        return results
//    }
//
//    @Transactional
//    fun syncQboLocationsToDb() {
//        qboCertifier.visit { credential ->
//            val locations = getAllQboLocations(credential.companyCode)
//            locations?.let {
//                it.forEach { location ->
//                    val locMap = location as Map<String, Any>
//                    syncQboLocation(credential.companyCode, locMap)
//                }
//            }
//        }
//    }
//
//    @Transactional
//    fun syncQboLocation(companyCode : String,location: Map<String,Any>) {
////        val qboId = location["Id"]?.toString() ?: return
////        val name = location["Name"]?.toString() ?: "Unknown"
////        val active = location["Active"]?.toString()?.toBoolean() ?: true
////        val category = CostCenterCategory.BRANCH // 예제에서는 BRANCH
////        val submittedJson = converter.toJson(location)
////
////        val key = QboClassKey(qboId = qboId, cctrId = qboId) // ID를 cost center ID로 설정
////        val existingQboClass = qboClassRepository.findById(key)
////
////        if (existingQboClass.isPresent) {
////            // 기존 데이터 업데이트
////            val qboClass = existingQboClass.get()
////            qboClass.updateTime = OffsetDateTime.now()
////            qboClass.isActive = active
////            qboClassRepository.save(qboClass)
////        } else {
////            // 새 데이터 삽입
////            val qboClass = QboClass(
////                key = key,
////                companyCode = companyCode,
////                code = qboId, // Location ID를 CCTR Code로 사용
////                category = category,
////                submittedJson = submittedJson!!,
////                createTime = OffsetDateTime.now(),
////                updateTime = OffsetDateTime.now(),
////            )
////            qboClassRepository.save(qboClass)
////        }
//    }
//}