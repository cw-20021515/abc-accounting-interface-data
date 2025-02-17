package com.abc.us.accounting.documents.domain.entity

import com.abc.us.accounting.documents.domain.type.ClosingStatus
import com.abc.us.accounting.supports.utils.Hashs
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import java.time.OffsetDateTime

@Comment("회계 월 마감 히스토리 정보")
@Entity
@Table(
    name = "fiscal_closing_history",
)
class FiscalClosingHistory (
    @Comment("ID")
    @Id
    val id: Long,                           // 자동으로 생성되는 값 (IdGenerator 이용)

    @Embedded
    val fiscalKey: FiscalKey,

    @Comment("마감 상태")
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    val status: ClosingStatus,

    @Comment("재오픈 사유")
    @Column(name = "reason")
    val reason:String,

    @Comment("생성일시")
    @Column(name = "create_time")
    val createTime: OffsetDateTime = OffsetDateTime.now(),

    @Comment("생성자")
    @Column(name = "created_by")
    val createdBy: String,

    @Comment("수정일시")
    @Column(name = "update_time")
    val updateTime: OffsetDateTime = OffsetDateTime.now(),

    @Comment("생성자")
    @Column(name = "updated_by")
    val updatedBy: String,
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        other as FiscalClosingHistory

        if (fiscalKey != other.fiscalKey) return false
        if (status != other.status) return false
        if (reason != other.reason) return false
        if (createTime != other.createTime) return false
        if (createdBy != other.createdBy) return false
        if (updateTime != other.updateTime) return false
        if (updatedBy != other.updatedBy) return false

        return true;
    }

    override fun hashCode(): Int {
        return Hashs.hash(fiscalKey, status, reason, createTime, createdBy, updateTime, updatedBy).toInt()
    }

    override fun toString(): String {
        return "FiscalClosingHistory(id=$id, fiscalKey=$fiscalKey, status=$status, reason=$reason, createTime=$createTime, createdBy=$createdBy, updateTime=$updateTime, updatedBy=$updatedBy)"
    }
}