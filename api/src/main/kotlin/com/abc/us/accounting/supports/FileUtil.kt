package com.abc.us.accounting.supports

import com.abc.us.accounting.supports.excel.ExcelHeader
import com.abc.us.accounting.supports.mapper.MapperUtil
import com.abc.us.accounting.supports.mapper.MultipartUtil
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.StreamUtils
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor


@Component
object FileUtil {

    private val logger = KotlinLogging.logger {}

    val localHomeDirectory: String get() = System.getProperty("user.home")

    class Detail {
        var id: String? = null
        var name: String? = null
        var format: String? = null
        var path: String? = null
        var bytes: Long = 0

        val createdAt: LocalDateTime = LocalDateTime.now()

        companion object {
            fun multipartOf(multipartFile: MultipartFile): Detail {
                val fileId: String = MultipartUtil.createFileId()
                val format: String = MultipartUtil.getFormat(multipartFile.contentType!!)!!
                val fileDetail = Detail()

                fileDetail.id = fileId
                fileDetail.name = multipartFile.originalFilename
                fileDetail.format = format
                fileDetail.path = MultipartUtil.createPath(fileId, format)
                fileDetail.bytes = multipartFile.size
                return fileDetail
            }

            fun partOf(fileName: String, fileSize: Long): Detail {
                val fileId: String = MultipartUtil.createFileId()
                val format: String = MultipartUtil.getFormat(MediaType.APPLICATION_JSON_VALUE)!!
                val fileDetail = Detail()

                fileDetail.id = fileId
                fileDetail.name = fileName
                fileDetail.format = format
                fileDetail.path = MultipartUtil.createPath(fileId, format)
                fileDetail.bytes = fileSize
                return fileDetail
            }
        }
    }


    /**
     * 파일의 확장자를 체크하여 허용 확장자일 경우 true를 리턴한다.
     */
    @Synchronized
    fun fileValidCheck(files: Array<MultipartFile?>?, validExtensions: List<String>): Boolean {
        // 파일이 null이 아니고, 각 파일의 확장자가 유효한 경우를 찾습니다.
        return files?.any { file ->
            file?.originalFilename?.let { filename ->
                validExtensions.contains(filename.substringAfterLast('.', ""))
            } == true
        } ?: false
    }

    /**
     * 파일의 확장자를 체크하여 허용 확장자일 경우 true를 리턴한다.
     */
    @Synchronized
    fun fileValidCheck(file: MultipartFile?, validExtensions: List<String>): Boolean {
        // 파일이 null이 아니고, 파일 크기가 0보다 크며, 유효한 확장자인지 확인합니다.
        return file?.takeIf { it.size > 0 }?.originalFilename?.let { filename ->
            val extension = filename.substringAfterLast('.', "")
            validExtensions.contains(extension)
        } ?: false
    }

    // 파일 사이즈 체크
    private fun fileSizeCheck(validExtensions: List<String>, extension: String?, fileUploadSize: Long): Boolean {
        // 확장자가 null이 아니고, 유효한 확장자 목록에 포함되며, 파일 크기가 500MB 미만인지 확인
        return extension?.let {
            validExtensions.any { validExt ->
                it.equals(validExt, ignoreCase = true)
            } && fileUploadSize < 500 * 1024 * 1024
        } ?: false
    }

    @Synchronized
    fun mkdirs(filePath: String): File {
        return File(filePath).apply {
            if (!exists()) mkdirs()
        }
    }

    @Throws(IOException::class)
    fun createTempFile(key: String, multipartFile: MultipartFile?): File? {
        if (multipartFile == null) {
            // multipartFile이 null인 경우 null 반환
            return null
        }
        var currentDate = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        // 임시 파일 생성
        val tempFile = File.createTempFile("$currentDate-$key-", ".tmp")

        // MultipartFile의 내용을 임시 파일에 쓰기
        multipartFile.inputStream.use { inputStream ->
            Files.copy(inputStream, tempFile.toPath())
        }

        return tempFile.apply {
            if (!exists()) mkdirs()
        }
    }

    fun lastIndexOf(str: String, ch: Char): Int {
        return str.indexOfLast { it == ch }
    }


    @Throws(IOException::class)
    fun createDownloadFiles(response: HttpServletResponse, resourcePath: String, originFileName: String) {
        val resource: Resource = FileSystemResource(resourcePath)
        if (resource.exists() && resource.isReadable) {
            val file: Path = Paths.get(resourcePath)
            var contentType: String = Files.probeContentType(file)
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE
            }

            response.apply {
                contentType = contentType
                setHeader(
                    HttpHeaders.CONTENT_DISPOSITION,
                    ContentDisposition.attachment()
                        .filename(originFileName, StandardCharsets.UTF_8)
                        .build()
                        .toString()
                )
            }

            // 파일을 응답으로 전송
            StreamUtils.copy(resource.inputStream, response.outputStream)
        } else {
            response.status = HttpServletResponse.SC_NOT_FOUND
        }
    }

    fun createDownloadZipFiles(
        response: HttpServletResponse,
        files: List<Pair<Resource, String>>,
        originFileName: String,
    ) {
        // 응답의 콘텐츠 타입과 헤더 설정
        response.contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE
        response.setHeader(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment()
                .filename("$originFileName.zip", StandardCharsets.UTF_8)
                .build()
                .toString()
        )

        // 중복 파일 이름을 처리하기 위한 세트
        val seenNames = mutableSetOf<String>()

        // StreamingResponseBody를 사용하여 ZIP 파일을 스트리밍으로 작성
        val responseBody = StreamingResponseBody { outputStream ->
            ZipOutputStream(outputStream).use { zipOutputStream ->
                files.forEach { (file, originalName) ->
                    // 파일 이름과 확장자를 분리
                    val (fileName, extension) = originalName.split(".", limit = 2).let {
                        if (it.size == 2) it else listOf(it[0], "")
                    }

                    // 중복 방지를 위한 새로운 파일 이름 생성
                    var entryName = originalName
                    var count = 1
                    while (seenNames.contains(entryName)) {
                        entryName = "${fileName}($count).${extension}"
                        count++
                    }
                    seenNames.add(entryName)

                    // ZIP 엔트리에 파일 추가
                    file.inputStream.use { inputStream ->
                        zipOutputStream.putNextEntry(ZipEntry(entryName))
                        inputStream.copyTo(zipOutputStream)
                        zipOutputStream.closeEntry()
                    }
                }
            }
        }

        // 응답의 출력 스트림에 ZIP 파일 내용을 작성
        response.outputStream.use { outputStream ->
            responseBody.writeTo(outputStream)
        }
    }

    @Synchronized
    fun fileUploadLocal(
        resourcePath: String,
        modifiedFilename: String,
        fileInfo: MultipartFile?,
    ): Boolean {
        // 원본 파일 이름을 정리하고 MIME 타입을 추출
        val originFileName: String = StringUtils.cleanPath(fileInfo?.originalFilename ?: "")
        val mimeType: String = originFileName.substringAfterLast('.', "")

        // 수정된 파일 이름과 MIME 타입을 로깅
        logger.info("originFileName: $originFileName, mimeType: $mimeType, modifiedFilename: $modifiedFilename")

        // 파일 경로 설정 및 디렉토리 생성
        val filePath: Path = Paths.get(resourcePath + modifiedFilename)
        return try {
            mkdirs(resourcePath)
            Files.copy(fileInfo?.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)
            true
        } catch (exc: IOException) {
            logger.error("File upload failed", exc)
            false
        }
    }

    fun getFieldNames(excludedProperties: Set<String>, clazz: Class<*>): List<String> {
        // 클래스의 주 생성자 파라미터에서 프로퍼티 이름을 가져옵니다
        val propertyNames = clazz.kotlin.primaryConstructor?.parameters?.mapNotNull {
            val schemaAnnotation = it.findAnnotation<Schema>()
            println(
                "getFieldNames fieldName : ${it.name}, desccription : ${schemaAnnotation?.description}, schemaAnnotation : ${
                    MapperUtil.logMapCheck(
                        schemaAnnotation
                    )
                }"
            )
//            schemaAnnotation?.description ?: it.name
            it.name
        } ?: emptyList()
        // 제외할 프로퍼티를 필터링하여 결과를 반환합니다
        return propertyNames.filterNot { it in excludedProperties }
    }

    fun getMergeFieldNames(
        excludedProperties: Set<String>,
        requestMergeList: List<ExcelHeader>, // ExcelHeader 객체 리스트로 변경
        clazz: Class<*>,
    ): List<ExcelHeader> {
        val propertyNames = clazz.kotlin.primaryConstructor?.parameters?.mapNotNull {
            val schemaAnnotation = it.findAnnotation<Schema>()
            schemaAnnotation?.description ?: it.name
        } ?: emptyList()

        // 제외할 프로퍼티를 필터링합니다
        val filteredProperties = propertyNames.filterNot { it in excludedProperties }

        // ExcelHeader 객체 리스트를 생성합니다
        val headers = mutableListOf<ExcelHeader>()

        filteredProperties.forEachIndexed { index, property ->
            // requestMergeList에서 해당 프로퍼티와 일치하는 ExcelHeader를 찾습니다
            val mergeHeader = requestMergeList.find { it.column?.contains(property) == true }
            val cellRangeAddress = mergeHeader?.merge ?: null // 기본값 설정
            val mergeLength = mergeHeader?.mergeLength ?: 0 // 기본값 설정
            val mergeRowIndex = mergeHeader?.mergeRowIndex ?: 0 // 기본값 설정

            // ExcelHeader 객체 추가
            headers.add(
                ExcelHeader(
                    column = property,
                    merge = cellRangeAddress,
                    mergeLength = mergeLength,
                    mergeRowIndex = mergeRowIndex,
                )
            )
            if (mergeLength > 1) { // 병합 대상 컬럼 칸수를 공백으로 처리
                repeat(mergeLength - 1) {
                    headers.add(ExcelHeader(column = ""))
                }
            }
        }

        return headers
    }


    // 파일 이름 생성 로직
    fun getFileName(fileName: String, fromDate: OffsetDateTime? = null, toDate: OffsetDateTime? = null): String {
        val reqFromDate = fromDate?.toString() ?: ""
        val reqToDate = toDate?.toString() ?: ""

        val fileName = when {
            reqFromDate.isNotEmpty() && reqToDate.isNotEmpty() -> {
                "${fileName}_$reqFromDate~$reqToDate"
            }

            reqFromDate.isNotEmpty() -> {
                "${fileName}_$reqFromDate"
            }

            reqToDate.isNotEmpty() -> {
                "${fileName}_$reqToDate"
            }

            else -> {
                "$fileName"
            }
        }
        return fileName
    }

}