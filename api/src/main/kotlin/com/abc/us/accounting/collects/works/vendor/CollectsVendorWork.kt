package com.abc.us.accounting.collects.works.vendor

import com.abc.us.accounting.collects.domain.repository.CollectVendorRepository
import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.entity.BulkDistinctInserter
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class CollectsVendorWork(
//    private val vendorMasterRepository : VendorMasterRepository,
    private val originVendorRepository : CollectVendorRepository,
    private val bulkInserter : BulkDistinctInserter,
    private val eventPublisher : ApplicationEventPublisher,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
//    fun generateVendor(etlvendor : VendorMaster) : CollectVendor{
//        return CollectVendor().apply {
//            vendorId = etlvendor.code
//            employerCode = etlvendor.employerCode
//            departmentCode = etlvendor.departmentCode
//            departmentName = etlvendor.departmentName
//            name = EmbeddableName().apply {
//                displayName = etlvendor.displayName
//                companyName = etlvendor.companyName
//                titleName = etlvendor.title
//                webAddr = etlvendor.webAddr
//                primaryPhone = etlvendor.office
//                alternatePhone = etlvendor.alternatePhone
//                mobile = etlvendor.mobile
//                fax = etlvendor.fax
//                primaryEmail = etlvendor.email
//                webAddr = etlvendor.webAddr
//            }
//            location = EmbeddableLocation().apply {
//                city = etlvendor.city
//                country = etlvendor.country
//                countryCode = etlvendor.countryCode
//                zipCode = etlvendor.zipCode
//                state = etlvendor.state
//                county = etlvendor.county
//                address1 = etlvendor.address
//            }
//            companyId = etlvendor.companyId
//            terms = etlvendor.terms
//            acctNum = etlvendor.acctNum
//            taxIdentifier = etlvendor.taxIdentifier
//            currency = etlvendor.currency
//            businessNumber =etlvendor.businessNumber
//            createTime= etlvendor.createTime
//            updateTime=etlvendor.updateTime
//            remark=etlvendor.remark
//            description= etlvendor.description
//            isActive = etlvendor.isActive
//        }
//    }
    fun collect(trailer: AsyncEventTrailer){
//        val vendorIds = mutableSetOf<String>()
//        vendorMasterRepository.findAll().forEach { vendorMaster ->
//            val originVendor = generateVendor(vendorMaster)
//            vendorSavor.execute(mutableListOf(originVendor) )
//            vendorIds.add(vendorMaster.id!!)
//        }
//        val trailer = AsyncEventTrailer.Builder()
//            .listener("establish/vendors")
//            .addFreight("vendorIds", vendorIds)
//            .build(this)
//        eventPublisher.publishEvent(trailer)
    }

    fun trigger(trailer: AsyncEventTrailer) {

//        val vendorIds = mutableSetOf<String>()
//
//        trailer.freights().forEach{ key,value ->
//            val entityLog = value as AuditEntityLog
//            vendorIds.add(entityLog.eventTableId!!)
//        }
//
//        vendorMasterRepository.findAllById(vendorIds.toMutableList()).forEach { vendorMaster ->
//            val originVendor = generateVendor(vendorMaster)
//            vendorSavor.execute(mutableListOf(originVendor) )
//        }
//
//        val trailer = AsyncEventTrailer.Builder()
//            .listener("establish/vendors")
//            .addFreight("vendorIds", vendorIds)
//            .addFreight("logs",trailer.freights())
//            .build(this)
//        eventPublisher.publishEvent(trailer)
    }
}