package com.abc.us.accounting.collects.works.inventory

import com.abc.us.accounting.collects.domain.entity.collect.CollectInventoryValuation
import com.abc.us.accounting.collects.domain.entity.collect.CollectMaterial
import com.abc.us.accounting.collects.domain.repository.CollectInventoryValuationRepository
import com.abc.us.accounting.collects.domain.repository.CollectMaterialRepository
import com.abc.us.accounting.commons.domain.type.TimeZoneCode
import com.abc.us.accounting.logistics.domain.type.InventoryAssetGradeType
import com.abc.us.accounting.logistics.domain.type.MovementType
import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.supports.converter.toOffsetAt
import com.abc.us.accounting.supports.converter.toYearMonth
import com.abc.us.accounting.supports.entity.BulkDistinctInserter
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.*
import java.util.*
import kotlin.random.Random

@Service
class CollectsInventoryWork(
    private val materialRepository : CollectMaterialRepository,
    private val InventoryValuationRepository : CollectInventoryValuationRepository,
    private val bulkInserter : BulkDistinctInserter,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun randomBigDecimal(min: BigDecimal, max: BigDecimal): BigDecimal {
        // Random 값을 생성해서 min과 max 사이의 값으로 변환
        val randomValue = min.toDouble() + (max.toDouble() - min.toDouble()) * Random.nextDouble()
        // 소수점 4자리까지 반올림하여 반환
        return BigDecimal(randomValue).setScale(4, RoundingMode.HALF_UP)
    }
    fun iterateOverEachMonth(startYM : YearMonth, endYM : YearMonth, process : (LocalDate, LocalDate) -> Boolean ) {
        var currentMonth = startYM

        while (currentMonth.isBefore(endYM) || currentMonth == endYM) {

            val firstDayOfMonth = currentMonth.atDay(1) // 첫 번째 날을 LocalDate로 변환
            val lastDayOfMonth = currentMonth.atEndOfMonth() // 마지막 날 계산

            process(firstDayOfMonth, lastDayOfMonth)

            println("Last day of ${currentMonth.month}: $lastDayOfMonth")

            // 다음 달로 이동
            currentMonth = currentMonth.plusMonths(1)
        }
    }

    fun buildInventoryValuation (material : CollectMaterial,
                                 startDate: LocalDate,
                                 endDate: LocalDate,
                                 grade : InventoryAssetGradeType,
                                 description : String) : CollectInventoryValuation {



        return CollectInventoryValuation(
//            baseTime = endDate.toOffsetAt(LocalTime.of(23, 0, 0)),
            issuedTime = endDate.toOffsetAt(LocalTime.of(23,59,59)),
//            recordTime = endDate.plusDays(1).toOffsetAt(LocalTime.of(2,0,0)),
                                         currency = Currency.getInstance("USD"),
//                                         relation = EmbeddableRelation().apply {
//                                             field = "NOTHING"
//                                             entity = "NOTHING"
//                                             value = "NOTHING"
//                                         },
                                         materialId = material.materialId,
//                                         materialName = material.materialName,
                                         movementType = MovementType.ENDING_INVENTORY,
//                                         materialProductType = material.productType.name,
                                         gradeType = grade,
                                         stockAvgUnitPrice =randomBigDecimal(BigDecimal(450.00),BigDecimal(1200.00)),
        ).apply {
//            modelName = material.materialModelName
            remark = description
//            createTime = OffsetDateTime.now()
//            updateTime = OffsetDateTime.now()

        }
    }
    @Transactional
    fun bulkInsert(inventoryValuations : MutableList<CollectInventoryValuation>) {

        bulkInserter.execute(InventoryValuationRepository,inventoryValuations)
        logger.info {
            "COLLECT-INVENTORY_VALUATION_INSERT[inventory_valuations=${inventoryValuations}]"
        }
    }
    fun collect(trailer: AsyncEventTrailer){

        val from = trailer.queries().get("fromDateTime") as LocalDateTime
        val to = trailer.queries().get("toDateTime") as LocalDateTime
        val timezone = trailer.queries().get("timezone") as TimeZoneCode


        logger.info { "COLLECT-START[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }
        val materials = materialRepository.findAll()
        val inventoryValuations = mutableListOf<CollectInventoryValuation>()
        iterateOverEachMonth(from.toYearMonth(),to.toYearMonth()) { startDate , endDate ->
            materials.forEach { material ->
                val gradeA =
                    buildInventoryValuation(material,startDate,endDate, InventoryAssetGradeType.GRADE_A,"기말재고 갱신")
                val gradB =
                    buildInventoryValuation(material,startDate,endDate, InventoryAssetGradeType.GRADE_B,"기말재고 갱신")
                inventoryValuations.add(gradeA)
                inventoryValuations.add(gradB)
            }
            true
        }
        bulkInsert(inventoryValuations)
        logger.info { "COLLECT-END[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }
    }
}