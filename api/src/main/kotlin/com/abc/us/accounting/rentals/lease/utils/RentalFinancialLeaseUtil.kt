package com.abc.us.accounting.rentals.lease.utils


import com.abc.us.accounting.rentals.lease.model.RentalFinancialLeaseScheduleInfo
import com.abc.us.accounting.rentals.lease.model.ReqRentalFinancialLeaseSchedule
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 금융삼각 스케쥴 서비스
 */
@Component
object RentalFinancialLeaseUtil {

    private val decimalFormat = DecimalFormat("#.####") // 소수점 4자리에서 반올림
    private val scale: Int = 4

    fun generateLeaseSchedule(reqRentalFinancialLeaseSchedule: ReqRentalFinancialLeaseSchedule): List<RentalFinancialLeaseScheduleInfo> {
        val schedule = mutableListOf<RentalFinancialLeaseScheduleInfo>()

        // 총 렌탈료를 기간으로 나눈 값
        val rentalPeriod = reqRentalFinancialLeaseSchedule.contractPeriod!!
        var monthlyRentalAmountForGoods = reqRentalFinancialLeaseSchedule.rentalAmountForGoods!!.toDouble() // 월별 렌탈료
        var monthlyRentalAmount = reqRentalFinancialLeaseSchedule.rentalAmount!!.toDouble() // 월별 렌탈료
        val interestRate = reqRentalFinancialLeaseSchedule.interestRate!! / 100 // 6.85 들어오면 백분율로 사용함.

        var contractDate:LocalDate = reqRentalFinancialLeaseSchedule.contractDate!!
        val totalDaysInMonth = contractDate.lengthOfMonth()
        val rentalStartDay = contractDate.dayOfMonth

        val lastMonths = contractDate.plusMonths(reqRentalFinancialLeaseSchedule.contractPeriod!!.toLong())
        // 첫 회차는 1월 10일부터 31일까지 일할 계산 (1월 1일부터 9일까지는 제외)
        val firstMonthRentalAmountForGoods = rentalAmount(contractDate, rentalStartDay, totalDaysInMonth, monthlyRentalAmountForGoods)
        val firstMonthRentalAmount = rentalAmount(contractDate, rentalStartDay, totalDaysInMonth, monthlyRentalAmount)
        val firstMonthRentalAmountForService = firstMonthRentalAmount - firstMonthRentalAmountForGoods
        // 마지막 회차는 2027년 1월 1일부터 1월 9일까지 일할 계산
        val lastMonthRentalAmount = monthlyRentalAmountForGoods - firstMonthRentalAmountForGoods

        // 마지막 이자수익 계산
        var lastRentalAmount = presentValueAmount((lastMonthRentalAmount), interestRate, (rentalStartDay-1))
        var isRentalStart = true
        if(rentalStartDay == 1){ // 시작이 1일이면 일할계산 없음.
            isRentalStart = false
            lastRentalAmount = presentValueAmount((lastMonthRentalAmount), interestRate, null)
        }
        var interestIncome = lastMonthRentalAmount - lastRentalAmount
        if(isRentalStart){
            // 마지막 회차 추가 (2027년 1월 1일부터 9일까지 일할 계산)
            schedule.add(
                RentalFinancialLeaseScheduleInfo(
                    depreciationYearMonth = lastMonths.format(DateTimeFormatter.ofPattern("yyyy-MM")),
                    depreciationBillYearMonth = lastMonths.plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM")),
                    depreciationCount = rentalPeriod + 1,
                    depreciationRentalAmount = decimalFormat.format(lastMonthRentalAmount).toBigDecimal(),
                    depreciationInterestIncome = decimalFormat.format(interestIncome).toBigDecimal()
                )
            )
        }

        // 중간 회차 계산 (마지막부터 0회차까지)
        var newAmount = lastMonthRentalAmount // 렌탈료
        var newPresentValue = lastRentalAmount
        try{
            for (cycle in reqRentalFinancialLeaseSchedule.contractPeriod!! - 1 downTo 0) {
                val yearMonth = contractDate.plusMonths(cycle.toLong()).format(DateTimeFormatter.ofPattern("yyyy-MM"))
                val billingYearMonth = contractDate.plusMonths(cycle.toLong() + 1).format(DateTimeFormatter.ofPattern("yyyy-MM"))
                var currentAmount = newAmount                               // 렌탈료
                var currentPresentValue = newPresentValue                   // 현재가치(PV) 계산
                // 현할차 계산
                var currentDifference = decimalFormat.format(currentAmount - currentPresentValue).toDouble()
                if(cycle == 0 && isRentalStart){
                    monthlyRentalAmountForGoods = firstMonthRentalAmountForGoods            // 1회차 렌탈료 자리 유지.
                }

                // NEXT 회차 데이터 정재
                newAmount += monthlyRentalAmountForGoods
                newPresentValue = presentValueAmount((monthlyRentalAmountForGoods + currentPresentValue), interestRate, null)
                if(cycle == 0){
                    val rentalEndDay = totalDaysInMonth - rentalStartDay + 1
                    newPresentValue = presentValueAmount((monthlyRentalAmountForGoods + currentPresentValue), interestRate, rentalEndDay)
                }
                val newDifference = decimalFormat.format(newAmount - newPresentValue).toDouble()
                // NEXT 회차 데이터 정재

                interestIncome = newDifference - currentDifference          // 이자수익
                schedule.add( // 스케줄에 추가
                    RentalFinancialLeaseScheduleInfo(
                        depreciationYearMonth = yearMonth,
                        depreciationBillYearMonth = billingYearMonth,
                        depreciationCount = cycle+1,
                        depreciationRentalAmount = decimalFormat.format(monthlyRentalAmountForGoods).toBigDecimal(),
                        depreciationBookValue = decimalFormat.format(currentAmount).toBigDecimal(),
                        depreciationPresentValue = decimalFormat.format(currentPresentValue).toBigDecimal(),
                        depreciationCurrentDifference = decimalFormat.format(currentDifference).toBigDecimal(),
                        depreciationInterestIncome = decimalFormat.format(interestIncome).toBigDecimal()
                    )
                )

                if(cycle == 0){ // 0 번째 회차 추가
                    currentAmount = newAmount                               // 렌탈료
                    currentPresentValue = newPresentValue                   // 현재가치(PV) 계산
                    currentDifference = currentAmount - currentPresentValue // 현할차 계산
                    schedule.add(
                        RentalFinancialLeaseScheduleInfo(
                            depreciationYearMonth = contractDate.format(DateTimeFormatter.ofPattern("yyyy-MM")),
                            depreciationBookValue = decimalFormat.format(currentAmount).toBigDecimal(),
                            depreciationPresentValue = decimalFormat.format(currentPresentValue).toBigDecimal(),
                            depreciationCurrentDifference = decimalFormat.format(currentDifference).toBigDecimal(),
                        )
                    )
                }
            }
        }catch(e:Exception){
            e.printStackTrace()
            println("error : ${e.message}")
        }

        return schedule.reversed()
    }


    private fun presentValueAmount(amount: Double, rate: Double, day: Int?): Double {
        val result = if (day == null) {
            amount / (1 + (rate / 12)) // 월간 계산
        } else {
            amount / (1 + (rate * day / 365)) // 일간 계산
        }
        return result.toBigDecimal().setScale(scale, RoundingMode.HALF_UP).toDouble()
    }

    // 일할 계산 함수 (시작일과 종료일을 바탕으로 렌탈료를 계산)
    private fun rentalAmount(startDate: LocalDate, startDay: Int, endDay: Int, monthlyRentalFee: Double): Double {
        val totalDaysInMonth = startDate.lengthOfMonth()
        val daysInPeriod = endDay - startDay + 1 // 기간 내 실제 일수

        // 계산식: (월 렌탈료 / 월의 일수) * 실제 사용 일수
        return roundAmount(BigDecimal((monthlyRentalFee / totalDaysInMonth) * daysInPeriod)).toDouble()
//        val rentalAmount = BigDecimal((monthlyRentalFee / totalDaysInMonth) * daysInPeriod).setScale(scale +1, RoundingMode.HALF_UP)
//        return rentalAmount.setScale(scale, RoundingMode.HALF_UP).toDouble()
    }

    fun rentalAmount(startDate: LocalDate, monthlyRentalFee: Double): Double {
        val endDay: Int = startDate?.lengthOfMonth()!!
        val startDay: Int = startDate?.dayOfMonth!!

        return rentalAmount(startDate, startDay, endDay, monthlyRentalFee)
    }

    fun roundAmount(rentalAmount: BigDecimal): BigDecimal {
        // 1원 처리!! 5자리 에서 반올림 후 4자리로 재처리 해야함.
        return rentalAmount.setScale(scale +1, RoundingMode.HALF_UP).setScale(scale, RoundingMode.HALF_UP)
    }

}
