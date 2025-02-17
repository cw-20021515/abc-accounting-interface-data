package com.abc.us.accounting.documents.domain.entity

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.type.ClosingStatus
import com.abc.us.accounting.supports.utils.Hashs
import com.abc.us.accounting.supports.utils.IdGenerator
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import java.time.OffsetDateTime

@Comment("회계 월 마감 정보")
@Entity
@Table(
    name = "fiscal_closing",
)
class FiscalClosing (
    @EmbeddedId
    val fiscalKey:FiscalKey,

    @Comment("마감 상태")
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    val status: ClosingStatus = ClosingStatus.OPEN,

    @Comment("사유")
    @Column(name = "reason")
    val reason:String = "",

    @Comment("생성일시")
    @Column(name = "create_time")
    val createTime: OffsetDateTime = OffsetDateTime.now(),

    @Comment("생성자")
    @Column(name = "created_by")
    val createdBy: String = Constants.APP_NAME,

    @Comment("수정일시")
    @Column(name = "update_time")
    val updateTime: OffsetDateTime = OffsetDateTime.now(),

    @Comment("수정자")
    @Column(name = "updated_by")
    val updatedBy: String = Constants.APP_NAME,

    @Version
    val version: Long = 0,
){

    companion object{
        fun of(fiscalKey: FiscalKey, userId:String = Constants.APP_NAME): FiscalClosing {
            return FiscalClosing(
                fiscalKey = fiscalKey,
                createdBy = userId,
                updatedBy = userId
            )
        }
    }

    fun isClosed(): Boolean {
        return status == ClosingStatus.CLOSED
    }
    fun isOpen(): Boolean {
        return status == ClosingStatus.OPEN
    }

    fun startClosing(userId:String): FiscalClosing {
        return withStatus(ClosingStatus.CLOSING, userId)
    }

    fun completeClosing(userId:String): FiscalClosing {
        return withStatus(ClosingStatus.CLOSED, userId)
    }

    fun reopenPeriod(userId:String, reason:String): FiscalClosing {
        return withStatus(ClosingStatus.OPEN, userId, reason)
    }


    fun withStatus(claimStatus: ClosingStatus, userId:String = Constants.APP_NAME, reason:String =""): FiscalClosing {
        val newStatus = status.transit(claimStatus)

        return FiscalClosing(
            fiscalKey = fiscalKey,
            status = newStatus,
            reason = reason,
            createTime = createTime,
            createdBy = createdBy,
            updateTime = OffsetDateTime.now(),
            updatedBy = userId,
            version = version
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FiscalClosing) return false

        if (fiscalKey != other.fiscalKey) return false
        if (status != other.status) return false
        if (reason != other.reason) return false


        return true
    }

    override fun hashCode(): Int {
        return Hashs.hash(fiscalKey, status, reason).toInt()
    }

    override fun toString(): String {
        return "FiscalClosing(fiscalKey=$fiscalKey, status=$status, reason='$reason', createTime=$createTime, createdBy='$createdBy', updateTime=$updateTime, updatedBy='$updatedBy', version=$version)"
    }

    fun toHistory(): FiscalClosingHistory {
        return FiscalClosingHistory(
            id = IdGenerator.generateNumericId(),
            fiscalKey = fiscalKey,
            status = status,
            reason = reason,
            createTime = createTime,
            createdBy = createdBy,
            updateTime = updateTime,
            updatedBy = updatedBy
        )
    }
}