package com.abc.us.accounting.commons.domain.type

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.apache.tools.ant.taskdefs.Local
import org.intellij.lang.annotations.Pattern
import java.time.*
import java.time.format.DateTimeFormatter

/**
 * 내부적으로 TimeZone 관리를 위한 코드가 포함됨
 */
enum class TimeZoneCode(
    val code: String,
    val displayName: String,
    val offset: String,
    val region: String
) {
    // Asia
    SEOUL("Asia/Seoul", "Seoul", "+09:00", "Asia"),
    TOKYO("Asia/Tokyo", "Tokyo", "+09:00", "Asia"),
    SINGAPORE("Asia/Singapore", "Singapore", "+08:00", "Asia"),
    HONG_KONG("Asia/Hong_Kong", "Hong Kong", "+08:00", "Asia"),
    SHANGHAI("Asia/Shanghai", "Shanghai", "+08:00", "Asia"),
    BANGKOK("Asia/Bangkok", "Bangkok", "+07:00", "Asia"),

    // North America
    TEXAS("America/Chicago", "Dallas", "-06:00", "Americas"),
    NEW_YORK("America/New_York", "New York", "-05:00", "Americas"),
    LOS_ANGELES("America/Los_Angeles", "Los Angeles", "-08:00", "Americas"),
    CHICAGO("America/Chicago", "Chicago", "-06:00", "Americas"),
    TORONTO("America/Toronto", "Toronto", "-05:00", "Americas"),

    // Europe
    LONDON("Europe/London", "London", "+00:00", "Europe"),
    PARIS("Europe/Paris", "Paris", "+01:00", "Europe"),
    BERLIN("Europe/Berlin", "Berlin", "+01:00", "Europe"),
    ZURICH("Europe/Zurich", "Zurich", "+01:00", "Europe"),

    // Oceania
    SYDNEY("Australia/Sydney", "Sydney", "+10:00", "Oceania"),
    MELBOURNE("Australia/Melbourne", "Melbourne", "+10:00", "Oceania"),

    // Default UTC
    UTC("UTC", "Coordinated Universal Time", "+00:00", "UTC");

    fun getZoneId(): ZoneId = ZoneId.of(code)

    fun getZoneOffset(): ZoneOffset = ZoneOffset.of(offset)

    /**
     * 해당 TimeZone의 현재시간
     */
    fun now(): OffsetDateTime {
        return OffsetDateTime.now(getZoneId())
    }

    /**
     * 해당 TimeZone의 현재일자
     */
    fun localDate():LocalDate {
        return now().toLocalDate()
    }

    fun convertTime(localDateTime: LocalDateTime, toZone: TimeZoneCode): OffsetDateTime {
        return convertTime(localDateTime, this, toZone)
    }

    fun toLocalDate(localDateTime: LocalDateTime, toZone: TimeZoneCode): LocalDate {
        return convertTime(localDateTime, toZone).toLocalDate()
    }

    companion object {
        private val DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        fun fromCode(code: String): TimeZoneCode {
            return entries.find { it.code == code }
                ?: throw IllegalArgumentException("Invalid timezone code: $code")
        }

        fun fromRegion(region: String): List<TimeZoneCode> {
            return entries.filter { it.region == region }
        }

        fun system(): TimeZoneCode {
            return fromCode(ZoneId.systemDefault().id)
        }

        fun parseISOTime(isoString: String): OffsetDateTime {
            return OffsetDateTime.parse(isoString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        }


        fun parseISOTime(isoString: String, toZone: TimeZoneCode): OffsetDateTime {
            return OffsetDateTime.parse(isoString, DateTimeFormatter.ISO_OFFSET_DATE_TIME).let { time ->
                convertTime(time, toZone)
            }
        }

        fun parseISOTimeToLocalDate(isoString: String): LocalDate {
            return parseISOTime(isoString).toLocalDate()
        }

        fun parseISOTimeToYearMonth(isoString: String): YearMonth {
            return parseISOTime(isoString).let { localDate ->
                YearMonth.of(localDate.year, localDate.month)
            }
        }

        fun parseISOTimeToIsoString(isoString: String): String {
            return parseISOTime(isoString).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        }

        fun convertTime(
            epochTime: Long,
            toZone: TimeZoneCode
        ): OffsetDateTime {
            Instant.ofEpochMilli(epochTime).let { instant ->
                return instant
                    .atOffset(toZone.getZoneOffset())
            }
        }

        fun convertTime(
            offsetDateTime: OffsetDateTime,
            toZone: TimeZoneCode
        ): OffsetDateTime {
            return offsetDateTime
                .toZonedDateTime()
                .withZoneSameInstant(toZone.getZoneId())
                .toOffsetDateTime()
        }

        fun convertTime(
            dateTime: LocalDateTime,
            fromZone: TimeZoneCode,
            toZone: TimeZoneCode
        ): OffsetDateTime {
            return dateTime
                .atZone(fromZone.getZoneId())
                .withZoneSameInstant(toZone.getZoneId())
                .toOffsetDateTime()
        }

        fun toLocalDate(offsetDateTime: OffsetDateTime, toZone: TimeZoneCode): LocalDate {
            return convertTime(offsetDateTime, toZone).toLocalDate()
        }

        fun toLocalDate(localDateTime: LocalDateTime, fromZone: TimeZoneCode, toZone: TimeZoneCode): LocalDate {
            return convertTime(localDateTime, fromZone, toZone).toLocalDate()
        }
    }

}

// JPA Converter
@Converter(autoApply = true)
class TimeZoneCodeConverter : AttributeConverter<TimeZoneCode, String> {
    override fun convertToDatabaseColumn(attribute: TimeZoneCode?): String? {
        return attribute?.code
    }

    override fun convertToEntityAttribute(dbData: String?): TimeZoneCode? {
        return dbData?.let { TimeZoneCode.fromCode(it) }
    }
}
