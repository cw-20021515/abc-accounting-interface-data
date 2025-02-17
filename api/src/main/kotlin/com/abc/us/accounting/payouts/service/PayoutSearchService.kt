 package com.abc.us.accounting.payouts.service

import com.abc.us.accounting.payouts.domain.repository.AccountingAccountRepository
import com.abc.us.accounting.payouts.domain.repository.AccountingCompanyRepository
import com.abc.us.accounting.payouts.domain.repository.AccountingCostCenterRepository
import com.abc.us.accounting.payouts.domain.repository.AccountingMaterialRepository
import com.abc.us.accounting.payouts.model.response.ResAccountCaseData
import com.abc.us.accounting.payouts.model.response.ResAccountCostCenterDto
import com.abc.us.accounting.payouts.model.response.ResAccountMaterialDto
import com.abc.us.accounting.payouts.model.response.ResCompanyInfoData
import com.abc.us.accounting.supports.mapper.MapperUtil
import com.abc.us.generated.models.PayoutAccountCaseType
import org.springframework.stereotype.Service


 @Service
class PayoutSearchService(
     private val accountRepository : AccountingAccountRepository,
     private val accountMaterialRepository : AccountingMaterialRepository,
     private val accountingCostCenterRepository : AccountingCostCenterRepository,
     private val accountingCompanyRepository : AccountingCompanyRepository,
) {

    // 코스트센터 검색 팝업 조회
    fun selectAccountByCostCenterList(costCenter:String?): List<ResAccountCostCenterDto>? {
        return accountingCostCenterRepository.selectBillingCostCenterList(costCenter)
    }

    // 자제 검색 팝업 조회
    fun selectBySearchMatterial(material:String?): List<ResAccountMaterialDto>? {
        return accountMaterialRepository.selectBySearchMatterial(material)
    }

    // 회사 검색 팝업 조회
    fun selectBySearchCompany(): List<ResCompanyInfoData>? {
        // CompanyRepository 참고
        return accountingCompanyRepository.selectBySearchCompany()
    }

    // 계정과목 검색 팝업 조회
    fun selectBySearchAccount(payoutAccountCaseType:PayoutAccountCaseType): List<ResAccountCaseData>? {
        var resData:List<ResAccountCaseData>? = null
        when (payoutAccountCaseType) {
            PayoutAccountCaseType.GENERAL -> {    // VENDOR > GENERAL 관련 작업 수행
                resData = accountRepository.selectOrderInventoryAssetsAccount()
                //resData = MockUtil.getDataFromJson("/codes/payouts/taskAccountCaseOther.json")
            }
            PayoutAccountCaseType.MATERIAL -> { // VENDOR > MATERIAL 관련 작업 수행
                resData = accountRepository.selectAccountsPayableAccount()
                //resData = MockUtil.getDataFromJson("/codes/payouts/taskAccountCaseMaterial.json")
            }
            PayoutAccountCaseType.EMPLOYEE -> { // EMPLOYEE 관련 작업 수행
                resData = accountRepository.selectExpenseAccount()
                //resData = MockUtil.getDataFromJson("/codes/payouts/taskAccountCaseEmployee.json")
            }
            else -> {}
        }
        return resData
    }
}
