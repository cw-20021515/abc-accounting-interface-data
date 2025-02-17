package com.abc.us.accounting.iface.domain.type.oms

enum class IfOrderItemStatus(val displayName: String, val koreanName: String, val description: String = "") {
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
        fun fromDisplayName(name: String): IfOrderItemStatus? = entries.find { it.displayName == name }
        fun fromKoreanName(name: String): IfOrderItemStatus? = entries.find { it.koreanName == name }
        fun fromName(name: String): IfOrderItemStatus? = entries.find { it.name == name }


        private fun getStatusList (from: IfOrderItemStatus, to: IfOrderItemStatus):List<IfOrderItemStatus> {
            return entries.filter { it.ordinal in from.ordinal..to.ordinal }
        }

        fun isOrderInstallStatus(status: IfOrderItemStatus):Boolean {
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
    fun isAcceptedStatus(claimStatus: IfOrderItemStatus):Boolean {
        when(claimStatus) {
            ORDER_RECEIVED, ORDER_COMPLETED, BOOKING_SCHEDULED, BOOKING_CONFIRMED, ORDER_ON_HOLD, WORK_IN_PROGRESS, CONTRACT_CONFIRMED, INSTALL_COMPLETED, ORDER_CONFIRMED -> {
                val list = getStatusList(claimStatus, ORDER_CONFIRMED)
                return list.contains(this)
            }
            CANCELLATION_RECEIVED,  CANCELLATION_PROCESSING, CANCELLATION_COMPLETED -> {
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
            SUSPENDED, PRODUCT_PREPARING, DELIVERY_IN_PROGRESS, DELIVERY_COMPLETED -> {
                return this == SUSPENDED
            }
            else -> return false
        }
    }
}