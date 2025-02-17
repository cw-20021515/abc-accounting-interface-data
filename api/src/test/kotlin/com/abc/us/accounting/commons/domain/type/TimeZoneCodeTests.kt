package com.abc.us.accounting.commons.domain.type

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import mu.KotlinLogging
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TimeZoneCodeTests: AnnotationSpec(){
    companion object {
        private val logger =KotlinLogging.logger{}
    }

    @Test
    fun `timezone test`() {
        val seoul = TimeZoneCode.SEOUL
        val texas = TimeZoneCode.TEXAS
        val utc = TimeZoneCode.UTC

        val offsetTime = OffsetDateTime.now()
        logger.info { "offsetTime:$offsetTime" }


        seoul.getZoneId().id shouldBe "Asia/Seoul"
        seoul.getZoneOffset().id shouldBe "+09:00"
        seoul.now().offset.id shouldBe "+09:00"

        texas.getZoneId().id shouldBe "America/Chicago"
        texas.getZoneOffset().id shouldBe "-06:00"
        texas.now().offset.id shouldBe "-06:00"

        utc.getZoneId().id shouldBe "UTC"
        utc.getZoneOffset().id shouldBe "Z"
        utc.now().offset.id shouldBe "Z"

        TimeZoneCode.fromCode("Asia/Seoul") shouldBe TimeZoneCode.SEOUL
        TimeZoneCode.fromCode("America/Chicago") shouldBe TimeZoneCode.TEXAS
        TimeZoneCode.fromCode("UTC") shouldBe TimeZoneCode.UTC


        TimeZoneCode.convertTime(offsetTime, seoul) shouldBe offsetTime
        TimeZoneCode.convertTime(offsetTime, texas).toLocalTime() shouldBe offsetTime.minusHours(15).toLocalTime()
        TimeZoneCode.convertTime(offsetTime, utc).toLocalTime() shouldBe offsetTime.minusHours(9).toLocalTime()
    }

    @Test
    fun `local date test`() {
        val seoul = TimeZoneCode.SEOUL
        val texas = TimeZoneCode.TEXAS
        val utc = TimeZoneCode.UTC



        val seoulOffsetDateTime =OffsetDateTime.of(2021, 9, 1, 0, 0, 0, 0, seoul.getZoneOffset())
        val localDateTime  = seoulOffsetDateTime.toLocalDateTime()
        val localDate = localDateTime.toLocalDate()
        logger.info { "localDateTime:$localDateTime, localDate:$localDate" }
        localDateTime.toLocalDate() shouldBe localDate

        // System 시간대가 같은지 확인
        TimeZoneCode.system().getZoneId() shouldBe ZoneId.systemDefault()

        // 서울 시간을 텍사스의 시간로 변환
        val texasTime = seoul.convertTime(localDateTime, texas)
        logger.info{ "texas offset time:$texasTime" }
        TimeZoneCode.convertTime(localDateTime, seoul, texas) shouldBe texasTime

        TimeZoneCode.convertTime(seoulOffsetDateTime, texas) shouldBe texasTime

        seoul.toLocalDate(localDateTime, texas) shouldBe localDate.minusDays(1)

        run {
            val offsetDateTime = OffsetDateTime.of(localDateTime, seoul.getZoneOffset())
            logger.info { "seoul local:$localDateTime, offset:$offsetDateTime" }
        }
        run {
            val offsetDateTime = OffsetDateTime.of(localDateTime, texas.getZoneOffset())
            logger.info { "texas local:$localDateTime, offset:$offsetDateTime" }
        }

        run {
            val offsetDateTime = OffsetDateTime.of(localDateTime, utc.getZoneOffset())
            logger.info { "utc local:$localDateTime, offset:$offsetDateTime" }
        }

        run {
            val offsetDateTime = TimeZoneCode.system().now()
            logger.info { "seoul offset:$offsetDateTime" }
        }
    }

    @Test
    fun `epoch time test`() {
        val seoul = TimeZoneCode.SEOUL
        val texas = TimeZoneCode.TEXAS
        val utc = TimeZoneCode.UTC

        val epochTime = 1630454400000L
        val seoulTime = TimeZoneCode.convertTime(epochTime, seoul)
        val texasTime = TimeZoneCode.convertTime(epochTime, texas)
        val utcTime = TimeZoneCode.convertTime(epochTime, utc)

        logger.info { "epochTime:$epochTime, seoulTime:$seoulTime, texasTime:$texasTime, utcTime:$utcTime" }

        seoulTime.toEpochSecond() shouldBe epochTime / 1000
        texasTime.toEpochSecond() shouldBe epochTime / 1000
        utcTime.toEpochSecond() shouldBe epochTime / 1000

        DateTimeFormatter.ISO_OFFSET_DATE_TIME
    }

    @Test
    fun `parsing time str test`() {
        run {
            val isoTimeStr = "2024-03-25T14:30:00+09:00"
            val offsetTime = TimeZoneCode.parseISOTime(isoTimeStr, TimeZoneCode.SEOUL)
            logger.info { "isoTimeStr:$isoTimeStr, offsetTime:$offsetTime" }
            offsetTime.offset.id shouldBe "+09:00"
        }

        run {
            val isoTimeStr = "2024-03-25T14:30:00-05:00"
            val offsetTime = TimeZoneCode.parseISOTime(isoTimeStr, TimeZoneCode.TEXAS)
            logger.info { "isoTimeStr:$isoTimeStr, offsetTime:$offsetTime" }
            offsetTime.offset.id shouldBe "-05:00"
        }

        run {
            val isoTimeStr = "2024-03-25T14:30:00Z"
            val offsetTime = TimeZoneCode.parseISOTime(isoTimeStr, TimeZoneCode.UTC)
            logger.info { "isoTimeStr:$isoTimeStr, offsetTime:$offsetTime" }
            offsetTime.offset.id shouldBe "Z"
        }
    }

    @Test
    fun `timezone convert test`() {
        run {
            val fromTimeStr = "2024-11-01T00:00-06:00";
            val utc = TimeZoneCode.parseISOTime(fromTimeStr, TimeZoneCode.UTC)
            logger.info("utc:$utc, by fromTimeStr:$fromTimeStr")
            utc shouldBe TimeZoneCode.parseISOTime("2024-11-01T06:00Z")
        }

    }
}