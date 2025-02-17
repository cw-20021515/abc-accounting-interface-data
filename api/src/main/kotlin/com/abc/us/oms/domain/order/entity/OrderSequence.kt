package com.abc.us.oms.domain.order.entity

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

//@Entity
//@Table(name = "order_sequence", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class OrderSequence(
    @Id
    @Column(name = "date_key")
    val dateKey: String,
    var seq: Long,
)
