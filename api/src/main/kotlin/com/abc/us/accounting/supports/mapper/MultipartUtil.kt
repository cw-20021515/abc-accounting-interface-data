package com.abc.us.accounting.supports.mapper

import org.springframework.util.StringUtils
import java.util.*

object MultipartUtil {
    private const val BASE_DIR = "Downloads"

    val localHomeDirectory: String
        /**
         * 로컬에서의 사용자 홈 디렉토리 경로를 반환합니다.
         */
        get() = System.getProperty("user.home")

    /**
     * 새로운 파일 고유 ID를 생성합니다.
     * @return 36자리의 UUID
     */
    fun createFileId(): String {
        return UUID.randomUUID().toString()
    }

    /**
     * Multipart 의 ContentType 값에서 / 이후 확장자만 잘라냅니다.
     * @param contentType ex) image/png
     * @return ex) png
     */
    fun getFormat(contentType: String): String? {
        if (StringUtils.hasText(contentType)) {
            return contentType.substring(contentType.lastIndexOf('/') + 1)
        }
        return null
    }

    /**
     * 파일의 전체 경로를 생성합니다.
     * @param fileId 생성된 파일 고유 ID
     * @param format 확장자
     */
    fun createPath(fileId: String?, format: String?): String {
        return String.format("%s/%s.%s", BASE_DIR, fileId, format)
    }

}