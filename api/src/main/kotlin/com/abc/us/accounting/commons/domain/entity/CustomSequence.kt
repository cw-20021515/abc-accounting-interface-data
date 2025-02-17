package com.abc.us.accounting.commons.domain.entity

import com.abc.us.accounting.supports.utils.buildToString
import jakarta.persistence.*

@Entity
@Table(name = "custom_sequence")
class CustomSequence private constructor(
    @Id
    @Column(name = "sequence_name")
    val name: String,

    @Column(name = "current_value", nullable = false)
    val currentValue:Long,

    @Version
    val version: Long = 0,
){
    companion object {
        fun create(name: String, initialValue: Long = 0, version:Long = 0) = CustomSequence(
            name = name,
            currentValue = initialValue,
            version = version
        )
    }

    // 변경이 필요할 때는 새로운 인스턴스를 생성
    fun next(): CustomSequence {
        return CustomSequence(
            name = this.name,
            currentValue = this.currentValue + 1,
            version = this.version  // 자동으로 버전을 증가시킴
        )
    }

    override fun toString(): String {
        return buildToString {
            add(
                "name" to name,
                "currentValue" to currentValue,
                "version" to version
            )
        }
    }
}
