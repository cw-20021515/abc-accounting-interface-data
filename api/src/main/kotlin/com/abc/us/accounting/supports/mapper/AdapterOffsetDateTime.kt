package com.abc.us.accounting.supports.mapper

import com.abc.us.accounting.supports.mapper.enums.OffsetDateTimeField
import com.google.gson.*
import java.lang.reflect.Type
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter


class AdapterOffsetDateTime(private val fieldType: String?) : JsonSerializer<OffsetDateTime>, JsonDeserializer<OffsetDateTime> {

    private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    override fun serialize(src: OffsetDateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.format(formatter))
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): OffsetDateTime {
        val jsonObject = json?.asJsonObject
        for (fieldName in OffsetDateTimeField.values()) {
            val field = if (fieldType?.equals("date", ignoreCase = true) == true) {
                fieldName.date
            } else {
                fieldName.time
            }
            println("field : $field")
            if (jsonObject?.has(field) == true) {
                return OffsetDateTime.parse(jsonObject.get(field).asString, formatter)
            }
        }
        throw JsonParseException("No valid date or time field found")
    }
}
