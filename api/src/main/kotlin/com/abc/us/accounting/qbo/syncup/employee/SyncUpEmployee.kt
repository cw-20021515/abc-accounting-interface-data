package com.abc.us.accounting.qbo.syncup.employee

import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.documents.domain.entity.Company
import com.abc.us.accounting.documents.domain.repository.CompanyRepository
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.payouts.domain.entity.Employee
import com.abc.us.accounting.payouts.domain.repository.EmployeeRepository
import com.abc.us.accounting.qbo.domain.entity.QboItemTemplate
import com.abc.us.accounting.qbo.domain.entity.QboVendor
import com.abc.us.accounting.qbo.domain.entity.key.QboVendorKey
import com.abc.us.accounting.qbo.domain.repository.QboVendorRepository
import com.abc.us.accounting.qbo.domain.type.VendorType
import com.abc.us.accounting.qbo.interact.QBOService
import com.abc.us.accounting.supports.converter.JsonConverter
import com.intuit.ipp.data.Vendor
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import com.intuit.ipp.data.EmailAddress
import com.intuit.ipp.data.TelephoneNumber
import com.intuit.ipp.data.PhysicalAddress
import com.intuit.ipp.data.WebSiteAddress
import kotlin.reflect.jvm.internal.impl.descriptors.Visibilities.Private

@Service
class SyncUpEmployee (
    private val qboService: QBOService,
    private val qboVendorRepository : QboVendorRepository,
    private val employeeRepository: EmployeeRepository,
    private val companyRepository: CompanyRepository
)  {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val converter = JsonConverter()
    }

    fun buildDisplayName(company : Company,employee: Employee) : String{
        val builder = StringBuilder()
        builder.append("ABC.E.")
        builder.append(company.code.code)
        builder.append(".")
        builder.append(employee.id)
        return builder.toString()
    }

    fun buildQboVendor(company : Company,employee: Employee) : Vendor {
        return Vendor().apply {
            title = employee.gradeName
            suffix = employee.roleName  // 16까지 지원되서 이거 추후 수정필요함
            displayName = buildDisplayName(company,employee)
            companyName = employee.companyCode

            primaryEmailAddr = EmailAddress().apply {
                address = employee.email
            }
//            billAddr = PhysicalAddress().apply {
//                city = employee.city
//                country = employee.country
//                countryCode = employee.countryCode
//                county = employee.county
//                //countrySubDivisionCode
//                postalCode = employee.zipCode
//                line1 = employee.address
//            }

            givenName = employee.firstName
            familyName = employee.lastName
            //taxIdentifier
            primaryPhone = TelephoneNumber().apply {
                freeFormNumber = employee.phone
            }
            fax = TelephoneNumber().apply {
                freeFormNumber = employee.fax
            }
        }
    }

    fun buildSubmittedVendor( companyCode : String,employee: Employee,vendor : Vendor) : QboVendor{
        val key = QboVendorKey(qboId=vendor.id,vendorId=employee.id,companyCode=companyCode)
        val submittedJson = converter.toJson(vendor)
        return QboVendor(key =key,type=VendorType.EMPLOYEE).apply {
            submitResult = submittedJson
        }
    }

    fun addQboVendor(company : Company,employee: Employee)  {

        try {
            val vendor = buildQboVendor(company,employee)
            val result = qboService.add(company.code.code,vendor)
            val submitted = buildSubmittedVendor(company.code.code,employee,result!!)
            qboVendorRepository.save(submitted)
        }
        catch (e : Exception) {
            logger.error { "Failure Add Employee [${employee.id}]-[${e.message}]" }
        }
    }

    fun syncup(trailer: AsyncEventTrailer) {
        val from = trailer.queries().get("startDateTime") as LocalDateTime
        val to = trailer.queries().get("endDateTime") as LocalDateTime

        logger.info { "QBO-START[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }
        val employees = employeeRepository.findAllByIsActiveTrue()

        val empMap: Map<String, MutableList<Employee>> = employees
            .filter { it.id != null } // userId가 null이 아닌 경우만 포함
            .groupBy { it.companyCode } // userId를 Key로 그룹핑
            .mapValues { entry -> entry.value.toMutableList() }

        empMap.forEach { companyCode,employees ->
            val company = companyRepository.findByCode(CompanyCode.of(companyCode))
            employees.forEach{ employee ->
                addQboVendor(company!!,employee)
            }
        }

        logger.info { "QBO-END[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }
    }
}