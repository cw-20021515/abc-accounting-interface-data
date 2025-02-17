package com.abc.us.accounting.supports.aws

import com.abc.us.accounting.supports.FileUtil
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.util.StreamUtils
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.ResponseBytes
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.core.exception.SdkException
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import java.nio.charset.StandardCharsets
import java.nio.file.Paths

/**
 * bucket : abc-us-billing-dev
 *
 */
@Component
class AwsS3Util(
    @Value("\${cloud.aws.s3.bucket:abc-us-accounting-dev}")
    private val bucket: String,
    @Qualifier("s3Client")
    private val s3Client: S3Client,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Synchronized
    @Throws(Throwable::class)
    fun storageFileUpload(
        fullPath: String,
        key: String,
        modifiedFilename: String,
        multipartFile: MultipartFile?,
    ): Boolean {
        // MultipartFile을 임시 파일로 변환
//        var key : String = modifiedFilename
        println("파일 업로드 Key 값 확인 : $modifiedFilename, fullPath : $fullPath")
        val mkdirsFile = FileUtil.mkdirs(fullPath)
//        val tempFile: File? = createTempFile(key, multipartFile)
        if (mkdirsFile == null) {
            println("MultipartFile is null or empty")
            return false
        }
        var isFileUpload = true
        val tempFile = FileSystemResource("$fullPath$modifiedFilename").file
        try {
            println("tempFile1 : $tempFile")
            multipartFile?.transferTo(tempFile)
            val putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key("$key")
                .contentType(multipartFile?.contentType ?: "application/octet-stream") // 기본 MIME 타입 지정
                .build()
            println("putObjectRequest : $putObjectRequest")
            val putObjectResponse: PutObjectResponse? =
                s3Client.putObject(putObjectRequest, RequestBody.fromFile(tempFile))
            println("putObjectResponse : ${putObjectResponse?.sdkHttpResponse()?.isSuccessful}")
            println("putObjectResponse : ${putObjectResponse.toString()}")
            if (putObjectResponse?.sdkHttpResponse()?.isSuccessful == false) {
                isFileUpload = false
            }

        } catch (e: SdkClientException) {
            isFileUpload = false
            e.printStackTrace()
        } finally {
            if (tempFile?.exists() == true) {
                // 임시 파일 삭제
                tempFile.delete()
            }
        }
        return isFileUpload
    }

    // 파일 업로드 메서드
    fun uploadFile(key: String, filePath: String) {
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key("$key")
            .build()

        val file = Paths.get(filePath)
        s3Client.putObject(putObjectRequest, RequestBody.fromFile(file))
        println("File uploaded to $bucket/$key")
    }

    // 파일 읽기 메서드
    fun readFile(key: String): String {
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key("$key")
            .build()

        s3Client.getObject(getObjectRequest).use { s3Object ->
            val content = s3Object.readAllBytes().toString(Charsets.UTF_8)
            println("File content: $content")
            return content
        }
    }

    fun downloadFile(key: String?, response: HttpServletResponse) {
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key("$key")
            .build()

        return try {
//            val getObjectResponse: ResponseInputStream<GetObjectResponse> = s3Client.getObject(getObjectRequest)
            val getObjectResponse: ResponseBytes<GetObjectResponse> = s3Client.getObjectAsBytes(getObjectRequest)
            val statusCode = getObjectResponse.response().sdkHttpResponse().statusCode()

            if (statusCode == 200) {
                // 파일 다운로드를 위한 콘텐츠 타입 및 헤더를 설정
                response.contentType = getObjectResponse.response().contentType() ?: "application/octet-stream"
                response.setHeader(
                    HttpHeaders.CONTENT_DISPOSITION,
                    ContentDisposition.attachment()
                        .filename(key, StandardCharsets.UTF_8)
                        .build()
                        .toString()
                )

                // 파일 내용을 응답 출력
//                StreamUtils.copy(getObjectResponse, response.outputStream)
                StreamUtils.copy(getObjectResponse.asByteArray(), response.outputStream)
                response.flushBuffer()
            } else {
                // 파일 검색이 성공하지 않았을 때의 처리를 합니다.
                response.status = HttpStatus.NOT_FOUND.value()
                response.writer.write("File not found or could not be retrieved.") // 파일을 찾을 수 없거나 검색할 수 없습니다.
            }
        } catch (e: SdkException) {
            // 예외를 로깅하거나 오류를 처리합니다.
            response.status = HttpStatus.INTERNAL_SERVER_ERROR.value()
            response.writer.write("Failed to get object from key: $key") // 객체 호출 실패
            e.printStackTrace()
        }
    }

    // 파일 삭제 메서드
    fun deleteFile(key: String): Boolean {
        val deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket(bucket)
            .key("attachments/$key")
            .build()

        try {
            val deleteObjectResponse: DeleteObjectResponse = s3Client.deleteObject(deleteObjectRequest)
            println("File deleted from key : $key")
            println("deleteObjectResponse : ${deleteObjectResponse?.sdkHttpResponse()?.isSuccessful}")
            if (deleteObjectResponse?.sdkHttpResponse()?.isSuccessful == true) {
                return true
            }
        } catch (e: SdkClientException) {
            logger.error("deleteFile Error : ${e.message}")
        }
        return false
    }
}