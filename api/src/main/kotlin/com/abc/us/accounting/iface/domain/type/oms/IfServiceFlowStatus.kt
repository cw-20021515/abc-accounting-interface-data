package com.abc.us.accounting.iface.domain.type.oms

enum class IfServiceFlowStatus(val displayName: String, val description: String = "") {
    SERVICE_CREATED(displayName = "서비스플로우 생성 완료", description = "서비스플로우 생성 완료"),
    SHIPPING_SCHEDULED(displayName = "출고요청 완료", description = "물류시스템에 출고요청에 대한 요청 및 응답이 완료된 상태"),
    BOOKING_SCHEDULED(displayName = "예약등록 완료", description = "배정시스탬에 예약추가 API 호출 및 응답이 완료된 상태"),
    BOOKING_CONFIRMED(displayName = "확정예약 완료", description = "배정시스템으로부터 설치예정일자 D-1에 확정예약을 전달완료한 상태"),
    SERVICE_SCHEDULED(displayName = "작업예정", description = "설치형 제품인 경우: 배정시스템으로부터 테크니션이 제품을 픽업한 상태\n직배송 제품인 경우: 물류 또는 택배사로부터 배송이 시작 신호를 받았을 때의 상태"),
    SERVICE_STARTED(displayName = "작업시작", description = "테크니션이 방문하여 작업을 시작한 상태"),
    SERVICE_COMPLETED(displayName = "작업완료", description = "설치형 제품인 경우: 배정시스템으로부터 테크니션이 제품 설치를 완료한 상태\n직배송 제품인 경우: 물류 또는 택배사로부터 배송이 완료 신호를 받았을 때의 상태"),
    SERVICE_CANCELED(displayName = "서비스 취소", description = "서비스플로우 취소 상태"),
    SHIPPING_CANCELED(displayName = "출고요청 취소", description = "출고요청이 취소된 상태"),
    BOOKING_CANCELED(displayName = "예약 취소", description = "배정 요청이 취소된 상태"),
    SERVICE_ON_HOLD(displayName = "서비스 보류", description = "배정이 취소되어 진행이 멈춘 상태");

    companion object {
        fun fromDisplayName(name: String): IfServiceFlowStatus? = values().find { it.displayName == name }

        fun isCanceled(status: IfServiceFlowStatus) = status.name.contains("CANCELED")
        fun isBookingRelated(status: IfServiceFlowStatus) = status.name.startsWith("BOOKING")
        fun isShippingRelated(status: IfServiceFlowStatus) = status.name.startsWith("SHIPPING")
        fun isServiceInProgress(status: IfServiceFlowStatus) = when(status) { SERVICE_STARTED, SERVICE_SCHEDULED -> true; else -> false }
        fun isCompleted(status: IfServiceFlowStatus) = status == SERVICE_COMPLETED
    }
}