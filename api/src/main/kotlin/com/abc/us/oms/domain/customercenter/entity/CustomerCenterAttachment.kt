package com.abc.us.oms.domain.customercenter.entity

import com.abc.us.oms.domain.common.config.AuditTimeEntity
import com.abc.us.oms.domain.customerinquiry.entity.CustomerInquiry
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.Column
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

//@Entity
//@Table(name = "customer_center_attachment", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class CustomerCenterAttachment(
    @Column(name = "file_name")
    var fileName: String,
    @Column(name = "content_type")
    var contentType: String? = null,
    @Column(name = "content_url")
    var contentUrl: String,
    @Column(name = "customer_inquiry_id")
    val customerInquiryId: String,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_inquiry_id", referencedColumnName = "id", updatable = false, insertable = false)
    val customerInquiry: CustomerInquiry? = null,
) : AuditTimeEntity()
