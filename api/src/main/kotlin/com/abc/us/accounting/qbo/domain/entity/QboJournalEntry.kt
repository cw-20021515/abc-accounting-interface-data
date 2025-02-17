package com.abc.us.accounting.qbo.domain.entity

import com.abc.us.accounting.qbo.domain.entity.key.QboJournalEntryKey
import com.fasterxml.jackson.annotation.JsonInclude
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.annotations.Type
import org.hibernate.type.YesNoConverter
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime


@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
data class QboJournalEntry (
    @Id
    @EmbeddedId
    var key: QboJournalEntryKey,

    @Comment("전표 중복체크로 사용")
    @Column(name = "doc_hash")
    val docHash: String,

    var roundingDifference : BigDecimal = BigDecimal.ZERO,

    var syncToken : String? = null,

    @Type(JsonType::class)
    @Column(name = "submit_result")
    val submitResult: String,

    @Comment("전기일")
    var postingDate: LocalDate,

    @Comment("생성 시간")
    var createTime: OffsetDateTime? = null,
    @Comment("갱신 시간")
    var updateTime: OffsetDateTime? = null,
    @Comment("사용 여부")
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true
) {

}