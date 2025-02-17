package com.abc.us.accounting.collects.domain.entity.audit

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import java.time.LocalDateTime
import java.time.OffsetDateTime


@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
class AuditEntityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public var id: Long? = null

    @Column(name = "action_type", nullable = false)
    public var actionType: String? = null

    @Column(name = "entity_id", nullable = false)
    public var entityId: String? = null

    @Column(name = "timestamp", nullable = false)
    public var timestamp: OffsetDateTime? = null

    @Column(name = "event_table_name", nullable = false)
    public var eventTableName: String? = null

    @Column(name = "event_table_id", nullable = false)
    public var eventTableId: String? = null

    @Column(name = "company_id", nullable = false)
    public var companyId : String? = null

    @Column(name = "processed", nullable = false)
    public var processed = false
}