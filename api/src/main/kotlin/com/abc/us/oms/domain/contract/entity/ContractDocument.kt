@file:Suppress("JpaDataSourceORMInspection")

package com.abc.us.oms.domain.contract.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

//@Entity
//@Table(name = "contract_document", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class ContractDocument(
    @Id
    @Column(name = "id")
    var id: String,
    @Column(name = "contract_id")
    var contractId: String,
    @Column(name = "channel_contract_id")
    var channelContractId: String,
    @Column(name = "customer_id")
    var customerId: String,
    @Column(name = "order_item_id")
    var orderItemId: String,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "contract_id",
        insertable = false,
        updatable = false,
        referencedColumnName = "id",
    )
    var contract: Contract? = null,
    @Column(name = "revision")
    var revision: Int? = 0,
    @Column(name = "mapping_data", columnDefinition = "TEXT")
    var mappingData: String? = null,
    @Column(name = "file_url")
    var fileUrl: String? = null,
    @CreatedDate
    @Column(name = "create_time")
    val createTime: LocalDateTime? = null,
    @LastModifiedDate
    @Column(name = "update_time")
    var updateTime: LocalDateTime? = null,
)
