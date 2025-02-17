package com.abc.us.accounting.documents.service

import com.abc.us.accounting.commons.domain.type.CurrencyCode
import com.abc.us.accounting.documents.domain.entity.Company
import com.abc.us.accounting.documents.domain.entity.FiscalRule
import com.abc.us.accounting.documents.domain.entity.FiscalYearMonth
import com.abc.us.accounting.documents.domain.repository.CompanyRepository
import com.abc.us.accounting.documents.domain.type.CompanyCode
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth

interface CompanyServiceable {
    fun getCompany(companyCode: CompanyCode): Company
    fun getCompanyCurrency(companyCode: CompanyCode): CurrencyCode
    fun getCompanyFiscalRule(companyCode: CompanyCode): FiscalRule
    fun getCompanyFiscalYearMonth(companyCode: CompanyCode, localDate: LocalDate = LocalDate.now()): FiscalYearMonth
    fun getCompanyFiscalYearMonth(companyCode: CompanyCode, yearMonth: YearMonth): FiscalYearMonth
}

@Service
class CompanyService (
    private val companyRepository: CompanyRepository,
) : CompanyServiceable{
    private val lock: Any = Any()

    private val cachedCompanyMap: MutableMap<CompanyCode, Company> = mutableMapOf()

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    init {
        construct()
    }


    private final fun construct() {
        logger.info("reload company data")
        synchronized(lock)  {
            if ( cachedCompanyMap.isEmpty() ) {
                val data = companyRepository.findAll()
                cachedCompanyMap.putAll(data.associateBy { it.companyCode() })
            }
        }
        logger.info("company data load finished, size:${cachedCompanyMap.size}")
    }

    override fun getCompany(companyCode: CompanyCode): Company {
        return cachedCompanyMap[companyCode] ?: throw IllegalArgumentException("company not found, companyCode:$companyCode")
    }

    override fun getCompanyCurrency(companyCode: CompanyCode): CurrencyCode {
        return getCompany(companyCode).currency
    }

    override fun getCompanyFiscalRule(companyCode: CompanyCode): FiscalRule {
        return getCompany(companyCode).fiscalRule
    }

    override fun getCompanyFiscalYearMonth(companyCode: CompanyCode, localDate: LocalDate): FiscalYearMonth {
        return getCompanyFiscalRule(companyCode).from(localDate)
    }

    override fun getCompanyFiscalYearMonth(companyCode: CompanyCode, yearMonth: YearMonth): FiscalYearMonth {
        return getCompanyFiscalRule(companyCode).from(yearMonth)
    }

}