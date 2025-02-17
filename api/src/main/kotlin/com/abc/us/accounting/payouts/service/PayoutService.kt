 package com.abc.us.accounting.payouts.service

import com.abc.us.accounting.payouts.domain.entity.AccountsPayable
import com.abc.us.accounting.payouts.domain.entity.AccountsPayableItem
import com.abc.us.accounting.payouts.domain.repository.AccountPayableRepository
import com.abc.us.accounting.payouts.domain.repository.AccountsPayableItemRepository
import com.abc.us.accounting.payouts.domain.repository.IAccountPayableRepository
import com.abc.us.accounting.model.ApiResponse
import com.abc.us.accounting.model.ResHeader
import com.abc.us.accounting.payouts.model.request.ReqPayoutExcelUploadDto
import com.abc.us.accounting.payouts.model.request.ReqPayoutInqyDto
import com.abc.us.accounting.payouts.model.response.ResPayoutInfoDto
import com.abc.us.accounting.supports.FileUtil
import com.abc.us.accounting.supports.mapper.MapperUtil
import com.abc.us.accounting.supports.excel.ExcelCellDto
import com.abc.us.accounting.supports.excel.ExcelRowDto
import com.abc.us.accounting.supports.excel.ExcelUtil
import com.abc.us.generated.models.AccountingPayoutType
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile


 @Service
class PayoutService(
     val payoutRepository: AccountPayableRepository,
     val iPayoutRepository: IAccountPayableRepository,
     val AccountsPayableItemRepository: AccountsPayableItemRepository,
     //val payoutRepositoryImpl: PayoutRepositoryImpl,
) {

    // 지급 현황 조회
    fun selectPayoutList(reqData: ReqPayoutInqyDto): Page<ResPayoutInfoDto>? {
        val sortByTypeId = reqData.sortBy.typeId.takeIf { !it.isNullOrBlank() } ?: "CREATE_TIME"
        var pageable: Pageable = PageRequest.of(
            if (0 <= 1) 0 else reqData.current,
            reqData.size,
            reqData.direction,
            sortByTypeId
        )
        return payoutRepository.findByPayoutList(pageable, reqData)
    }

    //  엑셀 다운로드
    fun selectPayoutExcelDown(response: HttpServletResponse, reqPayouts: ReqPayoutInqyDto) {
        val res: Page<ResPayoutInfoDto> = payoutRepository.findByPayoutList(null, reqPayouts)
        val excludedProperties = setOf("rownum", "totalCnt")
        val propertyNames = FileUtil.getFieldNames(excludedProperties, ResPayoutInfoDto::class.java)
        println("res : ${MapperUtil.logMapCheck(res.content)}")
        println("propertyNames : $propertyNames")
        val transformRes = reqPayouts.transformRes(res)
        val fileName = FileUtil.getFileName("Payouts-Data-by-Period")
        ExcelUtil.download(
            propertyNames,
            transformRes,
            fileName,
            response
        )
    }

    // HSSFWorkbook, XSSFWorkbook
    fun selectPayoutExcelUpload(file: MultipartFile): ApiResponse<ExcelRowDto<List<ReqPayoutExcelUploadDto>>> {
        val excludedProperties = setOf("rownum", "totalCnt", "payoutId", "approvalStatus")
//        val excludedIsNull = setOf("payoutId", "vendorId", "supplyVendorId")
        val excludedIsNull = setOf(
//            "payoutId", "approvalStatus", "payoutGroup", "payoutType",
//            "supplierId", "costCenter", "drafterId", "documentId",
//            "documentDate", "entryDate", "postingDate", "dueDate",
//            "currency", "remark", "invoiceId", "purchaseOrderId",
//            "billOfLadingId", "payoutAmount",
            "itemsAmount", "itemsUnitPrice",
            "itemsQuantity", "itemsAccountCode", "itemsAccountName", "itemsGoods",
            "itemsNarrative", "itemsBudgetUsageDate",
            "itemsBudgetDepartmentAmount", "itemsBudgetDepartmentName", "itemsBudgetAllocation",
            "itemsRemark"
        )


        val propertyNames = FileUtil.getFieldNames(excludedProperties, ReqPayoutExcelUploadDto::class.java)
//        var excludedIsNull = propertyNames.toSet()
        println("propertyNames : $propertyNames")
        val excelUploadList: MutableList<ExcelRowDto<ReqPayoutExcelUploadDto>> =
            ExcelUtil.getDataFromFile<ReqPayoutExcelUploadDto>(
                file,
                propertyNames,
                excludedIsNull
            )
        // excelUploadList에서 payoutType이 null이 아닌 항목만 필터링하고, 각 row의 valueObject를 추출하여 정렬
        val reqUploadData = excelUploadList
            .filter { it.rowList.payoutType != null }                       // payoutType이 null이 아닌 항목 필터링
            .map { it.rowList }                                             // 각 row의 valueObject만 추출
            .sortedWith(compareBy({ it.payoutGroup }, { it.payoutType }))   // payoutGroup과 payoutType으로 정렬

        // isFailed가 true인 항목만 필터링하여 cellList 생성
        val cellList: MutableList<ExcelCellDto> = excelUploadList.flatMap { row ->
            row.cellList?.filter { it.isFailed == true } ?: emptyList()     // isFailed가 true인 항목 필터링
        }.distinctBy { it.rowName }                                         // 중복 제거 (rowName 기준으로)
            .sortedWith(compareBy<ExcelCellDto> { it.rowIndex }.thenBy { it.columnIndex }) // rowIndex와 columnIndex로 정렬
            .toMutableList()                                                    // MutableList로 변환
        println("!cellList.isNullOrEmpty() : ${cellList.isNullOrEmpty()}")
        if (cellList.isNullOrEmpty()) { // 성공
            // List<List<ReqPayoutExcelUploadDto>>
            // reqUploadData를 payoutGroup 기준으로 그룹화
            var groupedPayouts = reqUploadData.groupBy { it.payoutGroup }.values.map { it.toList() }

            // AccountsPayable 및 AccountsPayableItem을 담을 리스트 초기화
            var accountsPayableList: MutableList<AccountsPayable> = mutableListOf()
            var accountsPayableItemList: MutableList<AccountsPayableItem> = mutableListOf()

            for ((index, group) in groupedPayouts.withIndex()) {
                // 첫 번째 항목을 기준으로 AccountsPayable 객체 생성
                var groupFirstItems = group.first()
                var reqAccountsPayable = groupFirstItems.toPayoutsPayable()
                // AccountsPayable 리스트에 추가
                accountsPayableList.add(reqAccountsPayable)

                // 해당 그룹의 itemsAmount 합계 계산
                var payoutAmount = group.sumOf { it.itemsAmount?.toDouble() ?: 0.0 }

                // 합계가 요청된 지급 금액과 일치하는지 확인
                if (payoutAmount != reqAccountsPayable.payoutAmount) {
                    cellList.add(
                        ExcelCellDto(
                            isFailed = true,
                            rowIndex = index + 1,
                            message = "itemsAmount totals do not match payoutAmount"
                        )
                    )
                }

                for (payoutItems in group) {
                    if (payoutItems.payoutType == AccountingPayoutType.ALL.name) {
                        cellList.add(
                            ExcelCellDto(
                                isFailed = true,
                                rowIndex = index + 1,
                                message = "[payoutType] ALL is not allowed"
                            )
                        )
                    }
                    if (payoutItems.payoutType == AccountingPayoutType.VENDOR.name) {
                        // itemsAmount와 (itemsUnitPrice * itemsQuantity) 일치 확인
                        if (payoutItems.getItemsAmountSum() != payoutItems.itemsAmount?.toDouble()) {
                            cellList.add(
                                ExcelCellDto(
                                    isFailed = true,
                                    rowIndex = index + 1,
                                    message = "(itemsUnitPrice * itemsQuantity) does not match itemsAmount"
                                )
                            )
                        }
                    }
                    // 지급 항목 추가
                    var newItems = payoutItems.addPayoutsItemPayable()
                    if (newItems != null) {
                        accountsPayableItemList.add(newItems) // 유효한 항목인 경우 리스트에 추가
                    }
                }
            }

            if (cellList.isNullOrEmpty()) {
                // 신규 등록  Saving to repositories
                if (accountsPayableList.isNotEmpty() && accountsPayableItemList.isNotEmpty()) {
                    val savedPayoutInfo = iPayoutRepository.saveAll(accountsPayableList)
                    println("Saved Accounts Payable: $savedPayoutInfo")
                    if (accountsPayableItemList.isNotEmpty()) {
                        val savedPayoutItems = AccountsPayableItemRepository.saveAll(accountsPayableItemList)
                        println("Saved Accounts Payable Items: $savedPayoutItems")
                    }
                    return ApiResponse(ResHeader(), ExcelRowDto(reqUploadData, cellList))
                }
            }
        }
        return ApiResponse(ResHeader().failed(), ExcelRowDto(reqUploadData, cellList))
    }

}
