package com.abc.us.accounting.supports.converter

import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*


fun OffsetDateTime.toISO(): String = this.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

fun LocalDateTime.toUTCLocalDate() : LocalDate = this.atZone(ZoneId.of("UTC")).toLocalDate()
fun LocalDateTime.toYearMonth(): YearMonth = YearMonth.of(this.year, this.month)
fun LocalDateTime?.toOffset(offset : ZoneOffset = ZoneOffset.UTC): OffsetDateTime = this?.atOffset(offset) ?: OffsetDateTime.now(offset)

fun String.toYearMonthFromISO(): YearMonth =YearMonth.from(LocalDateTime.parse(this, DateTimeFormatter.ISO_DATE_TIME))
fun String.toLocalDateFromISO(): LocalDate = OffsetDateTime.parse(this, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate()

fun LocalDate.toOffsetAt(velocity: LocalTime, offset: ZoneOffset = ZoneOffset.UTC): OffsetDateTime {
    val localDateTime = LocalDateTime.of(this, velocity)
    return OffsetDateTime.of(localDateTime, offset)
}

fun Long.toOffsetFromEpoch(offset: ZoneOffset = ZoneOffset.UTC): OffsetDateTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(this), offset)
fun Long.toISOFromEpoch(offset: ZoneOffset = ZoneOffset.UTC): String = this.toOffsetFromEpoch(offset).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
fun Long.toLocalDateFromEpoch(zoneId: ZoneId = ZoneId.of("UTC")): LocalDate = Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()
