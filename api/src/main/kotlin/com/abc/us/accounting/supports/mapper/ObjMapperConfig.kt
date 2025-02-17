package com.abc.us.accounting.supports.mapper

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.text.SimpleDateFormat

@Configuration
class ObjMapperConfig {

//    @Autowired
//    fun setUpObjectMapper(mapper: ObjectMapper) {
//        mapper.dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
//    }

    @Bean
    fun objectMapper(): ObjectMapper {
        val mapper = Jackson2ObjectMapperBuilder.json().build<ObjectMapper>()

        val module = SimpleModule()
        module.addSerializer(Double::class.java, CustomDoubleSerializer())
        mapper.registerModules(module, JavaTimeModule())
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//        mapper.registerModule(JavaTimeModule().addDeserializer(OffsetDateTime::class.java, OffsetDateTimeDeserializer()))
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)  // 키 정렬 해제
        mapper.dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        return mapper
    }

//    @Bean
//    fun objectMapper(): ObjectMapper {
//        val mapper = Jackson2ObjectMapperBuilder.json().build<ObjectMapper>()
//        val module = SimpleModule()
//        module.addSerializer(Double::class.java, CustomDoubleSerializer())
//        mapper.registerModules(module, JavaTimeModule())
//        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//        return mapper
//    }
}