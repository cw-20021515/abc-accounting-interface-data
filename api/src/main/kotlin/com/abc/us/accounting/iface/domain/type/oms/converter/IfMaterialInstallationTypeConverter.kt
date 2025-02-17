package com.abc.us.accounting.iface.domain.type.oms.converter

import com.abc.us.accounting.iface.domain.type.oms.IfMaterialInstallationType
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter


@Converter(autoApply = true)
class IfMaterialInstallationTypeConverter : AttributeConverter<IfMaterialInstallationType, String> {

    override fun convertToDatabaseColumn(attribute: IfMaterialInstallationType?): String {
        return attribute?.name ?: "UNKNOWN"  // NULL이면 기본값 "UNKNOWN"
    }

    override fun convertToEntityAttribute(dbData: String?): IfMaterialInstallationType {
        return try {
            IfMaterialInstallationType.valueOf(dbData ?: "UNKNOWN") // NULL 값이 들어오면 "UNKNOWN" 사용
        } catch (e: IllegalArgumentException) {
            IfMaterialInstallationType.UNKNOWN // 잘못된 값이 들어오면 기본값 "UNKNOWN" 설정
        }
    }
}