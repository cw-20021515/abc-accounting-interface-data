package com.abc.us.oms.domain.common.config

import com.abc.us.accounting.supports.entity.toEntityId
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PostLoad
import jakarta.persistence.PostPersist
import org.hibernate.proxy.HibernateProxy
import org.springframework.data.domain.Persistable
import java.io.Serializable
import java.util.*

@MappedSuperclass
abstract class AbstractEntity :
    Persistable<String>,
    Serializable {
    @Id
    private val id: String = ""//toEntityId()//SnowflakeId.generateId().toBase62()

    @JsonIgnore
    @jakarta.persistence.Transient
    private var isNew = true

    override fun getId(): String = id

    override fun isNew(): Boolean = isNew

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }

        if (other !is HibernateProxy && this::class != other::class) {
            return false
        }

        return id == getIdentifier(other)
    }

    private fun getIdentifier(obj: Any): Any = if (obj is HibernateProxy) {
        obj.hibernateLazyInitializer.identifier
    } else {
        (obj as AbstractEntity).id
    }

    override fun hashCode() = Objects.hashCode(id)

    @PostPersist
    @PostLoad
    protected fun load() {
        isNew = false
    }
}
