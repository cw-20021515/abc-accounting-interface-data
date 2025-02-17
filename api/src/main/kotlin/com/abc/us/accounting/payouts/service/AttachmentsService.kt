 package com.abc.us.accounting.payouts.service

import com.abc.us.accounting.payouts.domain.entity.PayoutAttachment
import com.abc.us.accounting.payouts.domain.repository.AttachmentsRepository
import com.abc.us.accounting.payouts.model.response.ResAttachmentInfo
import com.abc.us.accounting.supports.aws.AwsS3Util
import com.abc.us.accounting.supports.FileUtil
import com.abc.us.accounting.supports.mapper.MapperUtil
import jakarta.servlet.http.HttpServletResponse
import org.hibernate.query.sqm.tree.SqmNode.log
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter


 @Service
class AttachmentsService(
     private var attachmentsRepository: AttachmentsRepository,
     private var awsS3Util: AwsS3Util,
) {

    @Value("\${project.accounting.temp-file-path}")
    private var uploadDir: String? = null

    @Value("\${spring.profiles.active}")
    private var profilesActive: String? = null

    fun selectAttachmentsById(attachmentsId: String?, response: HttpServletResponse) {
        // 특정 ID와 삭제 여부가 false인 항목을 조회
        val attachmentsInfo = attachmentsRepository.findByIdAndIsDeleted(attachmentsId.toString(), false)

        if(attachmentsInfo?.isPresent == true){
            // 항목이 존재하고 resourcePath가 null이 아닌 경우
            attachmentsInfo?.let { attachments ->
                if (attachments != null && attachments.isPresent) {
                    attachments.get().let { info ->
                        val fullPath = "${info.resourcePath}${attachments.get()?.modifiedFileName}"
                        log.info("attachmentsId : $attachmentsId, resourcePath : $fullPath")
                        if (profilesActive?.contains("local") == false) {
                            awsS3Util.downloadFile("${attachments.get()?.resourcePath}", response)
                        } else {
                            info.originFileName?.let { FileUtil.createDownloadFiles(response, fullPath,it ) }

                        }
                    }
                }
            }
        }else{
            throw HttpMessageNotReadableException("Payout info not found for attachmentsId : $attachmentsId")
        }

    }

    fun deleteAttachments(id: String): Boolean {
        // 특정 ID와 삭제 여부가 false인 항목을 조회
        val attachmentsInfo = attachmentsRepository.findByIdAndIsDeleted(id, false)?.orElse(null)
        if (attachmentsInfo != null) {
            // AWS S3에서 파일 삭제
            var isFileDeleted = true
            if (profilesActive?.contains("local") == false) {
                isFileDeleted = awsS3Util.deleteFile("${attachmentsInfo.resourcePath}")
            }

            if (isFileDeleted) {
                // 파일 삭제가 성공하면 항목의 isDeleted를 true로 설정
                attachmentsInfo.isDeleted = true
                attachmentsRepository.save(attachmentsInfo)
            }
            return isFileDeleted
        }
        return false
    }

    /**
     * txId 데이터는 payoutId로 설정
     */
    fun fileUpload(fileInfo: MultipartFile?, payoutsId: String): ResAttachmentInfo? {
        // 파일명 중복을 피하기 위해 고유한 파일명 생성
        val originFileName: String = StringUtils.cleanPath(fileInfo?.originalFilename!!)
        val resourceSize: Long = fileInfo.size
        val mimeType: String = originFileName.substring(FileUtil.lastIndexOf(originFileName, '.') + 1)
        val modifiedFilename = System.currentTimeMillis().toString() + "." + mimeType
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        var resourcePath = (uploadDir + today) + "/"
        var attachment = PayoutAttachment()
        var createDatetime = OffsetDateTime.now()
        attachment.originFileName = originFileName
        attachment.modifiedFileName = modifiedFilename
        attachment.txId = payoutsId
        attachment.mimeType = mimeType
        attachment.createTime = createDatetime
        attachment.expireTime = createDatetime.plusYears(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
        attachment.isDeleted = false
        attachment.remark = null
        attachment.resourceSize = resourceSize
        attachment.resourcePath = resourcePath

        // 증거를 저장하고 저장된 엔티티를 가져옵니다
        val payoutAttachmentInfo: PayoutAttachment = attachmentsRepository.save(attachment)
        println("attachment : ${MapperUtil.logMapCheck(payoutAttachmentInfo)}")
        log.info("\noriginFileName : $originFileName, resourceSize : $resourceSize, mimeType : $mimeType, modifiedFilename: $modifiedFilename")
        var isS3PathSave = true
        var s3Path = "attachments/$today/${payoutAttachmentInfo.txId}-${payoutAttachmentInfo.modifiedFileName}"
//        var isFileUpload = awsS3Util.storageFileUpload("$resourcePath","$s3Path", "$modifiedFilename", fileInfo)
        var isFileUpload = if (profilesActive?.contains("local") == false) {
            awsS3Util.storageFileUpload("$resourcePath", "$s3Path", "$modifiedFilename", fileInfo)
        } else {
            isS3PathSave = false
            FileUtil.fileUploadLocal(resourcePath, modifiedFilename, fileInfo)
        }

        if (isFileUpload) {
            if (isS3PathSave) {
                attachment.resourcePath = s3Path
            }
            val attachmentInfoResponse = attachmentsRepository.save(attachment)
            val resAttachmentInfo = ResAttachmentInfo(
                attachmentId = attachmentInfoResponse.id,
                originFileName = attachmentInfoResponse.originFileName,
                modifiedFileName = attachmentInfoResponse.modifiedFileName,
                resourcePath = attachmentInfoResponse.resourcePath,
                resourceSize = attachmentInfoResponse.resourceSize,
                mimeType = attachmentInfoResponse.mimeType,
                createDatetime = attachmentInfoResponse.createTime,
                expireDatetime = attachmentInfoResponse.expireTime,
                remark = attachmentInfoResponse.remark
            )
            return resAttachmentInfo
        } else {
            payoutAttachmentInfo.isDeleted = true // 파일 업로드 오류 업데이트
            var payoutAttachmentInfo: PayoutAttachment = attachmentsRepository.save(attachment)
            throw IOException("Upload Error id : ${payoutAttachmentInfo.id}")
        }
        return null
    }

//    fun selectAttachmentsByTxId(txId:String?, response: HttpServletResponse) : List<Pair<Resource, String>>{
//        // 특정 트랜잭션 ID와 삭제 여부가 false인 항목들을 조회
//        val attachmentsList = attachmentsRepository.findByTxIdAndIsDeleted(txId.toString(), false)
//
//        // 파일 리소스를 저장할 리스트
//        val fileResourceList: MutableList<Pair<Resource, String>> = mutableListOf()
//        if (attachmentsList != null) {
//            // 항목이 존재하는 경우
//            if (attachmentsList.isNotEmpty()) {
//                for (attachment in attachmentsList) {
//                    // resourcePath가 null이 아닌 경우
//                    attachment?.resourcePath?.let { resourcePath ->
//                        // 전체 파일 경로 생성
//                        val fullPath = "$resourcePath${attachment.modifiedFileName}"
//                        log.info("fullPath : $fullPath")
//
//                        // Resource 객체 생성
//                        val resource: Resource = FileSystemResource(fullPath)
//
//                        // 파일 리소스를 리스트에 추가
//                        fileResourceList.add(resource to attachment.originFileName)
//                    }
//                }
//            } else {
//                // 항목이 존재하지 않을 경우 예외 발생
//                throw Exception("File with the specified txId does not exist.")
//            }
//        }
//        return fileResourceList
//    }
}