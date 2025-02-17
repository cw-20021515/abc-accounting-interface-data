package com.abc.us.accounting.payouts.domain.entity

import com.abc.us.accounting.configs.CustomTsidSupplier
import com.fasterxml.jackson.annotation.JsonInclude
import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Comment
import java.time.OffsetDateTime

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
class AccountsPayableItem {
    @Id
    @Tsid(CustomTsidSupplier::class)
    @Comment("고유 식별자")
    var id: String? = null

    @Comment("계정 코드")
    var accountCode: String? = null

    @Comment("계정 이름")
    var accountName: String? = null

    @Comment("지급 총액(세금포함)")
    var amount: Double? = null

    @Comment("예산 사용처")
    var budgetAllocation: String? = null

    @Comment("예산 부서 금액")
    var budgetAmount: Double? = null

    @Comment("예산 사용 시간")
    var budgetUsageTime: OffsetDateTime? = null

    @Comment("예산 코스트 센터")
    var costCenter: String? = null

    @Comment("미지급금 항목의 상세 설명")
    var description: String? = null

    @Comment("거래라인 아이템번호")
    var lineNumber: String? = null

    @Comment("자재 카테고리 코드")
    var materialCategoryCode: String? = null

    @Comment("자재 ID")
    var materialId: String? = null

    @Comment("자재 이름")
    var materialName: String? = null

    @Comment("자재 유형")
    var materialType: String? = null

    @Comment("미지급금 항목의 이름")
    var name: String? = null

    @Comment("거래처 지급유형")
    var payoutCaseType: String? = null

    @Comment("차/대변 구분자(차변:D(Debit),대변:C(Credit)")
    var postingKey: String? = null

    @Comment("수량")
    var quantity: Int? = null

    @Comment("비고")
    var remark: String? = null

    @Comment("지급 세액")
    var tax: Double? = null

    @Comment("트랜잭션 ID")
    var txId: String? = null

    @Comment("품목의 단위(PIECE (개), BOX(박스)")
    var unitMeasure: String? = null

    @Comment("단가")
    var unitPrice: Double? = null
}
