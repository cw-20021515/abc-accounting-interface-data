package com.abc.us.accounting.collects.domain.type

/**
 * - [BillingPaymentStatus] - [BillingPaymentStatus] customerId를 생성한 채널(BB:청구서 발행 전,
 *      Scheduled:예정/대기, Completed:완료, Payment:납부, Late: 지연, Overdue: 연체, Dismantle:해체비, Penalty:위약급, Exemption: 면제, Release: 해지)
 * - `PD`     - [Payment Due]     - [납부예정] 청구서 발행 후 당월 첫 정기결제일 당일까지 납부가 안된 상태 - [우선순위 : 연체 예정]
 * - `PC`     - [Payment Completed]     - [납부완료] 청구서 기준 렌탈료+연체가산금이 모두 납부된 상태
 * - `PCD`     - [Payment Completed Dismantling Fee] - [납부완료_해체비] 해지를 진행하면서 발생한 고객 청구 비용에 대해 수납이 완료된 상태 - 렌탈 해지 처리 중인 경우 발생하는 납부 상태
 * - `BB`     - [Billing Before]     - [청구서 발생 전 상태] 청구서 발생 전 상태
 * - `PCPD`     - [Payment Completed Penalty Dismantlement Fee]     - [납부완료_위약금+해체비] 해지 처리 중 위약금/해체비의 수납이 완료된 상태 - 렌탈 해지 처리 중인 경우 발생하는 납부 상태
 * - `PCE`     - [Payment Completed Exemption]     - [납부완료_면제] 렌탈료 면제 프로모션으로 인하여, 면제 회차 발생 시 표시되는 상태
 * - `PL`     - [Payment Late]     - [미납] 청구서 발행 후 첫 정기결제일+1 일 부터 해당 청구에 대한 결제가 미완료 된 상태 - 납부 완료 시 ‘수납’으로 상태 변경
 * - `PSD`     - [Pending Storage-Disassembly Fee]     - [수납대기-해체비] 해지를 진행하면서 발생한 고객 청구 비용에 대해 수납이 완료되지 않아 대기중인 상태 - 렌탈 해지 처리 중인 경우 발생하는 납부 상태
 * - `PSPD`     - [Pending Storage-Cancellation Penalty+Disassembly Fee]     - [수납대기-위약금+해체비] 해지 처리 중 위약금/해체비의 수납이 완료되지 않아 변경된 상태값 - 렌탈 해지 처리 중인 경우 발생하는 납부 상태
 * - `PO`     - [Payment Overdue]     - [연체] 미납이 발생한 익월부터 ‘연체’로 납부 상태 표시
 */

enum class PaymentStatusEnum (val symbol:String) {
    PAYMENT_DUE("PD"),
    PAYMENT_COMPLETED("PC"),
    PAYMENT_COMPLETED_DISMANTLING_FEE("PCD"),
    BILLING_BEFORE("BB"),
    PAYMENT_COMPLETED_PENALTY_DISMANTLEMENT_FEE("PCPD"),
    PAYMENT_COMPLETED_EXEMPTION("PCE"),
    PAYMENT_LATE("PL"),
    PENDING_STORAGE_DISASSEMBLY_FEE("PSD"),
    PENDING_STORAGE_CANCELLATION_PENALTY_DISASSEMBLY_FEE("PSCPD"),
    PAYMENT_OVERDUE("PO")
}