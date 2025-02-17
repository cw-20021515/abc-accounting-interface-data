package com.abc.us.accounting.supports.mapper

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode

class CustomDoubleSerializer : JsonSerializer<Double>() {
    @Throws(IOException::class)
    override fun serialize(value: Double, gen: JsonGenerator, serializers: SerializerProvider) {
        val d = BigDecimal(value)
        gen.writeNumber(d.setScale(2, RoundingMode.HALF_UP).toPlainString())
    }
}
