package com.abc.us.accounting.logistics.service

import com.abc.us.accounting.logistics.domain.type.MovementCategory
import com.abc.us.accounting.logistics.domain.type.MovementGroup
import com.abc.us.accounting.logistics.domain.type.MovementType
import com.abc.us.accounting.logistics.model.request.LogisticsInventoryCostStatusRequest
import com.abc.us.accounting.logistics.model.request.LogisticsInventoryMovementStatusRequest
import com.abc.us.accounting.logistics.model.response.LogisticsInventoryCostStatusData
import com.abc.us.accounting.logistics.model.response.LogisticsInventoryMovementStatusData
import com.abc.us.accounting.model.ApiPageResponse
import com.abc.us.generated.models.*
import com.opencsv.CSVWriter
import org.springframework.core.io.InputStreamResource
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ModelAttribute
import java.io.*
import java.math.BigDecimal
import java.time.LocalDate

@Service
class LogisticsService {

    fun getInventoryCostStatus(
        req: LogisticsInventoryCostStatusRequest
    ): Page<LogisticsInventoryCostStatusData> {
        // 현재는 더미데이터. 이후 데이터베이스 조회로직 추가 예정
        val data1 = LogisticsInventoryCostStatusData(
            costUpdateDate = "2024-06-30".let { LocalDate.parse(it) },
            inventoryCostId = "WP_113816-0003",
            materialId = "WP_113816",
            warehouseId = "warehouse1",
            warehouseName = "Dallas CDC",
            inventoryCost = BigDecimal("526.22"),
            materialName = "NECOA NT B/I Icy Silver",
            modelName = "CHP-1110N_SL",
            materialType = MaterialType.PRODUCT,
            materialCategory = MaterialCategoryCode.WATER_PURIFIER,
            installType = MaterialAttributeInstallationTypeCode.BUILT_IN,
            filterType = MaterialAttributeFilterTypeCode.NANO_TRAP,
            featureType = MaterialAttributeKeyFeatureCode.COLD_HOT_PURIFIED,
        )
        val data2 = LogisticsInventoryCostStatusData(
            costUpdateDate = "2024-06-30".let { LocalDate.parse(it) },
            inventoryCostId = "WP_113818-0003",
            materialId = "WP_113818",
            warehouseId = "warehouse1",
            warehouseName = "Dallas CDC",
            inventoryCost = BigDecimal("526.22"),
            materialName = "NECOA NT B/I Pebble Gray",
            modelName = "CHP-1110N_GR",
            materialType = MaterialType.PRODUCT,
            materialCategory = MaterialCategoryCode.WATER_PURIFIER,
            installType = MaterialAttributeInstallationTypeCode.BUILT_IN,
            filterType = MaterialAttributeFilterTypeCode.NANO_TRAP,
            featureType = MaterialAttributeKeyFeatureCode.COLD_HOT_PURIFIED,
        )
        val data3 = LogisticsInventoryCostStatusData(
            costUpdateDate = "2024-06-30".let { LocalDate.parse(it) },
            inventoryCostId = "WP_113819-0003",
            materialId = "WP_113819",
            warehouseId = "warehouse1",
            warehouseName = "Dallas CDC",
            inventoryCost = BigDecimal("526.22"),
            materialName = "NECOA NT B/I Bronze Beige",
            modelName = "CHP-1110N_BR",
            materialType = MaterialType.PRODUCT,
            materialCategory = MaterialCategoryCode.WATER_PURIFIER,
            installType = MaterialAttributeInstallationTypeCode.BUILT_IN,
            filterType = MaterialAttributeFilterTypeCode.NANO_TRAP,
            featureType = MaterialAttributeKeyFeatureCode.COLD_HOT_PURIFIED,
        )
        val dataList = listOf(data1, data2, data3)
        val pageable: Pageable = Pageable.ofSize(10)
        return PageImpl(dataList, pageable, dataList.size.toLong())
    }

    fun downloadInventoryCostStatus(
        req: LogisticsInventoryCostStatusRequest
    ): InputStreamResource{
        // 파일 다운로드는 코드값을 value로 변경해야한다
        // 현재는 더미데이터. 이후 데이터베이스 조회로직 추가 예정
        val data1 = LogisticsInventoryCostStatusData(
            costUpdateDate = "2024-06-30".let { LocalDate.parse(it) },
            inventoryCostId = "WP_113816-0003",
            materialId = "WP_113816",
            warehouseId = "warehouse1",
            warehouseName = "Dallas CDC",
            inventoryCost = BigDecimal("526.22"),
            materialName = "NECOA NT B/I Icy Silver",
            modelName = "CHP-1110N_SL",
            materialType = MaterialType.PRODUCT,
            materialCategory = MaterialCategoryCode.WATER_PURIFIER,
            installType = MaterialAttributeInstallationTypeCode.BUILT_IN,
            filterType = MaterialAttributeFilterTypeCode.NANO_TRAP,
            featureType = MaterialAttributeKeyFeatureCode.COLD_HOT_PURIFIED,
        )
        val data2 = LogisticsInventoryCostStatusData(
            costUpdateDate = "2024-06-30".let { LocalDate.parse(it) },
            inventoryCostId = "WP_113818-0003",
            materialId = "WP_113818",
            warehouseId = "warehouse1",
            warehouseName = "Dallas CDC",
            inventoryCost = BigDecimal("526.22"),
            materialName = "NECOA NT B/I Pebble Gray",
            modelName = "CHP-1110N_GR",
            materialType = MaterialType.PRODUCT,
            materialCategory = MaterialCategoryCode.WATER_PURIFIER,
            installType = MaterialAttributeInstallationTypeCode.BUILT_IN,
            filterType = MaterialAttributeFilterTypeCode.NANO_TRAP,
            featureType = MaterialAttributeKeyFeatureCode.COLD_HOT_PURIFIED,
        )
        val data3 = LogisticsInventoryCostStatusData(
            costUpdateDate = "2024-06-30".let { LocalDate.parse(it) },
            inventoryCostId = "WP_113819-0003",
            materialId = "WP_113819",
            warehouseId = "warehouse1",
            warehouseName = "Dallas CDC",
            inventoryCost = BigDecimal("526.22"),
            materialName = "NECOA NT B/I Bronze Beige",
            modelName = "CHP-1110N_BR",
            materialType = MaterialType.PRODUCT,
            materialCategory = MaterialCategoryCode.WATER_PURIFIER,
            installType = MaterialAttributeInstallationTypeCode.BUILT_IN,
            filterType = MaterialAttributeFilterTypeCode.NANO_TRAP,
            featureType = MaterialAttributeKeyFeatureCode.COLD_HOT_PURIFIED,
        )
        val dataList = listOf(data1, data2, data3)

        val headers = listOf(
            "갱신일",
            "재고원가ID",
            "자재ID",
            "물류센터ID",
            "물류센터",
            "재고원가",
            "자재이름",
            "모델명",
            "자재유형",
            "카테고리",
            "설치유형",
            "필터",
            "기능군",
        )

        val byteArrayOutputStream = ByteArrayOutputStream()
        val writer = OutputStreamWriter(byteArrayOutputStream)
        val csvWriter = CSVWriter(writer, ',', CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)

        // CSV 파일의 헤더와 데이터를 기록
        csvWriter.writeNext(headers.toTypedArray())
        dataList.forEach { it ->
            csvWriter.writeNext(
                listOf(
                    it.costUpdateDate.toString(),
                    it.inventoryCostId,
                    it.materialId,
                    it.warehouseId,
                    it.warehouseName,
                    it.inventoryCost.toString(),
                    it.materialName,
                    it.modelName,
                    it.materialType.toString(),
                    it.materialCategory.toString(),
                    it.installType.toString(),
                    it.filterType.toString(),
                    it.featureType.toString()
                ).toTypedArray()
            )
        }
        csvWriter.flush()

        return InputStreamResource(ByteArrayInputStream(byteArrayOutputStream.toByteArray()))

    }

    fun getInventoryMovementStatus(
        @ModelAttribute req: LogisticsInventoryMovementStatusRequest
    ): Page<LogisticsInventoryMovementStatusData> {
        // 현재는 더미데이터. 이후 데이터베이스 조회로직 추가 예정
        val data1 = LogisticsInventoryMovementStatusData(
            movementId = "2401010234",
            movementDate = "2025-01-01".let { LocalDate.parse(it) },
            warehouseId = "warehouse1",
            warehouseName = "Dallas CDC",
            materialId = "WP_113816",
            movementCategory = MovementCategory.INBOUND.code,
            movementGroup = MovementGroup.INBOUND_PURCHASE.code,
            movementType = MovementType.INBOUND_PURCHASE_ORDER.code,
            quantity = 100,
            unitPrice = BigDecimal("510.00"),
            amount = BigDecimal("51000.00"),
            inventoryQuantity = 100,
            inventoryUnitPrice = BigDecimal("510.00"),
            inventoryValue = BigDecimal("51000.00"),
            inventoryCostId = "WP_113816-0001",
            materialName = "NECOA NT B/I Icy Silver"
        )
        val data2 = LogisticsInventoryMovementStatusData(
            movementId = "2401230003",
            movementDate = "2025-01-23".let { LocalDate.parse(it) },
            warehouseId = "warehouse1",
            warehouseName = "Dallas CDC",
            materialId = "WP_113816",
            movementCategory = MovementCategory.OUTBOUND.code,
            movementGroup = MovementGroup.OUTBOUND_SALES.code,
            movementType = MovementType.OUTBOUND_SALES_ORDER.code,
            quantity = 100,
            unitPrice = BigDecimal("510.00"),
            amount = BigDecimal("51000.00"),
            inventoryQuantity = 100,
            inventoryUnitPrice = BigDecimal("510.00"),
            inventoryValue = BigDecimal("51000.00"),
            inventoryCostId = "WP_113816-0001",
            materialName = "NECOA NT B/I Icy Silver"
        )
        val data3 = LogisticsInventoryMovementStatusData(
            movementId = "2401010234",
            movementDate = "2025-01-01".let { LocalDate.parse(it) },
            warehouseId = "warehouse1",
            warehouseName = "Dallas CDC",
            materialId = "WP_113816",
            movementCategory = MovementCategory.IN_TRANSIT.code,
            movementGroup = MovementGroup.IN_TRANSIT.code,
            movementType = MovementType.IN_TRANSIT.code,
            quantity = 100,
            unitPrice = BigDecimal("510.00"),
            amount = BigDecimal("51000.00"),
            inventoryQuantity = 100,
            inventoryUnitPrice = BigDecimal("510.00"),
            inventoryValue = BigDecimal("51000.00"),
            inventoryCostId = "WP_113816-0001",
            materialName = "NECOA NT B/I Icy Silver"
        )
        val dataList = listOf(data1, data2, data3)
        val pageable: Pageable = Pageable.ofSize(10)
        return PageImpl(dataList, pageable, dataList.size.toLong())
    }

    fun downloadInventoryMovementStatus(
        req: LogisticsInventoryMovementStatusRequest
    ): InputStreamResource{
        // 파일 다운로드는 코드값을 value로 변경해야한다
        // 현재는 더미데이터. 이후 데이터베이스 조회로직 추가 예정
        val data1 = LogisticsInventoryMovementStatusData(
            movementId = "2401010234",
            movementDate = "2025-01-01".let { LocalDate.parse(it) },
            warehouseId = "warehouse1",
            warehouseName = "Dallas CDC",
            materialId = "WP_113816",
            movementCategory = MovementCategory.INBOUND.code,
            movementGroup = MovementGroup.INBOUND_PURCHASE.code,
            movementType = MovementType.INBOUND_PURCHASE_ORDER.code,
            quantity = 100,
            unitPrice = BigDecimal("510.00"),
            amount = BigDecimal("51000.00"),
            inventoryQuantity = 100,
            inventoryUnitPrice = BigDecimal("510.00"),
            inventoryValue = BigDecimal("51000.00"),
            inventoryCostId = "WP_113816-0001",
            materialName = "NECOA NT B/I Icy Silver"
        )
        val data2 = LogisticsInventoryMovementStatusData(
            movementId = "2401230003",
            movementDate = "2025-01-23".let { LocalDate.parse(it) },
            warehouseId = "warehouse1",
            warehouseName = "Dallas CDC",
            materialId = "WP_113816",
            movementCategory = MovementCategory.OUTBOUND.code,
            movementGroup = MovementGroup.OUTBOUND_SALES.code,
            movementType = MovementType.OUTBOUND_SALES_ORDER.code,
            quantity = 100,
            unitPrice = BigDecimal("510.00"),
            amount = BigDecimal("51000.00"),
            inventoryQuantity = 100,
            inventoryUnitPrice = BigDecimal("510.00"),
            inventoryValue = BigDecimal("51000.00"),
            inventoryCostId = "WP_113816-0001",
            materialName = "NECOA NT B/I Icy Silver"
        )
        val data3 = LogisticsInventoryMovementStatusData(
            movementId = "2401010234",
            movementDate = "2025-01-01".let { LocalDate.parse(it) },
            warehouseId = "warehouse1",
            warehouseName = "Dallas CDC",
            materialId = "WP_113816",
            movementCategory = MovementCategory.IN_TRANSIT.code,
            movementGroup = MovementGroup.IN_TRANSIT.code,
            movementType = MovementType.IN_TRANSIT.code,
            quantity = 100,
            unitPrice = BigDecimal("510.00"),
            amount = BigDecimal("51000.00"),
            inventoryQuantity = 100,
            inventoryUnitPrice = BigDecimal("510.00"),
            inventoryValue = BigDecimal("51000.00"),
            inventoryCostId = "WP_113816-0001",
            materialName = "NECOA NT B/I Icy Silver"
        )
        val dataList = listOf(data1, data2, data3)

        val headers = listOf(
            "수불ID",
            "수불일자",
            "물류센터ID",
            "물류센터",
            "자재ID",
            "수불분류",
            "수불그룹",
            "수불유형",
            "수량",
            "단가",
            "금액",
            "재고수량",
            "재고단가",
            "재고금액",
            "재고원가ID",
            "자재이름"
        )

        val byteArrayOutputStream = ByteArrayOutputStream()
        val writer = OutputStreamWriter(byteArrayOutputStream)
        val csvWriter = CSVWriter(writer, ',', CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)

        // CSV 파일의 헤더와 데이터를 기록
        csvWriter.writeNext(headers.toTypedArray())
        dataList.forEach { item ->
            csvWriter.writeNext(
                listOf(
                    item.movementId,
                    item.movementDate.toString(),
                    item.warehouseId,
                    item.warehouseName,
                    item.materialId,
                    item.movementCategory,
                    item.movementGroup,
                    item.movementType,
                    item.quantity.toString(),
                    item.unitPrice.toString(),
                    item.amount.toString(),
                    item.inventoryQuantity.toString(),
                    item.inventoryUnitPrice.toString(),
                    item.inventoryValue.toString(),
                    item.inventoryCostId,
                    item.materialName
                ).toTypedArray()
            )
        }
        csvWriter.flush()

        return InputStreamResource(ByteArrayInputStream(byteArrayOutputStream.toByteArray()))

    }

}