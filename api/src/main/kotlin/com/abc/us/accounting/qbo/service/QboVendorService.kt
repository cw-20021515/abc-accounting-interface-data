//package com.abc.us.accounting.qbo.service
//
//import com.abc.us.accounting.payouts.domain.entity.Employee
//import com.abc.us.accounting.payouts.domain.entity.Vendor
//import jakarta.transaction.Transactional
//
//class QboVendorService(
//    private val vendorRepository: VendorRepository,
//    private val employeeRepository: EmployeeRepository,
//    private val qboVendorRepository: QboVendorRepository,
//
//) {
//    private fun createVendorData(entity: Any, displayName: String): Map<String, Any> {
//        val commonData = mapOf(
//            "DisplayName" to displayName,
//            "PrimaryEmailAddr" to (entity as? Vendor)?.email ?: (entity as? Employee)?.email,
//            "PrimaryPhone" to (entity as? Vendor)?.phone ?: (entity as? Employee)?.phone,
//            "CompanyName" to entity.companyCode,
//            "BillAddr" to entity.address
//        )
//        return commonData
//    }
//
//    @Transactional
//    fun syncVendorsToQuickBooks(companyCode: String) {
//        val existingQboVendors = qboVendorRepository.findAllByCompanyCode(companyCode).map { it.key.vendorId }.toSet()
//
//        val vendors = vendorRepository.findByCompanyCode(companyCode)
//        val employees = employeeRepository.findByCompanyCode(companyCode)
//
//        val allEntities = (vendors + employees).filter { it.id !in existingQboVendors }
//
//        val qboVendorsToSave = mutableListOf<QboVendor>()
//
//        allEntities.forEach { entity ->
//            val displayName = entity.id
//            val vendorData = createVendorData(entity, displayName)
//
//            val qboId = quickBooksService.registerVendor(vendorData)
//
//            if (qboId != null) {
//                val qboVendorKey = QboVendorKey(qboId = qboId, vendorId = entity.id, companyCode = companyCode)
//                val qboVendor = QboVendor(
//                    key = qboVendorKey,
//                    code = entity.id,
//                    submitResult = vendorData.toString()
//                )
//                qboVendorsToSave.add(qboVendor)
//            }
//        }
//
//        if (qboVendorsToSave.isNotEmpty()) {
//            qboVendorRepository.saveAll(qboVendorsToSave)
//        }
//    }
//}