//package com.abc.us.task
//
//import com.abc.us.ApplicationConfig
//import com.abc.us.GithubContent
//import mu.KotlinLogging
//import org.gradle.api.DefaultTask
//import org.gradle.api.file.DirectoryProperty
//import org.gradle.api.file.RegularFileProperty
//import org.gradle.api.provider.Property
//import org.gradle.api.tasks.Input
//import org.gradle.api.tasks.InputFile
//import org.gradle.api.tasks.OutputDirectory
//import org.gradle.api.tasks.TaskAction
//
//abstract class ApiSync : DefaultTask() {
//    @get:Input
//    abstract val githubAccessToken: Property<String>
//
//    @get:InputFile
//    abstract val configFile: RegularFileProperty
//
//    // The directory to write source files to
//    @get:OutputDirectory
//    abstract val outputDir: DirectoryProperty
//
//    companion object {
//        private val logger = KotlinLogging.logger {}
//    }
//
//    @TaskAction
//    fun sync() {
//        logger.info("config file: ${configFile.get().asFile}")
////      설정 정보:  application.yml
//        val configFile = configFile.get().toString()
//        val yamlConfig = ApplicationConfig.loadYmlConfig(configFile)
//        val githubConfig = yamlConfig.application.github
//        githubConfig.token = githubAccessToken.get()
//        val githubContent = GithubContent(yamlConfig)
//        githubContent.download(githubConfig.version, githubConfig.commit, githubConfig.token, outputDir.get().asFile)
//    }
//}
