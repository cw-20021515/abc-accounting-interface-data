package com.abc.us.plugin

//import com.abc.us.ApplicationConfig
//import com.abc.us.task.ApiSync
//import com.abc.us.task.Hello

//import io.swagger.v3.oas.models.media.ComposedSchema
//import io.swagger.v3.oas.models.media.Schema
//import io.swagger.v3.parser.OpenAPIV3Parser
//import io.swagger.v3.parser.core.models.ParseOptions
//import org.gradle.accessors.dm.LibrariesForLibs
//import org.gradle.kotlin.dsl.*
//import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

//val libs = the<LibrariesForLibs>()

//plugins {
//    java
//}

//dependencies {
//    implementation(libs.bundles.spring.boot)
//    implementation(libs.spring.tx)
//    implementation(platform(libs.spring.cloud.dependencies))
//    implementation(platform(libs.spring.security.bom))
//    implementation(libs.bundles.logging)
//    implementation(libs.bundles.utils)
//    implementation(libs.bundles.feign)
//    implementation(libs.bundles.jackson)
//    implementation(libs.swagger.annotations)
//    testImplementation(platform(libs.junit.bom))
//    testImplementation("org.junit.jupiter:junit-jupiter")
//    testImplementation(libs.spring.boot.starter.test)
//}

//val helloTask = tasks.register("hello", Hello::class.java) {
//    username.set("plugin")
//}

//tasks.register<ApiSync>("apiSync") {
//    description = "download api spec"
//    configFile.set(layout.projectDirectory.file("${rootDir.path}/common/src/main/resources/application-common.yml"))
//    outputDir.set(layout.projectDirectory.dir("${rootDir.path}/common/src/main/api-specification"))
//    githubAccessToken.set(System.getenv()["GITHUB_TOKEN"])
//}

//// A project extension
//interface ApiFilterExtension {
//    // A configurabled
//    abstract val apiFilter: ListProperty<String>
//    abstract val modelFilter: ListProperty<String>
//}

//val apiFilter = project.extensions.create<ApiFilterExtension>("filter")
//
//apiFilter.apply {
//    val openApiFile = File("${rootDir.path}/common/main/src/api-specification/openapi/openapi.yaml")
//
//    if (project.name != "common" && openApiFile.exists()) {
//        val configFilePath = "${rootDir.path}/common/src/main/resources/application-common.yml"
//        val yamlConfig = ApplicationConfig.loadYmlConfig(configFilePath)
//        val apiFilters = yamlConfig.application.apiFilters
//        val modelFilters = yamlConfig.application.modelFilters
//        val commonModelNames = yamlConfig.application.commonModelNames
//        val parseOptions = ParseOptions()
//        parseOptions.isResolve = true
//        parseOptions.isResolveFully = true
//        parseOptions.isFlatten = true
//        val openAPI = OpenAPIV3Parser().read(openApiFile.absolutePath, null, parseOptions)
//
////        openAPI.paths.filter {path ->
////            val pathKey = path.key
////            val filtered = apiFilters.find {filter ->
//////                println("tag name:${tag.name}")
////                pathKey.contains(filter)
////            }
////            filtered != null
////        }
//
//        //val filteredTags = openAPI.tags.filter { tag ->
//        //    val filtered = apiFilters.find { filter ->
////     //          println("tag name:${tag.name}")
//        //        tag.name.contains(filter)
//        //    }
//        //    filtered != null
//        //}.map {
//        //    val splittedApiName = it.name.split("/")
//        //    val role = CaseUtils.toCamelCase(splittedApiName[0], true, '-')
//        //    val group = CaseUtils.toCamelCase(splittedApiName[1], true, '-')
//        //    "$role$group"
//        //}
//
//        val filteredModelNames = openAPI.components.schemas.filter { schema ->
//            val filtered = modelFilters.find { filter ->
//                schema.key.lowercase().startsWith(filter)
//            }
//            filtered != null
//        }.map {
//            it.key
//        }.filter { !it.contains("_") }
//
//        val allOfDataSuffix = filteredModelNames.map { it.plus("_allOf_data") }
//
//        println("filteredModelNames:${filteredModelNames}")
//
//        println("commonModelNames:${commonModelNames}")
//        //apiFilter.addAll(filteredTags)
//        modelFilter.addAll(filteredModelNames)
//        modelFilter.addAll(allOfDataSuffix)
//        modelFilter.addAll(commonModelNames)
//    }
//}

//fun printModel(schema: Schema<*>) {
//    when (schema) {
//        is ComposedSchema -> {
//            schema.allOf.firstOrNull()?.let { printModel(it) }
//        }
//
//        else -> {
//            println(schema?.name ?: schema?.javaClass?.simpleName)
//        }
//    }
//}

//val openApiGenerateTask = tasks.register<GenerateTask>("openApiGenerate") {
//    val tags = apiFilter.apiFilter.get()
//    val models = apiFilter.modelFilter.get()
//    val api: String = tags.joinToString { it }
//    val model: String = models.joinToString { it }
//
//    println("api:$api")
//    println("model:$model")
//    println("root-directory.path:${rootDir.path}")
//
//    group = "openapi tools"
//    skipValidateSpec.set(true)
//    generatorName.set("kotlin-spring")
//    outputDir.set("${rootDir.path}/${project.name}")
//    packageName.set("com.abc.us.generated")
//
//    inputSpec.set("${rootDir.path}/common/src/main/api-specification/openapi/openapi.yaml")
//    configOptions.set(
//        mapOf(
//            "useSpringBoot3" to "true",
//            "interfaceOnly" to "true",
//            "enumPropertyNaming" to "UPPERCASE",
//            "useTags" to "true"
//        ),
//    )
//    additionalProperties.set(
//        mapOf(
//            "removeEnumValuePrefix" to "false", // https://stackoverflow.com/questions/66589561/openapi-generator-jaxrs-spec-stop-shorterning-my-enum-values
//
//        ),
//    )
//
//    openapiNormalizer.set(
//        mapOf(
//            Pair("REFACTOR_ALLOF_WITH_PROPERTIES_ONLY", "true")
////            Pair("REFACTOR_ALLOF_INLINE_SCHEMAS", "true")
//        )
//    )
//
//    globalProperties.set(
//        mapOf(
//            Pair("apis", api.replace(" ", "")), // no value or comma-separated api names
//            Pair("models", ""), // no value or comma-separated api names
////            Pair("models", model.replace(" ","")), // no value or comma-separated api names
//        ),
//    )
//
//}

//tasks.test {
//    useJUnitPlatform()
//}

