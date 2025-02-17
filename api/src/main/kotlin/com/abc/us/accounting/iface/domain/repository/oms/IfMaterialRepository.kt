package com.abc.us.accounting.iface.domain.repository.oms

import com.abc.us.accounting.iface.domain.entity.oms.IfMaterial
import com.abc.us.accounting.iface.domain.type.oms.IfMaterialType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface IfMaterialRepository: JpaRepository<IfMaterial, String> {
    @Query("SELECT m from IfMaterial m WHERE m.materialId in :materialIds")
    fun findAllByMaterialIdIn(materialIds: List<String>): List<IfMaterial>

    @Query("SELECT m from IfMaterial m WHERE m.materialType in :materialTypes")
    fun findAllByMaterialTypeIn(materialTypes: List<IfMaterialType>): List<IfMaterial>
}
