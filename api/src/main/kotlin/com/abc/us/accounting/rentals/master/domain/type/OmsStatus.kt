package com.abc.us.accounting.rentals.master.domain.type


enum class ContractStatus(val code:String, val displayName: String, val koreanName: String, val description: String = "") {
    PENDING_CONFIRMATION("PC", displayName = "Pending Confirmation", koreanName = "계약 확인 대기 중"),
    ACTIVE("AT", displayName = "Active", koreanName = "활성화된 계약"),
    BREACHED_CONTRACT("BC", displayName = "Breached Contract", koreanName = "위반된 계약"),
    TRANSFER_TO_COLLECTION("TC", displayName = "Transfer To Collection", koreanName = "추심 전환된 계약"),
    CONTRACT_ENDED("CE", displayName = "Contract Ended", koreanName = "약정/의무사용기간 종료"),
    CONTRACT_CANCELLED("CC", displayName = "Contract Cancelled", koreanName = "렌탈 해지"),
    CONTRACT_WITHDRAWN("CW", displayName = "Contract Withdrawn", koreanName = "렌탈 주문 철회");

    companion object {
        fun fromDisplayName(name: String): ContractStatus? = values().find { it.displayName == name }
        fun fromKoreanName(name: String): ContractStatus? = values().find { it.koreanName == name }
    }
}

enum class OrderItemStatus(val displayName: String, val koreanName: String, val description: String = "") {
    ORDER_RECEIVED(displayName = "Order Received", koreanName = "주문 접수", description = "주문 접수 설명"),
    ORDER_COMPLETED(displayName = "Order Completed", koreanName = "주문 완료", description = "주문 완료 설명"),
    BOOKING_SCHEDULED(displayName = "Booking Scheduled", koreanName = "가배정"),
    BOOKING_CONFIRMED(displayName = "Booking Confirmed", koreanName = "확정배정"),
    ORDER_ON_HOLD(displayName = "Order On Hold", koreanName = "주문보류"),
    WORK_IN_PROGRESS(displayName = "Work In Progress", koreanName = "작업시작", description = "작업시작 설명"),
    CONTRACT_CONFIRMED(displayName = "Contract Confirmed", koreanName = "계약확정"),
    INSTALL_COMPLETED(displayName = "Install Completed", koreanName = "설치완료"),
    ORDER_CONFIRMED(displayName = "Order Confirmed", koreanName = "주문확정"),
    CANCELLATION_RECEIVED(displayName = "Cancellation Received", koreanName = "취소요청접수"),
    CANCELLATION_PROCESSING(displayName = "Cancellation Processing", koreanName = "취소완료대기"),
    CANCELLATION_REQUESTED(displayName = "Cancellation Requested", koreanName = "취소처리요청"),
    CANCELLATION_COMPLETED(displayName = "Cancellation Completed", koreanName = "취소완료"),
    RETURN_RECEIVED(displayName = "Return Received", koreanName = "해지접수"),
    RETURN_PROCESSING(displayName = "Return Processing", koreanName = "해지진행중"),
    RETURN_ON_HOLD(displayName = "Return On Hold", koreanName = "해지보류"),
    RETURN_COMPLETED(displayName = "Return Completed", koreanName = "해지/반환완료"),
    REFUND_RECEIVED(displayName = "Refund Received", koreanName = "반품접수"),
    REFUND_PROCESSING(displayName = "Refund Processing", koreanName = "반품진행중"),
    REFUND_ON_HOLD(displayName = "Refund On Hold", koreanName = "반품보류"),
    DISMANTLING_STARTED(displayName = "Dismantling Started", koreanName = "해체작업시작"),
    MATERIAL_RETURNED(displayName = "Material Returned", koreanName = "해체제품입고완료"),
    REFUND_COMPLETED(displayName = "Refund Completed", koreanName = "반품완료"),
    SUSPENDED(displayName = "Suspended", koreanName = "중지"),
    PRODUCT_PREPARING(displayName = "Product Preparing", koreanName = "상품 준비중"),
    DELIVERY_IN_PROGRESS(displayName = "Delivery In Progress", koreanName = "배송중"),
    DELIVERY_COMPLETED(displayName = "Delivery Completed", koreanName = "배송완료");

    companion object {
        fun fromDisplayName(name: String): OrderItemStatus? = entries.find { it.displayName == name }
        fun fromKoreanName(name: String): OrderItemStatus? = entries.find { it.koreanName == name }
        fun fromName(name: String): OrderItemStatus? = entries.find { it.name == name }


        private fun getStatusList (from:OrderItemStatus, to:OrderItemStatus):List<OrderItemStatus> {
            return entries.filter { it.ordinal in from.ordinal..to.ordinal }
        }

        fun isOrderInstallStatus(status: OrderItemStatus):Boolean {
            val list = getStatusList(status, ORDER_CONFIRMED)
            return list.contains(status)
        }
    }


    /**
     * 현재 상태가 과거의 상태에서 지나 간 것을 보장 하는지?
     * - 현재상태: this,
     * - 판단상태: claimStatus
     * 예) claimStatus: ORDER_RECEIVED => 현재 상태는 주문 접수 부터 ORDER_CONFIRMED 까지 가능
     * TODO: 현재는 history가 안넘어와서 임시로 구현함 => history가 넘어오면 정확하게 체크할 예정임
     */
    fun isAcceptedStatus(claimStatus:OrderItemStatus):Boolean {
        when(claimStatus) {
            ORDER_RECEIVED, ORDER_COMPLETED, BOOKING_SCHEDULED, BOOKING_CONFIRMED, ORDER_ON_HOLD, WORK_IN_PROGRESS, CONTRACT_CONFIRMED, INSTALL_COMPLETED, ORDER_CONFIRMED -> {
                val list = getStatusList(claimStatus, ORDER_CONFIRMED)
                return list.contains(this)
            }
            CANCELLATION_RECEIVED,  CANCELLATION_PROCESSING, CANCELLATION_COMPLETED-> {
                val list = getStatusList(claimStatus, CANCELLATION_COMPLETED)
                return list.contains(this)
            }
            RETURN_RECEIVED, RETURN_PROCESSING, RETURN_ON_HOLD, RETURN_COMPLETED -> {
                val list = getStatusList(claimStatus, RETURN_COMPLETED)
                return list.contains(this)
            }
            REFUND_RECEIVED, REFUND_PROCESSING, REFUND_ON_HOLD, DISMANTLING_STARTED, MATERIAL_RETURNED, REFUND_COMPLETED -> {
                val list = getStatusList(claimStatus, REFUND_COMPLETED)
                return list.contains(this)
            }
            SUSPENDED, PRODUCT_PREPARING, DELIVERY_IN_PROGRESS, DELIVERY_COMPLETED-> {
                return this == SUSPENDED
            }
            else -> return false
        }
    }
}
enum class OrderItemType(val value: String) {

    RENTAL("RENTAL"),
    ONETIME("ONETIME"),
    AUTO_ORDER("AUTO_ORDER"),
    NONE("NONE");
    companion object {
        fun fromName(name: String): OrderItemType? = OrderItemType.entries.find { it.name == name }

    }
}




enum class ServiceFlowStatus(val displayName: String, val description: String = "") {
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
        fun fromDisplayName(name: String): ServiceFlowStatus? = values().find { it.displayName == name }

        fun isCanceled(status: ServiceFlowStatus) = status.name.contains("CANCELED")
        fun isBookingRelated(status: ServiceFlowStatus) = status.name.startsWith("BOOKING")
        fun isShippingRelated(status: ServiceFlowStatus) = status.name.startsWith("SHIPPING")
        fun isServiceInProgress(status: ServiceFlowStatus) = when(status) { SERVICE_STARTED, SERVICE_SCHEDULED -> true; else -> false }
        fun isCompleted(status: ServiceFlowStatus) = status == SERVICE_COMPLETED
    }
}

enum class ServiceFlowType(val displayName: String, val koreanName: String, val description: String = "") {
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
        fun fromDisplayName(name: String): ServiceFlowType? = values().find { it.displayName == name }
        fun fromKoreanName(name: String): ServiceFlowType? = values().find { it.koreanName == name }

        // 서비스 유형 분류 도움 메서드
        fun isRelocationRelated(type: ServiceFlowType) = type.name.startsWith("RELOCATION")
        fun isInstallRelated(type: ServiceFlowType) =
            type == INSTALL || type == RELOCATION_INSTALL || type == REINSTALL
        fun isUninstallRelated(type: ServiceFlowType) =
            type == RELOCATION_UNINSTALL || type == RETURN || type == REFUND
    }
}

enum class OmsChargeStatus(val value: String) {
    CREATED("CREATED"),
    SCHEDULED("SCHEDULED"),
    PENDING("PENDING"),
    PAID("PAID"),
    UNPAID("UNPAID"),
    OVERDUE("OVERDUE")
}
