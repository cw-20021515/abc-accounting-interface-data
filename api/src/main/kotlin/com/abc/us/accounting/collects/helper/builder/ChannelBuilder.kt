package com.abc.us.accounting.collects.helper.builder

import com.abc.us.accounting.collects.domain.entity.collect.CollectChannel
import com.abc.us.accounting.collects.domain.entity.collect.CollectOrder
import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableRelation
import com.abc.us.accounting.collects.domain.type.ChannelTypeEnum
import com.abc.us.generated.models.Channel
import com.abc.us.generated.models.OrderView
import mu.KotlinLogging


class ChannelBuilder {
    companion object {
        private val logger = KotlinLogging.logger {}
        fun makeChannel(order: OrderView, channel: Channel): CollectChannel {
            return CollectChannel().apply {
                relation = EmbeddableRelation().apply {
                    entity = CollectOrder::class.simpleName
                    field = "order_id"
                    value = order.orderId
                }
                channelId = channel.channelId
                channelType = channel.channelType?.let { type ->
                    when (type) {
                        Channel.ChannelType.ONLINE_MALL -> ChannelTypeEnum.ONLINE_MALL
                        Channel.ChannelType.CUSTOMER_CENTER -> ChannelTypeEnum.CUSTOMER_CENTER
                        Channel.ChannelType.OFFLINE_STORE -> ChannelTypeEnum.OFFLINE_STORE
                        Channel.ChannelType.SELLER -> ChannelTypeEnum.SELLER
                        Channel.ChannelType.OTHER -> ChannelTypeEnum.OTHER
                        Channel.ChannelType.INFLUENCER -> ChannelTypeEnum.INFLUENCER
                    }
                }
                channelName = channel.channelName
                channelDetail = channel.channelDetail
                createTime = channel.createTime
                updateTime = channel.updateTime
            }
        }

        fun build(originOrders: MutableList<OrderView>): MutableList<CollectChannel> {

            val channels = mutableListOf<CollectChannel>()
            originOrders.forEach { order ->
                val channel = makeChannel(order, order.channel)
                channels.add(channel)
            }
            return channels
        }
    }

}