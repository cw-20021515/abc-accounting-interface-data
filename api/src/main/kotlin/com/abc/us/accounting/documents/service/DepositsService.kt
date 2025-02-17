package com.abc.us.accounting.documents.service

import com.abc.us.accounting.documents.domain.repository.DepositsRepository
import com.abc.us.accounting.documents.model.DepositsData
import com.abc.us.accounting.documents.model.DepositsRequest
import com.abc.us.accounting.supports.excel.ExcelUtil
import com.abc.us.accounting.supports.mapper.MapperUtil
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class DepositsService(
    private val depositsRepository: DepositsRepository
) {

    fun findDeposits(
        req: DepositsRequest
    ): Page<DepositsData> {
        return findByReq(
            req,
            PageRequest.of(
                req.current - 1,
                req.size
            )
        )
    }

    fun depositsExcelDownload(
        req: DepositsRequest,
        response: HttpServletResponse
    ) {
        val res = findByReq(req).toList()
        val headers = listOf(
            "수납일자",
            "수납ID",
            "수납유형",
            "수납상태",
            "수납대행사",
            "수납수단",
            "통화",
            "수납액",
            "수수료",
            "입금액",
            "차이",
            "입금일자",
            "입금ID",
            "청구ID",
            "주문ID",
            "고객ID",
            "참조ID",
            "참조유형",
            "비고"
        )
        val datas = res.map {
            listOf(
                it.receiptDate,
                it.receiptId,
                it.receiptType,
                it.receiptStatus,
                it.receiptProvider,
                it.receiptMethod,
                it.currency,
                it.receiptAmount,
                it.fee,
                it.depositAmount,
                it.differenceAmount,
                it.depositDate,
                it.depositId,
                it.billId,
                it.orderId,
                it.customerId,
                it.referenceId,
                it.referenceType,
                it.remark
            )
        } as List<List<Any>>
        val fileName = "입금현황"
        ExcelUtil.download(
            headers,
            datas,
            fileName,
            response
        )
    }

    fun findByReq(
        req: DepositsRequest,
        pageable: Pageable? = null
    ): Page<DepositsData> {
        val page = depositsRepository.findByReq(
            req.periodType,
            req.periodFromDate,
            req.periodToDate,
            req.receiptType,
            req.receiptStatus,
            req.receiptMethod,
            req.orderId,
            req.customerId,
            req.depositId,
            pageable
        )
        return MapperUtil.convert(page)
    }
}