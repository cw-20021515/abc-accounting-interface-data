package com.abc.us.accounting.supports.aws

import com.abc.us.accounting.supports.FileUtil.localHomeDirectory
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File

@Component
class AmazonS3ResourceStorage(
    private val s3Client: S3Client,
    @Value("\${cloud.aws.s3.bucket:abc-us-accounting-dev}")
    private val bucket: String
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }


    @Throws(Throwable::class)
    fun store(fullPath: String, multipartFile: MultipartFile) {
        val file: File = File(localHomeDirectory, fullPath)
        try {
            multipartFile.transferTo(file)

            val putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(multipartFile.originalFilename!!.substring(1))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .build()
            s3Client.putObject(putObjectRequest, RequestBody.fromFile(file))

        } catch (e: Exception) {
            throw e
        } finally {
            if (file.exists()) {
                file.delete()
            }
        }
    }

}
