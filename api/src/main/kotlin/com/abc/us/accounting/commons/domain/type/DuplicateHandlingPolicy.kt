package com.abc.us.accounting.commons.domain.type

interface DuplicateDataPolicy {
    fun shouldOverwrite(existingVersion: Long = 0L, incomingVersion: Long = 0L): Boolean
}


enum class DuplicateHandlingPolicy(val description: String) : DuplicateDataPolicy {
    PRESERVE_EXISTING("기존 데이터를 유지합니다.") {
        override fun shouldOverwrite(existingVersion: Long, incomingVersion: Long): Boolean {
            return false
        }
    },
    ALWAYS_OVERWRITE("기존 데이터를 새로운 데이터로 대체합니다.") {
        override fun shouldOverwrite(existingVersion: Long, incomingVersion: Long): Boolean {
            return true
        }
    },
    VERSION_BASED("버전을 비교하여 최신 데이터로 갱신합니다.") {
        override fun shouldOverwrite(existingVersion: Long, incomingVersion: Long): Boolean {
            return existingVersion < incomingVersion
        }
    };
}