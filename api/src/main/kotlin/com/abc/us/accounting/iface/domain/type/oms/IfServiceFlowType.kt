package com.abc.us.accounting.iface.domain.type.oms

enum class IfServiceFlowType(val displayName: String, val koreanName: String, val description: String = "") {
    INSTALL(displayName = "Install", koreanName = "설치"),
    REPLACEMENT(displayName = "Replacement", koreanName = "교환"),
    RETURN(displayName = "Return", koreanName = "해지"),
    REFUND(displayName = "Refund", koreanName = "반품"),
    REPAIR(displayName = "Repair", koreanName = "수리"),
    COURIER(displayName = "Courier", koreanName = "직배송"),
    RELOCATION_UNINSTALL(displayName = "Relocation-Uninstall", koreanName = "이사-해체"),
    RELOCATION_INSTALL(displayName = "Relocation-Install", koreanName = "이사-설치"),
    REINSTALL(displayName = "Reinstall", koreanName = "재설치");

    companion object {
        fun fromDisplayName(name: String): com.abc.us.accounting.iface.domain.type.oms.IfServiceFlowType? = values().find { it.displayName == name }
        fun fromKoreanName(name: String): com.abc.us.accounting.iface.domain.type.oms.IfServiceFlowType? = values().find { it.koreanName == name }

        // 서비스 유형 분류 도움 메서드
        fun isRelocationRelated(type: com.abc.us.accounting.iface.domain.type.oms.IfServiceFlowType) = type.name.startsWith("RELOCATION")
        fun isInstallRelated(type: com.abc.us.accounting.iface.domain.type.oms.IfServiceFlowType) =
            type == com.abc.us.accounting.iface.domain.type.oms.IfServiceFlowType.INSTALL || type == com.abc.us.accounting.iface.domain.type.oms.IfServiceFlowType.RELOCATION_INSTALL || type == com.abc.us.accounting.iface.domain.type.oms.IfServiceFlowType.REINSTALL
        fun isUninstallRelated(type: com.abc.us.accounting.iface.domain.type.oms.IfServiceFlowType) =
            type == com.abc.us.accounting.iface.domain.type.oms.IfServiceFlowType.RELOCATION_UNINSTALL || type == com.abc.us.accounting.iface.domain.type.oms.IfServiceFlowType.RETURN || type == com.abc.us.accounting.iface.domain.type.oms.IfServiceFlowType.REFUND
    }
}