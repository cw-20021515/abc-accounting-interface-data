package com.abc.us.accounting.configs

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.time.Instant

class InstantTypeAdapter : TypeAdapter<Instant>() {
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: Instant) {
        out.value(value.toString()) // 또는 필요한 형식으로 변환
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): Instant {
        return Instant.parse(`in`.nextString()) // 또는 필요한 변환
    }
}