 package com.abc.us.accounting.payouts.service

import com.abc.us.accounting.payouts.domain.entity.AccountsPayable
import com.abc.us.accounting.payouts.domain.entity.AccountsPayableItem
import com.abc.us.accounting.payouts.domain.repository.AccountPayableRepository
import com.abc.us.accounting.payouts.domain.repository.AccountsPayableItemRepository
import com.abc.us.accounting.payouts.domain.repository.AttachmentsRepository
import com.abc.us.accounting.payouts.domain.repository.IAccountPayableRepository
import com.abc.us.accounting.payouts.model.request.ReqItemPayoutSave
import com.abc.us.accounting.payouts.model.request.ReqItemsVendor
import com.abc.us.accounting.payouts.model.response.ResItemInqyPayout
import com.abc.us.accounting.payouts.model.response.ResPayout
import com.abc.us.accounting.payouts.model.response.ResPayoutItem
import com.abc.us.accounting.supports.mapper.MapperUtil
import com.abc.us.generated.models.AccountingPayoutApprovalStatus
import com.abc.us.generated.models.AccountingPayoutType
import com.abc.us.generated.models.PayoutAccountCaseType
import jakarta.transaction.Transactional
import org.hibernate.query.sqm.tree.SqmNode.log
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse


 @Service
class PayoutDetailService(
     val payoutRepositoryImpl: AccountPayableRepository,
     var payoutRepository: IAccountPayableRepository,
     val payoutItemRepository: AccountsPayableItemRepository,
     var attachmentsRepository: AttachmentsRepository,
) {
     fun selectPayoutDetail(payoutId: String): ResItemInqyPayout? {
         // 지급 상세 조회
         val payoutDetail = payoutRepositoryImpl.findByPayoutDetail(payoutId) ?: return null
         payoutDetail.approvalStatus = payoutDetail.approvalStatus ?: AccountingPayoutApprovalStatus.DRAFTING.name
         println("payoutDetail : $payoutDetail")
         //  지급 ID가 존재하는 경우, 파일 리스트 조회
         payoutDetail.itemsConvertAttachments(payoutDetail.payoutId?.let {
             attachmentsRepository.findByTxIdAndIsDeletedFalse(it)
         })
         // 지급 아이템 조회
//        val payoutItems = payoutItemRepositoryImpl.findByPayoutItems(payoutId)
         val payoutItems = payoutItemRepository.findByTxId(payoutId)
         if (payoutItems?.get() != null) {
             // 지급 ITEM 개인/업체 구분 변환
//            payoutDetail.employeeItems?.add(ReqItemsEmployee().toItems(payoutId, payoutItems?.get()))

             payoutDetail.itemsConvert(payoutDetail.payoutType?.name, payoutItems.get())
         }
         return payoutDetail
     }

     // 지급 과목상세 저장(임시저장 포함)
     @Transactional
     fun savePayoutsDetail(reqItemPayout: ReqItemPayoutSave): ResPayoutItem<MutableList<String>>? {
         var resItemsMap = mutableMapOf<String, Any>()
         var itemids = mutableListOf<String>()
         if (reqItemPayout.payoutId.isNullOrEmpty()) {
             // insert
             // 지급 정보를 저장
             val payoutInfo = payoutRepository.save(reqItemPayout.toPayoutsPayable(AccountsPayable()))
             // 지급 항목 리스트를 변환 및 저장
             reqItemPayout.payoutId = payoutInfo?.id
             val payoutItemsList = payoutInfo.id?.let { reqItemPayout.addPayoutsItemPayable(it) } as MutableList<AccountsPayableItem>
             for (payoutItems in payoutItemsList) { // 지급 항목 리스트를 반복 처리
                 var newItems = payoutItemRepository.save(payoutItems)
                 if (newItems?.id == null) {
                     println("저장 오류 payoutItems : ${MapperUtil.logMapCheck(payoutItems)}")
                 }else{
                     println("newItemsId : ${newItems?.id}")
                     itemids.add(newItems?.id as String)
                 }
             }

         } else {
             // update
             val payoutInfo = payoutRepository.findById(reqItemPayout.payoutId.toString())
             if (payoutInfo.isPresent) { // 지급 정보가 존재하는 경우 처리
                 var payoutsId = reqItemPayout.payoutId!!
                 // 지급 정보 업데이트
                 payoutRepository.save(reqItemPayout.toPayoutsPayable(payoutInfo.get()))

                 // 지급 업데이트 ItemId 확인
                 val payoutItems: List<AccountsPayableItem>? = reqItemPayout.addPayoutsItemPayable(payoutsId)
                 val idsToNew = payoutItems?.map { it.id }?.toList() ?: emptyList()
                 val oldItemsList = payoutItemRepository.findByTxId(payoutsId).getOrElse { emptyList() }
                 val deleteByItems = oldItemsList.filter { it.id !in idsToNew }.map { it.id }
                 if (deleteByItems.isNotEmpty()) {
                     payoutItemRepository.deleteAllById(deleteByItems)
                 }

                 log.debug("filteredItems : ${MapperUtil.logMapCheck(deleteByItems)}")
                 log.debug("idsToNew : ${MapperUtil.logMapCheck(idsToNew)}")
                 log.debug("oldItemsList : ${MapperUtil.logMapCheck(oldItemsList.map { it.id }.toList())}")

                 if (reqItemPayout.payoutType == AccountingPayoutType.EMPLOYEE) {
                     reqItemPayout.employeeItems?.map { reqItem ->
                         if (reqItem.payoutItemId?.trim().isNullOrEmpty()) { // 지급 항목 ID가 없거나 항목 정보가 존재하지 않는 경우 추가
                             var accountsPayableItem = reqItem.toItems(payoutsId, AccountsPayableItem())
                             accountsPayableItem.txId = payoutsId
                             accountsPayableItem.payoutCaseType = PayoutAccountCaseType.EMPLOYEE.name
                             var payoutItemAdd = payoutItemRepository.save(accountsPayableItem)
                             itemids.add(payoutItemAdd?.id as String)
                         } else {
                             println("payoutType error : ${reqItemPayout.payoutType}")
                             val itemsInfo = payoutItemRepository.findById(reqItem.payoutItemId?.trim().toString())
                             // 지급 항목 ID가 존재하고 기존 항목이 존재하며 ID가 비어있지 않은 경우
                             if (itemsInfo != null) {
                                 var accountsPayableItem = reqItem.toItems(payoutsId, itemsInfo.get())
                                 var payoutItemUpdate = payoutItemRepository.save(accountsPayableItem)
                                 itemids.add(payoutItemUpdate?.id as String)
                             } else {
                                 throw HttpMessageNotReadableException("EMPLOYEE payoutItemId is null : ${reqItem.payoutItemId}")
                             }
                         }
                     }
                 } else if (reqItemPayout.payoutType == AccountingPayoutType.VENDOR) {
                     itemids.addAll(addVendorItems(payoutsId, PayoutAccountCaseType.MATERIAL, reqItemPayout.materialItems))
                     itemids.addAll(addVendorItems(payoutsId, PayoutAccountCaseType.GENERAL, reqItemPayout.generalItems))
                 }
             }
         }
         println("savePayoutsDetail itemids : $itemids")
         return ResPayoutItem(reqItemPayout.payoutId, itemids)
     }

     // payoutType이 Vendor인 케이스 처리.
     private fun addVendorItems(payoutsId:String, payoutCaseType:PayoutAccountCaseType, payoutsItems: List<ReqItemsVendor>?) : List<String> {
         var itemids = mutableListOf<String>()
         payoutsItems?.map { reqItem ->
             if (reqItem.payoutItemId?.trim().isNullOrEmpty()) { // 지급 항목 ID가 없거나 항목 정보가 존재하지 않는 경우 추가
                 var accountsPayableItem = reqItem.toItems(payoutsId, AccountsPayableItem())
                 accountsPayableItem.txId = payoutsId
                 accountsPayableItem.payoutCaseType = payoutCaseType.name
                 var payoutItemAdd = payoutItemRepository.save(accountsPayableItem)
                 itemids.add(payoutItemAdd?.id as String)
             } else {
                 println("itemId : ${reqItem.payoutItemId?.trim().toString()}")
                 val itemsInfo = payoutItemRepository.findById(reqItem.payoutItemId?.trim().toString())
                 if (reqItem.payoutItemId != null && itemsInfo?.isPresent == true) {
                     // 지급 항목 ID가 존재하고 기존 항목이 존재하며 ID가 비어있지 않은 경우
                     var accountsPayableItem = reqItem.toItems(payoutsId, itemsInfo.get())
                     if(payoutCaseType == PayoutAccountCaseType.GENERAL){
                         accountsPayableItem.amount = reqItem.payoutAmount
                     }
                     var payoutItemUpdate = payoutItemRepository.save(accountsPayableItem)
                     itemids.add(payoutItemUpdate?.id as String)
                 } else {
                     throw HttpMessageNotReadableException("VENDOR payoutItemId is null : ${reqItem.payoutItemId}")
                 }
             }
         }
         return itemids
     }

     fun deletePayoutsDetail(payoutsId: String?): ResPayout? {
         // payoutsId가 null이거나 빈 문자열인 경우 처리
         if (payoutsId.isNullOrBlank()) {
             log.warn("유효하지 않은 payoutId: $payoutsId")
             return null
         }

         // ID로 지급 정보를 조회
         val payoutInfo = payoutRepository.findById(payoutsId)
         // TODO 승인 프로세스 도입되면 삭제 기능 수정 예정

         // 지급 정보가 존재하는지 확인
         return if (payoutInfo.isPresent) {
             val payoutDelete = payoutRepository.deleteById(payoutsId)
             log.info("payoutDelete: ${payoutDelete}}")
                 // 지급 항목을 조회
                 val payoutItemList = payoutItemRepository.findByTxId(payoutsId)?.orElse(null)

                 var code: Boolean = false
                 // 항목이 존재하는 경우, ID를 추출하여 삭제
                 payoutItemList?.let { items ->
                     val idsToDelete = items.map { it.id }
                     // 삭제 전 엔티티 수 확인
                     val countBeforeDelete = payoutItemRepository.countByIdIn(idsToDelete)
                     // 삭제 작업 수행
                     payoutItemRepository.deleteAllById(idsToDelete)
                     // 삭제 후 엔티티 수 확인
                     val countAfterDelete = payoutItemRepository.countByIdIn(idsToDelete)
                     // 삭제가 성공했는지 확인
                     if (countBeforeDelete > countAfterDelete) {
                         code = true
                     }
                 }
                 ResPayout(payoutsId)
         } else {
             throw HttpMessageNotReadableException("Payout info not found for ID: $payoutsId")
         }
     }

}
