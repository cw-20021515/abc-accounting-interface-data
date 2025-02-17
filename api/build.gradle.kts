import org.gradle.accessors.dm.LibrariesForLibs
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask


plugins {
    application
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.spring)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.jvm.convert)

//    alias(libs.plugins.kotlin.kapt)

    id("com.abc.us.plugin.common")
    id("io.kotest") version "0.4.11"
    id("com.google.cloud.tools.jib") version "3.4.1"
    kotlin("plugin.jpa") version "1.9.22"
//    kotlin("plugin.noarg") version "2.0.10"
    `java-test-fixtures`
}
repositories {
    mavenCentral()
    // AWS code artifact 사용을 위한 설정 추가 (com.abc.us.sdk:abc-sdk-authorizer-spring-boot-starter 로드에 필요)
    maven {
        url = uri("https://abc-us-712487249036.d.codeartifact.us-west-2.amazonaws.com/maven/abc-us-integration/")
        credentials {
            username = "aws"
            password = file("${System.getenv("HOME")}/.aws/codeartifact/abc-us").readText()
        }
    }
    gradlePluginPortal()
}

apply(plugin = "kotlin-kapt")

val libs = the<LibrariesForLibs>()

dependencies {
    api(libs.kotlin.reflect)

    implementation(project(":api-specification"))


    implementation(libs.spring.tx)
    implementation(platform(libs.spring.cloud.dependencies))
    implementation(platform(libs.spring.security.bom))
    implementation(libs.bundles.logging)
    implementation(libs.bundles.utils)
    implementation(libs.bundles.feign)
    implementation(libs.bundles.jackson)
    implementation(libs.swagger.annotations)
    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(libs.spring.boot.starter.test)


    implementation(libs.bundles.spring.boot)
    implementation(libs.bundles.spring.thymeleaf)
    testImplementation(libs.bundles.spring.test)
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.bundles.mockk)
    testImplementation(libs.bundles.testcontainers)


    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
    //implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    // spring retry 의존성 추가
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    // spring security oauth2 resource server 의존성 추가
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("com.github.f4b6a3:tsid-creator:5.2.5")
    implementation("org.hashids:hashids:1.0.3")
    implementation("commons-codec:commons-codec:1.17.1")

    // hibernate jsonb 타입 지원
//    implementation("com.vladmihalcea:hibernate-types-60:2.21.1")

    implementation("org.liquibase:liquibase-core")
    implementation("org.postgresql:postgresql")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    testFixturesImplementation("org.jetbrains.kotlin:kotlin-stdlib")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-data-jpa")

    testFixturesImplementation("com.github.javafaker:javafaker:1.0.2") { exclude(module = "snakeyaml") }
    testImplementation(testFixtures(project(":api")))

    testFixturesImplementation("io.mockk:mockk:1.13.5")
    testFixturesImplementation("com.ninja-squad:springmockk:4.0.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.springframework.security:spring-security-config")
    implementation("org.springframework.security:spring-security-core")
    implementation("com.google.code.gson:gson:2.10.1")
    // caffeine 의존성 추가
    implementation("com.github.ben-manes.caffeine:caffeine:3.0.5")

    // ABC SDK 인증 모듈 의존성 추가
    implementation("com.abc.us.sdk:abc-sdk-authorizer-spring-boot-starter:2024.10.002")

    // Quickbook Java SDK에 필요한 라이브러리
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("oauth.signpost:signpost-core:1.2.1.1")
    implementation("oauth.signpost:signpost-commonshttp4:1.2")
    implementation("com.sun.mail:javax.mail:1.6.2")
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    implementation("org.glassfish.jaxb:jaxb-runtime:3.0.1")
    implementation("commons-configuration:commons-configuration:1.6")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:3.0.1")
    implementation("org.jvnet.jaxb2_commons:jaxb2-basics-runtime:1.11.1")
    implementation("org.apache.ant:ant:1.10.11")
    implementation("joda-time:joda-time:2.12.5")

    // Quickbook Java SDK에서 jackson 관련 버전이 안맞으면 parsing 에러 발생됨
    implementation("com.fasterxml.jackson.core:jackson-core:2.17.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.1")
    implementation("com.fasterxml.jackson.module:jackson-module-jaxb-annotations:2.17.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.1")

    implementation(files("libs/ipp-v3-java-data-6.4.1.jar"))
    implementation(files("libs/ipp-v3-java-devkit-6.4.1.jar"))
    implementation(files("libs/oauth2-platform-api-6.4.1.jar"))

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.github.openfeign:feign-okhttp")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")
    implementation("net.logstash.logback:logstash-logback-encoder:7.3")

    // amazon resource storage
    implementation(platform("software.amazon.awssdk:bom:2.25.59"))
    implementation("software.amazon.awssdk:s3")
    implementation("software.amazon.awssdk:sts")
    implementation("software.amazon.awssdk:sso")
    implementation("software.amazon.awssdk:ssooidc")
    implementation("com.google.code.gson:gson:2.10.1")

    // fileupload
    implementation("commons-io:commons-io:2.18.0")
    implementation("commons-fileupload:commons-fileupload:1.4")
    implementation("org.apache.poi:poi:5.0.0")
    implementation("org.apache.poi:poi-ooxml:5.0.0")


//    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
//    kapt("com.querydsl:querydsl-apt:5.0.0:jakarta")
    //implementation("com.abc.us.commonlib:common-lib:2023.10.001")

    implementation("com.opencsv:opencsv:5.7.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Test> {
    jvmArgs("-Xmx8192m", "-XX:+HeapDumpOnOutOfMemoryError", "-XX:HeapDumpPath=heapdump.hprof")
}
jib {
    from {
        image = "public.ecr.aws/amazoncorretto/amazoncorretto:21.0.5-al2023-headless"
        platforms {
            platform {
                architecture = "arm64"
                os = "linux"
            }
        }
    }
    container {
        this
            .jvmFlags =
            listOf(
                "-XX:InitialRAMPercentage=50.0",
                "-XX:MaxRAMPercentage=75.0",
                "-XX:FlightRecorderOptions=stackdepth=256",
                "-Djava.net.preferIPv4Stack=true",
                "-Duser.language=en",
                "-Duser.timezone=UTC",
                "-Dnetworkaddress.cache.ttl=30",
            )
        ports = listOf("9090")
    }
}
