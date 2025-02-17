package com.abc.us.accounting.rentals.onetime.model

import com.abc.us.accounting.documents.domain.entity.DocumentTemplate
import com.abc.us.accounting.documents.domain.type.CompanyCode
import com.abc.us.accounting.iface.domain.entity.oms.*
import com.abc.us.accounting.iface.domain.model.Refund
import com.abc.us.accounting.logistics.domain.entity.InventoryCosting

sealed interface OnetimeProcessItem {
    val companyCode: CompanyCode
    val docTemplate: DocumentTemplate
}

data class OnetimePaymentProcessItem(
    override val companyCode: CompanyCode,
    override val docTemplate: DocumentTemplate,
    val customerId: String,
    val onetimePayment: IfOnetimePayment? = null,
    val orderItem: IfOrderItem? = null,
    val material: IfMaterial? = null,
    val serviceFlow: IfServiceFlow? = null,
    val inventoryCosting: InventoryCosting? = null,
    val channel: IfChannel? = null,
    val refund: Refund? = null,
):OnetimeProcessItem{

}

data class OnetimeLogisticsProcessItem(
    override val companyCode: CompanyCode,
    override val docTemplate: DocumentTemplate,
    val orderItem: IfOrderItem,
    val material: IfMaterial?,
    val serviceFlowItem: IfServiceFlow? = null,
    val inventoryCosting: InventoryCosting? = null,
):OnetimeProcessItem{

}