plugins {
    // https://docs.gradle.org/current/userguide/kotlin_dsl.html
    // -> Avoid using internal Kotlin DSL APIs
    `kotlin-dsl` // enable the Kotlin-DSL
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven { url = uri("https://repo.spring.io/milestone") }
}

dependencies {
    implementation("io.swagger.parser.v3:swagger-parser:2.1.16")
    implementation("org.apache.commons:commons-text:1.10.0")
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    implementation(libs.bundles.spring.boot)
    implementation(libs.bundles.logging)
    implementation(libs.bundles.feign)
    implementation(libs.bundles.jackson)

    implementation(libs.openapi.generator.gradle.plugin)
}

tasks.register("printJvmArgs") {
    doLast {
        println("JVM Max Memory: ${Runtime.getRuntime().maxMemory() / (1024 * 1024)} MB")
    }
}