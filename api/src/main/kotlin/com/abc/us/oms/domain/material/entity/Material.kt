@file:Suppress("JpaDataSourceORMInspection")

package com.abc.us.oms.domain.material.entity
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.persistence.CascadeType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDate
import java.time.LocalDateTime

//@Entity
//@Table(name = "material", schema = "public", catalog = "abc_oms")
@JsonInclude(JsonInclude.Include.NON_NULL)
class Material(
    @Id
    @Column(name = "id")
    val id: String,
    @Column(name = "manufacturer_code")
    val manufacturerCode: String,
    @Column(name = "type")
    val type: String,
    @Column(name = "model_name")
    var modelName: String? = null,
    @Column(name = "model_name_prefix")
    var modelNamePrefix: String? = null,
    @Column(name = "name")
    var name: String,
    @Column(name = "brand_name")
    var brandName: String? = null,
    @Column(name = "product_type")
    var productType: String? = null,
    @Column(name = "sales_type")
    var salesType: String? = null,
    @Column(name = "sales_status")
    var salesStatus: String? = null,
    @Column(name = "description")
    var description: String? = null,
    @Column(name = "is_representative")
    var isRepresentative: Boolean? = false,
    @Column(name = "is_recurring_delivery")
    var isRecurringDelivery: Boolean? = false,
    @Column(name = "is_data_setup_completed")
    var isDataSetupCompleted: Boolean? = false,
    @Column(name = "shipping_method_type")
    var shippingMethodType: String? = null,
    @Column(name = "maximum_units_per_order")
    var maximumUnitsPerOrder: Int? = 0,
    @Column(name = "life_span")
    var lifespan: Int? = 0,
    @Column(name = "relation_type")
    var relationType: String,
    @Column(name = "effective_start_date")
    var effectiveStartDate: LocalDate? = null,
    @Column(name = "effective_end_date")
    var effectiveEndDate: LocalDate? = null,
    @Column(name = "registered_by")
    var registeredBy: String,
    @Column(name = "display_related_materials_in_store")
    var displayRelatedMaterialsInStore: Boolean? = false,
    @Column(name = "resource_folder_url")
    var resourceFolderUrl: String? = null,
    @CreatedBy
    @Column(name = "create_user")
    var createUser: String? = "Administrator",
    @Column(name = "create_time")
    @CreatedDate
    var createTime: LocalDateTime? = null,
    @LastModifiedBy
    @Column(name = "update_user")
    var updateUser: String? = createUser,
    @Column(name = "update_time")
    @LastModifiedDate
    var updateTime: LocalDateTime? = null,
    @Column(name = "category_id")
    var categoryId: String,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "material_related_ids", joinColumns = [JoinColumn(name = "material_id")])
    @Column(name = "related_material_id")
    var relatedMaterialIds: MutableList<String>? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id", insertable = false, updatable = false)
    var category: MaterialCategory? = null,
    @Column(name = "series_id")
    var seriesId: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id", referencedColumnName = "id", insertable = false, updatable = false)
    var series: MaterialSeries? = null,
    @OneToMany(mappedBy = "material", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("create_time ASC")
    var attributes: MutableList<MaterialAttribute> = mutableListOf(),
    @OneToMany(mappedBy = "material", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("create_time ASC")
    var specifications: MutableList<MaterialSpecification> = mutableListOf(),
    @OneToMany(mappedBy = "material", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("create_time ASC")
    var contents: MutableList<MaterialContent> = mutableListOf(),
    @OneToMany(mappedBy = "material", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("create_time ASC")
    var prices: MutableList<MaterialPrice> = mutableListOf(),
)
