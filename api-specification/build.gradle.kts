import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.spring)
    alias(libs.plugins.kotlin.plugin.allopen)
    alias(libs.plugins.kotlin.plugin.serialization)
    id("com.abc.us.plugin.common")
}

allOpen {
    annotation("org.springframework.stereotype.Controller")
    annotation("org.springframework.stereotype.Service")
    annotation("org.springframework.stereotype.Repository")
    annotation("org.springframework.boot.autoconfigure.SpringBootApplication")
    annotation("org.springframework.context.annotation.Configuration")
    annotation("org.springframework.boot.context.properties.ConfigurationProperties")

}

repositories {
    mavenCentral()
    gradlePluginPortal()
}
// common 모듈에서 사용할 라이브러리
// 다른 모듈에서 사용할 수 있기 때문에 api, testApi로 설정
dependencies {
    api(libs.kotlin.reflect)
    api(libs.commons.lang3)
    api(libs.kotlin.stdlib)

    implementation(libs.bundles.spring.boot)
    implementation(libs.swagger.annotations)
}

val openApiGenerateTask = tasks.register<GenerateTask>("openApiGenerate") {

    println("root-directory.path:${rootDir.path}")

    group = "openapi tools"
    skipValidateSpec.set(true)
    generatorName.set("kotlin-spring")
    outputDir.set("${rootDir.path}/${project.name}")
    packageName.set("com.abc.us.generated")

    inputSpec.set("${rootDir.path}/api-specification/api-specs/openapi/openapi.yaml")
    configOptions.set(
        mapOf(
            "useSpringBoot3" to "true",
            "interfaceOnly" to "true",
            "enumPropertyNaming" to "UPPERCASE",
            "useTags" to "true"
        ),
    )
    additionalProperties.set(
        mapOf(
            "removeEnumValuePrefix" to "false", // https://stackoverflow.com/questions/66589561/openapi-generator-jaxrs-spec-stop-shorterning-my-enum-values
        ),
    )

    openapiNormalizer.set(
        mapOf(
            Pair("REFACTOR_ALLOF_WITH_PROPERTIES_ONLY", "true")
//            Pair("REFACTOR_ALLOF_INLINE_SCHEMAS", "true")
        )
    )

    globalProperties.set(
        mapOf(
            Pair("apis", ""), // no value or comma-separated api names
            Pair("models", ""), // no value or comma-separated api names
        ),
    )
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}