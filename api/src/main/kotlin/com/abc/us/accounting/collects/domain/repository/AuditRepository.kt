package com.abc.us.accounting.collects.domain.repository

import com.abc.us.accounting.collects.domain.entity.audit.AuditTargetEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AuditTargetRepository : JpaRepository<AuditTargetEntity, Long> {

    /**
     * 활성 상태(`isActive`가 true)인 특정 `entityName`의 `AuditTargetEntity` 목록을 조회합니다.
     *
     * @param entityName 조회할 엔터티 이름
     * @return 활성 상태의 엔터티 목록
     */
    fun findByEntityNameAndIsActiveTrue(entityName: String): List<AuditTargetEntity>
}
