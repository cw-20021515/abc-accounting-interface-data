package com.abc.us.accounting.qbo.domain.entity

import com.abc.us.accounting.collects.domain.entity.node.EntityNode
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Transient
import org.hibernate.annotations.Comment
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.type.YesNoConverter
import java.time.OffsetDateTime

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
data class QboCredential (

    @Id
    @Comment("회사 고유 식별자")
    val realmId: String,

    @Comment("사업장 ID")
    val companyCode: String,

    @Comment("application active profile")
    val activeProfile: String,

    @Comment("credential 식별 이름")
    val targetName: String,

    @Comment("oauth2 인증 client id")
    val clientId: String,

    @Comment("oauth2 인증 secret key")
    val clientSecret: String,

    @Comment("권한 범위")
    val scope: String,

    @Comment("액세스 토큰")
    @Column(columnDefinition = "TEXT")
    var accessToken: String? = null,

    @Comment("리프레시 토큰")
    @Column(columnDefinition = "TEXT")
    var refreshToken: String? = null,

    @Comment("unknown")
    @Column(columnDefinition = "TEXT")
    var idToken: String? = null,

    @Comment("토큰 유형")
    var tokenType: String? = null,

    @Comment("액세스 토큰 생성 일시")
    var accessTokenIssuedTime: OffsetDateTime? = null,

    @Comment("액세스 토큰 만료 일시")
    var accessTokenExpireTime: OffsetDateTime? = null,

    @Comment("리프레시 토큰 생성 일시")
    var refreshTokenIssuedTime: OffsetDateTime? = null,

    @Comment("리프레시 토큰 만료 일시")
    var refreshTokenExpireTime: OffsetDateTime? = null,

    @Comment("BASE64 code")
    @Column(columnDefinition = "TEXT")
    var basicToken: String? = null,

    @Comment("sub")
    var sub: String? = null,

    @Comment("givenName")
    var givenName: String? = null,

    @Comment("email")
    var email: String? = null,

    @Comment("생성 일시")
    @CreationTimestamp
    var createTime: OffsetDateTime? = null,

    @Comment("업데이트 일시")
    var updateTime: OffsetDateTime? = null,

    @Comment("Active 여부")
    @Convert(converter = YesNoConverter::class)
    var isActive: Boolean = true,
) {
    companion object    {
        fun of(other:QboCredential): QboCredential {
            return QboCredential(
                realmId = other.realmId,
                companyCode = other.companyCode,
                activeProfile = other.activeProfile,
                targetName = other.targetName,
                clientId = other.clientId,
                clientSecret = other.clientSecret,
                scope = other.scope,
                accessToken = other.accessToken,
                refreshToken = other.refreshToken,
                idToken = other.idToken,
                tokenType = other.tokenType,
                accessTokenIssuedTime = other.accessTokenIssuedTime,
                accessTokenExpireTime = other.accessTokenExpireTime,
                refreshTokenIssuedTime = other.refreshTokenIssuedTime,
                refreshTokenExpireTime = other.refreshTokenExpireTime,
                basicToken = other.basicToken,
                sub = other.sub,
                givenName = other.givenName,
                email = other.email,
                createTime = other.createTime,
                updateTime = other.updateTime,
                isActive = other.isActive
            )
        }
    }
}