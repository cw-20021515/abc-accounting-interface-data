package com.abc.us.accounting.documents.domain.entity

import com.abc.us.accounting.documents.domain.type.*
import jakarta.persistence.*
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.hibernate.annotations.Comment
import org.hibernate.type.YesNoConverter
import java.time.OffsetDateTime


@Entity
@Table (
    name = "account",
)
class Account (

    @Id
    @EmbeddedId
    val accountKey: AccountKey,

    @Comment("계정의 이름")
    @Column(name="name", nullable = false)
    val name: String,

    @Comment("계정의 이름 설명")
    @Column(name="description", nullable = false)
    val description: String,

    @Comment("계정 유형")
    @Convert(converter = AccountTypeNameConverter::class)
    @Column(name="account_type", nullable = false)
    val accountType: AccountType,

    @Comment("계정의 유형 (예: 자산, 부채, 수익, 비용 등)")
    @Convert(converter = AccountClassNameConverter::class)
    @Column(name="account_class", nullable = false)
    val accountClass: AccountClass,

    @Comment("미결관리 여부")
    @Convert(converter = YesNoConverter::class)
    @Column(name="is_open_item_mgmt", nullable = false)
    val isOpenItemMgmt: Boolean,

    @Comment("Active 여부에 활용")
    @Convert(converter = YesNoConverter::class)
    @Column(name="is_active", nullable = false)
    val isActive: Boolean,

    @Comment("계정의 시스템 소스 유형")
    @Column(name="system_source", nullable = false, columnDefinition = "VARCHAR(10)")
    @Enumerated(EnumType.STRING)
    val systemSource: SystemSourceType,

    @Comment("퀵북 부모계정코드")
    @Column(name="qbo_parent_account_code")
    val qboParentAccountCode: String? = null,

    @Comment("QBO Account Type")
    @Convert(converter = QBOAccountTypeConverter::class)
    @Column(name="qbo_account_type")
    val qboAccountType: QBOAccountType? = null,

    @Comment("QBO Account SubType")
    @Convert(converter = QboAccountSubTypeConverter::class)
    @Column(name="qbo_account_subtype")
    val qboAccountSubType: QBOAccountSubType? = null,


    @Comment("연결계정코드")
    @Column(name="consolidation_account_code")
    val consolidationAccountCode: String? = null,

    @Comment("생성 일시")
    var createTime: OffsetDateTime = OffsetDateTime.now(),

    @Comment("수정 일시")
    var updateTime: OffsetDateTime? = OffsetDateTime.now()
){

    @PrePersist
    @PreUpdate
    fun prePersist() {
        updateTime = OffsetDateTime.now()
    }

    fun getNaturalAccountSide(): AccountSide {
        return accountClass.natualAccountSide
    }

    override fun toString(): String {
        return  this.javaClass.simpleName+ "{" +
                ", companyCode='" + accountKey.companyCode + '\'' +
                ", accountCode='" + accountKey.accountCode + '\'' +
                ", accountName='" + name + '\'' +
                ", accountDescription='" + description + '\'' +
                ", accountType=" + accountType + '\'' +
                ", accountClass=" + accountClass + '\'' +
                ", consolidationAccountCode='" + consolidationAccountCode + '\'' +
                ", openItemMgmt=" + isOpenItemMgmt + '\'' +
                ", isActive=" + isActive + '\'' +
                ", systemSource=" + systemSource + '\'' +
                ", qboAccountType=" + qboAccountType + '\'' +
                ", qboAccountSubType=" + qboAccountSubType + '\'' +
                ", createTime=" + createTime + '\'' +
                ", updateTime=" + updateTime + '\'' +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is Account) return false

        return EqualsBuilder()
            .append(accountKey, other.accountKey)
            .append(name, other.name)
            .append(accountType, other.accountType)
            .append(accountClass, other.accountClass)
            .append(isOpenItemMgmt, other.isOpenItemMgmt)
            .append(qboAccountType, other.qboAccountType)
            .append(qboAccountSubType, other.qboAccountSubType)
            .append(systemSource, other.systemSource)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(17, 37)
            .append(accountKey)
            .append(name)
            .append(accountType)
            .append(accountClass)
            .append(isOpenItemMgmt)
            .append(qboAccountType)
            .append(qboAccountSubType)
            .append(systemSource)
            .toHashCode()
    }

    fun decisionOpenItemStatus():OpenItemStatus {
        if (isOpenItemMgmt) {
            return OpenItemStatus.OPEN
        } else {
            return OpenItemStatus.NONE
        }
    }
}