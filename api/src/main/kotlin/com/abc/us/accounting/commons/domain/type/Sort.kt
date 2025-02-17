package com.abc.us.accounting.commons.domain.type

sealed class Sort {
    enum class By (val value: String, val field: String) {
        CREATE_TIME("create_time", "createTime"),
        UPDATE_TIME("update_time", "updateTime"),
        POSTING_DATE("posting_date", "postingDate"),
        DOCUMENT_DATE("document_date", "documentDate"),
        ENTRY_DATE("entry_date", "entryDate"),
        ACCOUNT_CODE("account_code", "accountKey.accountCode")
    }

    enum class Direction (val value: String) {
        ASC("asc"),
        DESC("desc"),
        ;
        fun toSortDirection(): org.springframework.data.domain.Sort.Direction {
            return when (this) {
                ASC -> org.springframework.data.domain.Sort.Direction.ASC
                DESC -> org.springframework.data.domain.Sort.Direction.DESC
            }
        }
    }
}
