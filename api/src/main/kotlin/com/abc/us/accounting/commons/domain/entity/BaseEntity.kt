package com.abc.us.accounting.commons.domain.entity

import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PostLoad
import jakarta.persistence.PostPersist
import jakarta.persistence.Transient
import org.springframework.data.domain.Persistable

@MappedSuperclass
abstract class BaseEntity<ID : Any>(
    @Transient
    private var _isNew: Boolean = true

): Persistable<ID> {

    override fun isNew(): Boolean = _isNew

    @PostPersist
    @PostLoad
    fun markNotNew() {
        _isNew = false
    }
}