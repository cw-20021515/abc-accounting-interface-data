//package com.abc.us
//
//import feign.*
//import feign.jackson.JacksonDecoder
//import feign.jackson.JacksonEncoder
//import mu.KotlinLogging
//import java.io.*
//import java.nio.file.*
//import java.util.zip.ZipEntry
//import java.util.zip.ZipInputStream
//
//
//@Headers(
//    value = [
//        "Accept: application/vnd.github+json"
//    ]
//)
//interface GithubApi {
//    @RequestLine("GET /repos/abc-us/abc-api-specs/zipball/{commit-number}")
//    @Headers(
//        value =
//        [
//            "Authorization: Bearer {access-token}",
//            "X-GitHub-Api-Version: {api-version}"
//        ]
//    )
//    fun download(
//        @Param("api-version") apiVersion: String,
//        @Param("commit-number") commitNumber: String,
//        @Param("access-token") token: String
//    ): Response
//}
//
//internal class GithubContent(yamlConfig: YamlConfig) {
//    var githubPath: String
//    var githubToken: String
//    var feignClient: GithubApi
//
//    init {
//        githubPath = yamlConfig.application.github.path
//        githubToken = yamlConfig.application.github.token
//        feignClient = Feign.Builder().encoder(JacksonEncoder()).decoder(JacksonDecoder())
//            .target(GithubApi::class.java, githubPath)
//    }
//
//    companion object {
//        private val logger = KotlinLogging.logger {}
//    }
//
//    fun download(apiVersion: String, commitNumber: String, token: String, destinationDir: File) {
//        runCatching {
//            val response = feignClient.download(apiVersion, commitNumber, token)
//            if (response.status() == 200) {
//                val buffer = ByteArray(1024)
//                val zis = ZipInputStream(response.body().asInputStream())
//                var zipEntry = zis.getNextEntry()
//                while (zipEntry != null) {
//                    if (zipEntry.name.contains("openapi")) {
//                        val newFile = createNewFile(destinationDir, zipEntry)
//                        if (zipEntry.isDirectory) {
//                            if (!newFile.isDirectory() && !newFile.mkdirs()) {
//                                throw IOException("Failed to create directory $newFile")
//                            }
//                        } else {
//                            val parent = newFile.getParentFile()
//                            if (!parent.isDirectory() && !parent.mkdirs()) {
//                                throw IOException("Failed to create directory $parent")
//                            }
//                            val fos = FileOutputStream(newFile)
//                            var len: Int
//                            while (zis.read(buffer).also { len = it } > 0) {
//                                fos.write(buffer, 0, len)
//                            }
//                            fos.close()
//                        }
//                    }
//                    zipEntry = zis.getNextEntry()
//                }
//                zis.closeEntry()
//                zis.close()
//            } else {
//                throw IOException("Failed to synchronize api")
//            }
//        }.onFailure {
//            logger.error("Failed to synchronize api[commitNumber:${commitNumber}]", it)
//        }
//    }
//    fun extractAfterFirstSlash(filePath: String): String {
//        val firstSlashIndex = filePath.indexOf("/")
//        // If a slash is found, return the substring after the first slash
//        return if (firstSlashIndex != -1) {
//            filePath.substring(firstSlashIndex + 1) // +1 to exclude the slash itself
//        } else {
//            ""
//        }
//    }
//
//    @Throws(IOException::class)
//    fun createNewFile(destinationDir: File, zipEntry: ZipEntry): File {
//        val destFile = File(destinationDir, extractAfterFirstSlash(zipEntry.name))
//        val destDirPath = destinationDir.getCanonicalPath()
//        val destFilePath = destFile.getCanonicalPath()
//        if (!destFilePath.startsWith(destDirPath + File.separator)) {
//            throw IOException("Entry is outside of the target dir: " + zipEntry.name)
//        }
//        return destFile
//    }
//}
