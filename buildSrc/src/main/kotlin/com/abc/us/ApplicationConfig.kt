//package com.abc.us
//
//import com.fasterxml.jackson.annotation.JsonProperty
//import com.fasterxml.jackson.databind.DeserializationFeature
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
//import com.fasterxml.jackson.module.kotlin.registerKotlinModule
//import java.io.File
//
//data class YamlConfig(
//    @JsonProperty("abc-app")
//    val application: AppConfig,
//)
//
//data class AppConfig(
//    @JsonProperty("github")
//    val github: GithubConfig,
//    @JsonProperty("api-filter-prefix")
//    val apiFilters: List<String>,
//    @JsonProperty("model-filter-prefix")
//    val modelFilters: List<String>,
//    @JsonProperty("common-model-names")
//    val commonModelNames: List<String>
//)
//
//data class GithubConfig(
//    val path: String,
//    var token: String,
//    val commit: String,
//    val version: String
//)
//
//object ApplicationConfig {
//    fun loadYmlConfig(path: String): YamlConfig {
//        val objectMapper =
//            ObjectMapper(YAMLFactory()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//                .registerKotlinModule()
//        val yamlFile = File(path)
//        return objectMapper.readValue(yamlFile, YamlConfig::class.java)
//    }
//}
