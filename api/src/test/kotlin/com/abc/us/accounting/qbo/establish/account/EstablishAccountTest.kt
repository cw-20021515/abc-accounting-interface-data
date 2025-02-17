//package com.abc.us.accounting.qbo.submit.account
//
//import com.abc.us.accounting.documents.domain.repository.AccountRepository
//import com.abc.us.accounting.qbo.domain.entity.associate.AssociationMap
//import com.abc.us.accounting.qbo.domain.repository.AssociationMapRepository
//import com.abc.us.accounting.qbo.interact.Communicator
//import com.abc.us.accounting.qbo.interact.Credentials
//import io.kotest.core.spec.style.FunSpec
//import io.kotest.matchers.comparables.shouldBeGreaterThan
//import io.kotest.matchers.nulls.shouldNotBeNull
//import io.kotest.matchers.shouldBe
//import org.slf4j.LoggerFactory
//import org.springframework.boot.test.context.SpringBootTest
//
//@SpringBootTest(properties = ["spring.profiles.active=test"])
//@ActiveProfiles("test")
//class EstablishAccountTest(
//    private val credentials: Credentials,
//    private val communicator: Communicator,
//    private val accountRepository : AccountRepository,
//    private val AssociationMapRepository: AssociationMapRepository,
//    private val establishAccount : EstablishAccount
//) :
// FunSpec({
//
//     test("inquiryQboAccounts") {
//
//     }
//
//     test("inquiryQboAccountsGroupByCompanyCode") {
//         val accountsGroupByCompany = establishAccount.inquiryQboAccountsGroupByCompanyCode()
//
//         accountsGroupByCompany.shouldNotBeNull()
//         credentials.visit { company ->
//             accountsGroupByCompany.get(company.code).shouldNotBeNull()
//             true
//         }
//         accountsGroupByCompany.forEach { companyCode, groupByAccounts ->
//             groupByAccounts.shouldNotBeNull()
//             groupByAccounts.forEach { accountName, qboAccount->
//                 qboAccount.shouldNotBeNull()
//                 qboAccount.id.shouldNotBeNull()
//                 qboAccount.name.shouldNotBeNull()
//                 //qboAccount.acctNum.shouldNotBeNull()
//             }
//         }
//     }
//    test("separateUpdateAccounts") {
//        val accountMasters = accountRepository.findAll()
//        val accountsGroupByCompany = establishAccount.inquiryQboAccountsGroupByCompanyCode()
//        val updateAccounts = establishAccount.separateUpdateAccounts(accountMasters,accountsGroupByCompany)
//        updateAccounts.shouldNotBeNull()
//        updateAccounts.forEach{ companyCode , accounts ->
//            accounts.forEach { account ->
//                account.id.shouldNotBeNull()
//                account.name.shouldNotBeNull()
//                account.acctNum.shouldNotBeNull()
//                logger.info("Separate Exists Account-[${companyCode}]-[${account.acctNum}]-[${account.id}.${account.name}]")
//            }
//        }
//    }
//
//     test("updateAccountsToQbo") {
//         val accountMasters = accountRepository.findAll()
//         val saveAssociations = mutableListOf<AssociationMap>()
//         val errMsgs = mutableListOf<String>()
//         val updateAccounts = establishAccount.updateAccountsToQbo(accountMasters,saveAssociations,errMsgs)
//
//         errMsgs.size shouldBe 0
//         updateAccounts.size shouldBeGreaterThan 0
//         saveAssociations.forEach { association ->
//             association.associatedId.shouldNotBeNull()
//             association.associatedValue.shouldNotBeNull()
//             association.associatedName.shouldNotBeNull()
//             association.associatedType.shouldNotBeNull()
//             association.associatedEntity.shouldNotBeNull()
//             association.realmId.shouldNotBeNull()
//             association.companyCode.shouldNotBeNull()
//             association.sandboxName.shouldNotBeNull()
//             logger.info("Updated associated-[${association.associatedId}]-[${association.associatedValue}]-[${association.associatedName}.${association.associatedEntity}]")
//         }
//     }
//
//     test("addAccountsToQbo") {
//         val accountMasters = accountRepository.findAll()
//         val saveAssociations = mutableListOf<AssociationMap>()
//         val errMsgs = mutableListOf<String>()
//         val updatedAccounts = establishAccount.updateAccountsToQbo(accountMasters,saveAssociations,errMsgs)
//         val addedAccounts = establishAccount.addAccountsToQbo(accountMasters,updatedAccounts,saveAssociations,errMsgs)
//
//         errMsgs.size shouldBe 0
//         addedAccounts.size shouldBeGreaterThan 0
//         saveAssociations.forEach { association ->
//             association.associatedId.shouldNotBeNull()
//             association.associatedValue.shouldNotBeNull()
//             association.associatedName.shouldNotBeNull()
//             association.associatedType.shouldNotBeNull()
//             association.associatedEntity.shouldNotBeNull()
//             association.realmId.shouldNotBeNull()
//             association.companyCode.shouldNotBeNull()
//             association.sandboxName.shouldNotBeNull()
//             logger.info("Added associated-[${association.associatedId}]-[${association.associatedValue}]-[${association.associatedName}.${association.associatedEntity}]")
//         }
//     }
//
//     test("existsAssociatedMap") {
//
//     }
//
//     test("loadAccounts") {
//      establishAccount.loadAccounts { account ->
//       account.shouldNotBeNull()
//       account.code.shouldNotBeNull()
//       account.name.shouldNotBeNull()
//       logger.info("Load Account-[${account.code}]-[${account.name}]")
//       true
//      }
//     }
//
//     test("establish") {
//      //establishAccount.establish(AsyncEventTrailer.Builder().build(this))
//     }
// }){
// companion object {
//  private val logger = LoggerFactory.getLogger(this::class.java)
// }
//}
