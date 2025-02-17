 package com.abc.us.accounting.payouts.service


import com.abc.us.accounting.payouts.domain.entity.ApprovalRequest
import com.abc.us.accounting.payouts.model.request.ReqApprovalAddPayout
import com.abc.us.accounting.payouts.model.request.ReqApprovalRequestPayout
import com.abc.us.accounting.payouts.model.response.ResApprovalRequestPayout
import com.abc.us.accounting.payouts.domain.repository.ApprovalRequestRepository
import com.abc.us.generated.models.AccountingPayoutApprovalStatus
import org.springframework.stereotype.Service
import java.time.OffsetDateTime


 @Service
class ApprovalRequestService(
     var approvalRequestRepository: ApprovalRequestRepository,
) {

    fun selectApprovalReqByTxId(payoutId: String): ResApprovalRequestPayout? {
        // 상태값과 txId에 따라 필터링하여 ApprovalRequest 리스트
        val approvalRequestList: MutableList<ApprovalRequest> = approvalRequestRepository.findAllByTxId(payoutId)
        println("approvalRequestList : $approvalRequestList")
        if(!approvalRequestList.isNullOrEmpty()){
            // ApprovalRequest 목록을 ResApprovalRequestPayout 목록으로 변환하여 반환
            return ResApprovalRequestPayout.approvalListConvert(approvalRequestList).last()
        }
        return null
    }

    /**
     * 조회시 데이터가 없을때
     * 조회시 데이터가 있을때
     */
    fun addApprovalReqByTxId(payoutId: String, reqApproval: ReqApprovalAddPayout): ResApprovalRequestPayout? {
        // 요청된 ID로 ApprovalRequest를 조회
        val approvalRequest = approvalRequestRepository.findByTxId(reqApproval.payoutId.toString())
        if (approvalRequest.isPresent) {
            // Optional에서 ApprovalRequest 객체 호출
            val request = reqApproval.approvalRequest(payoutId, ApprovalRequest())
            request.issueTime = OffsetDateTime.now()
            // 수정된 ApprovalRequest를 저장
            return reqApproval.approvalListConvert(approvalRequestRepository.save(request))
        }else{ // 조회된 객체가 존재하 않을때 처리
            println("approvalRequest.get() : ${approvalRequest.get()}")
            val request = reqApproval.approvalRequest(payoutId, approvalRequest.get().first())
            return reqApproval.approvalListConvert(approvalRequestRepository.save(request))
        }
    }

    fun updateApprovalReqByTxId(
        payoutId: String,
        approvalId: String,
        approvalStatus: AccountingPayoutApprovalStatus,
    ): ResApprovalRequestPayout? {
        // txId와 approverId를 통해 승인 요청을 조회합니다.
        val approvalRequest = approvalRequestRepository.findById(approvalId)
        if (approvalRequest.isPresent) { // 승인 요청이 존재하는지 확인
            // 요청 상태가 'APPROVED'이고 새로운 상태가 'CANCELLED' 또는 'REJECTED'인 경우 업데이트를 허용하지 않음., 이미 승인이라면 허용하지 않음.
            if (!((approvalStatus == AccountingPayoutApprovalStatus.CANCELLED || approvalStatus == AccountingPayoutApprovalStatus.REJECTED)
                        && approvalRequest.get().approvalStatus == AccountingPayoutApprovalStatus.APPROVED.name)
                && approvalRequest.get().approvalStatus != AccountingPayoutApprovalStatus.APPROVED.name
            ) {
//                updatePayoutApprovalStatus(payoutId, approvalStatus)
                var approvalReq = approvalRequest.get() // 업데이트 대상 추가
                approvalReq.approvalStatus = approvalStatus.name
                if(approvalRequest.get().approvalStatus == AccountingPayoutApprovalStatus.APPROVED.name){
                    approvalReq.issueTime = OffsetDateTime.now()
                }
                // 업데이트된 요청을 저장하고, 변환하여 반환합니다.
                return ReqApprovalRequestPayout().approvalListConvert(approvalRequestRepository.save(approvalReq))
            }
        }
        return null
    }

//    fun updatePayoutApprovalStatus(payoutId: String, approvalStatus: AccountingPayoutApprovalStatus) {
//        val payoutInfo = payoutRepository.findById(payoutId)?.get() ?: return
//        val approvalReqList = approvalRequestRepository.findAllByTxId(payoutId)
//        val submittedCnt = approvalReqList.count {
//            it.approvalStatus == AccountingPayoutApprovalStatus.SUBMITTED.name ||
//            it.approvalStatus == AccountingPayoutApprovalStatus.DRAFTING.name
//        }
//
//        when {
//            submittedCnt > 0 && payoutInfo.approvalStatus != AccountingPayoutApprovalStatus.SUBMITTED -> {
//                payoutInfo.approvalStatus = AccountingPayoutApprovalStatus.SUBMITTED
//            }
//            submittedCnt == 0 -> {
//                payoutInfo.approvalStatus = when (approvalStatus) {
//                    AccountingPayoutApprovalStatus.APPROVED,
//                    AccountingPayoutApprovalStatus.REJECTED,
//                    AccountingPayoutApprovalStatus.CANCELLED -> approvalStatus
//                    else -> AccountingPayoutApprovalStatus.DRAFTING
//                }
//            }
//        }
//
//        payoutRepository.save(payoutInfo)
//    }

}