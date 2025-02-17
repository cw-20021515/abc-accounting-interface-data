package com.abc.us.accounting.supports.mapper

import com.abc.us.accounting.supports.mapper.enums.OffsetDateTimeField
import com.google.gson.*
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class AdapterLocalDate(private val fieldType: String?) : JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

    private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    override fun serialize(src: LocalDate?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.format(formatter))
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LocalDate {
        val jsonObject = json?.asJsonObject
        for (fieldName in OffsetDateTimeField.values()) {
            val field = if (fieldType?.equals("date", ignoreCase = true) == true) {
                fieldName.date
            } else {
                fieldName.time
            }
            println("field : $field")
            if (jsonObject?.has(field) == true) {
                return LocalDate.parse(jsonObject.get(field).asString, formatter)
            }
        }
        throw JsonParseException("No valid date or time field found")
    }
}
