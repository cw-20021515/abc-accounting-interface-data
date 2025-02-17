package com.abc.us.accounting.collects.helper.builder

import com.abc.us.accounting.collects.domain.entity.collect.CollectInstallation
import com.abc.us.accounting.collects.domain.entity.collect.CollectOrderItem
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableLocation
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableRelation
import com.abc.us.generated.models.InstallationInformation
import com.abc.us.generated.models.OrderItemView

class InstallationBuilder {
    companion object {
        fun makeInstallation(install: InstallationInformation): CollectInstallation {
            return CollectInstallation().apply {
                relation = EmbeddableRelation().apply {
                    entity = CollectOrderItem::class.simpleName
                    field = "order_item_id"
                    value = install.orderItemId
                }
                installId = install.installationInformationId
                orderItemId = install.orderItemId
                serialNumber = install.serialNumber
                technicianId = install.technicianId
                serviceFlowId = install.serviceFlowId
                installationTime = install.installationTime
                warrantyStartTime = install.warrantyStartTime
                warrantyEndTime = install.warrantyEndTime
                waterType = install.waterType
                createTime = install.createTime
                updateTime = install.updateTime

                location = EmbeddableLocation().apply {
                    branchId = install.branchId
                    warehouseId = install.warehouseId
                    state = install.state
                    city = install.city
                    address1 = install.address1
                    address2 = install.address2
                    latitude = install.latitude
                    longitude = install.longitude
                    zipCode = install.zipcode
                }
            }
        }

        fun build(orderItems: MutableList<OrderItemView>): MutableList<CollectInstallation> {
            val installs = mutableListOf< CollectInstallation>()
            orderItems.forEach { orderItem ->
                orderItem.installationInformation?.let { install ->
                    install.installationInformationId?.let { infoId ->
                        installs.add(makeInstallation(install))
                    }
                }
            }
            return installs
        }
    }
}