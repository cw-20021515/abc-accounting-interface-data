package com.abc.us.accounting.documents.domain.entity

import com.abc.us.accounting.config.Constants
import com.abc.us.accounting.documents.domain.type.SystemSourceType
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.type.YesNoConverter
import java.time.OffsetDateTime

@Entity
@Comment("연결계정")
@Table(name = "consolidation_account")
class ConsolidationAccount(
    @Id
    @Comment("연결계정코드")
    @Column(name="code", nullable = false)
    val code: String,


    @Comment("연결계정 레벨")
    @Column(name="level", nullable = false)
    val level:Int,

    @Comment("부모 연결계정코드")
    @Column(name="parent_code", nullable = true)
    val parentCode:String? = null,

    @Comment("연결계정명(한글)")
    @Column(name="name", nullable = false)
    val name: String,

    @Comment("연결계정명(영어)")
    @Column(name="eng_name", nullable = false)
    val engName:String,

    @Comment("연결계정 설명")
    @Column(name="description", nullable = true)
    val description: String?=null,

    @Comment("기표가능여부")
    @Convert(converter = YesNoConverter::class)
    @Column(name="is_postable", nullable = false)
    val isPostable:Boolean,

    @Comment("활성화 여부")
    @Convert(converter = YesNoConverter::class)
    @Column(name="is_active", nullable = false)
    val isActive:Boolean,

    @Comment("시스템 소스")
    @Column(name="system_source", nullable = false)
    @Enumerated(EnumType.STRING)
    val systemSource: SystemSourceType,

    @Comment("생성 일시")
    @Column(name="create_time", nullable = false)
    val createTime: OffsetDateTime = OffsetDateTime.now(),

    @Comment("생성자")
    @Column(name="created_by", nullable = false)
    val createdBy:String = Constants.APP_NAME,

    @Comment("수정 일시")
    @Column(name="update_time", nullable = false)
    val updateTime: OffsetDateTime = OffsetDateTime.now(),

    @Comment("수정자")
    @Column(name="updated_by", nullable = false)
    val updatedBy:String = Constants.APP_NAME
)
