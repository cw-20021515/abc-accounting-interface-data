package com.abc.us.accounting.qbo.syncup.item
import com.abc.us.accounting.collects.trigger.audit.AsyncEventTrailer
import com.abc.us.accounting.commons.domain.type.TimeZoneCode
import com.abc.us.accounting.qbo.domain.entity.QboItem
import com.abc.us.accounting.qbo.domain.entity.QboItemTemplate
import com.abc.us.accounting.qbo.domain.entity.key.QboItemKey
import com.abc.us.accounting.qbo.domain.repository.ItemCreateTemplateRepository
import com.abc.us.accounting.qbo.domain.repository.QboItemRepository
import com.abc.us.accounting.qbo.interact.QBOService
import com.abc.us.accounting.qbo.service.QboAccountService
import com.abc.us.accounting.supports.converter.JsonConverter
import com.abc.us.accounting.iface.domain.entity.oms.IfMaterial
import com.abc.us.accounting.iface.domain.repository.oms.IfMaterialRepository
import com.intuit.ipp.data.Item
import com.intuit.ipp.data.ItemTypeEnum
import com.intuit.ipp.data.ReferenceType
import com.intuit.ipp.util.StringUtils
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.OffsetDateTime


@Service
class SyncUpItem (
    private val qboService: QBOService,
    private val itemRepository : QboItemRepository,
    private val ifMaterialRepository: IfMaterialRepository,
    private val qboAccountService: QboAccountService,
    private val createTemplateRepository : ItemCreateTemplateRepository
)  {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val converter = JsonConverter()
    }
    fun buildSubmittedItem(submittedItem : Item,
                           template : QboItemTemplate) : QboItem {
        val submitJson = converter.toJson(submittedItem)
        return QboItem(key = QboItemKey(qboId = submittedItem.id,
            companyCode= template.companyCode),
            displayName = submittedItem.name,
            templateId = template.templateId,
            createCategory = template.createCategory,
            createType = template.createType,
            associatedType = template.associatedType,
            submitResult = submitJson?.let { it }?:"" )
            .apply {
                assetAccountCode = template.assetAccountCode
                assetAccountName = template.assetAccountName
                incomeAccountCode = template.incomeAccountCode
                incomeAccountName = template.incomeAccountName
                expenseAccountCode = template.expenseAccountCode
                expenseAccountName = template.expenseAccountName
                documentType = template.documentType
                managementUnit =  template.managementUnit
                createTime = OffsetDateTime.now()
                updateTime = OffsetDateTime.now()
            }
    }

    fun buildSubmittedItem(material : IfMaterial,
                           submittedItem : Item,
                           template : QboItemTemplate) : QboItem {
        val submitJson = converter.toJson(submittedItem)
        return QboItem(key = QboItemKey(qboId = submittedItem.id,
                                      companyCode= template.companyCode),
                       displayName = submittedItem.name,
                       templateId = template.templateId,
                       createCategory = template.createCategory,
                       createType = template.createType,
                       associatedType = template.associatedType,
                       submitResult = submitJson?.let { it }?:"" )
            .apply {
                materialId = material.materialId
                assetAccountCode = template.assetAccountCode
                assetAccountName = template.assetAccountName
                incomeAccountCode = template.incomeAccountCode
                incomeAccountName = template.incomeAccountName
                expenseAccountCode = template.expenseAccountCode
                expenseAccountName = template.expenseAccountName
                documentType = template.documentType
                managementUnit =  template.managementUnit
                createTime = OffsetDateTime.now()
                updateTime = OffsetDateTime.now()
            }
    }

    fun buildItemName(material: IfMaterial, template : QboItemTemplate) : String{
        val builder = StringBuilder()
        builder.append("ABC.M.")
        builder.append(template.createCategory.symbol)
        builder.append(".")
        builder.append(template.createType.symbol)
        builder.append(".")
        builder.append(material.materialId)
        return builder.toString()
    }

    fun buildItemName(template : QboItemTemplate) : String{
        val builder = StringBuilder()
        builder.append("ABC.M.")
        builder.append(template.createCategory.symbol)
        builder.append(".")
        builder.append(template.createType.symbol)
        builder.append(".")
        return builder.toString()
    }

    fun buildQboItem(template : QboItemTemplate) : Item {

        return Item().apply {
            name = buildItemName(template)
            description = template.createCategory.name + "-" + template.createType.name

            type = ItemTypeEnum.fromValue(template.associatedType)
            assetAccountRef = template.assetAccountCode?.let { acctNum ->
                ReferenceType().apply {
                    qboAccountService.findQboAccount(template.companyCode,acctNum)?.let {
                        value = it.key.qboId
                        name = it.key.accountName
                    }?: run {
                        throw NoSuchFieldException("Not found submitted ${template.assetAccountName}(${template.assetAccountCode})")
                    }
                }
            }
            expenseAccountRef = template.expenseAccountCode?.let {acctNum ->
                ReferenceType().apply {
                    qboAccountService.findQboAccount(template.companyCode,acctNum)?.let {
                        value = it.key.qboId
                        name = it.key.accountName
                    }?: run {
                        throw NoSuchFieldException("Not found submitted ${template.expenseAccountName}(${template.expenseAccountCode})")
                    }
                }
            }
            incomeAccountRef = template.incomeAccountCode?.let { acctNum ->
                ReferenceType().apply {
                    qboAccountService.findQboAccount(template.companyCode,acctNum)?.let {
                        value = it.key.qboId
                        name = it.key.accountName
                    }?: run {
                        throw NoSuchFieldException("Not found submitted ${template.incomeAccountName}(${template.incomeAccountCode})")
                    }
                }
            }
        }
    }

    fun buildQboItem(material: IfMaterial, template : QboItemTemplate) : Item {

        return Item().apply {
            name = buildItemName(material,template)
            description = material.description
            if (template.managementUnit != null && template.managementUnit == "SKU") {
                sku = material.materialId
            }
            type = ItemTypeEnum.fromValue(template.associatedType)
            assetAccountRef = template.assetAccountCode?.let { acctNum ->
                ReferenceType().apply {
                    qboAccountService.findQboAccount(template.companyCode,acctNum)?.let {
                        value = it.key.qboId
                        name = it.key.accountName
                    }?: run {
                        throw NoSuchFieldException("Not found submitted ${template.assetAccountName}(${template.assetAccountCode})")
                    }
                }
            }
            expenseAccountRef = template.expenseAccountCode?.let {acctNum ->
                ReferenceType().apply {
                    qboAccountService.findQboAccount(template.companyCode,acctNum)?.let {
                        value = it.key.qboId
                        name = it.key.accountName
                    }?: run {
                        throw NoSuchFieldException("Not found submitted ${template.expenseAccountName}(${template.expenseAccountCode})")
                    }
                }
            }
            incomeAccountRef = template.incomeAccountCode?.let { acctNum ->
                ReferenceType().apply {
                    qboAccountService.findQboAccount(template.companyCode,acctNum)?.let {
                        value = it.key.qboId
                        name = it.key.accountName
                    }?: run {
                        throw NoSuchFieldException("Not found submitted ${template.incomeAccountName}(${template.incomeAccountCode})")
                    }
                }
            }
        }
    }
    fun submit(template : QboItemTemplate) : MutableList<QboItem>{
        val submittedItems = mutableListOf<QboItem>()
        try {
            val qboItem = buildQboItem( template)
            val addedItem = qboService.add(template.companyCode, qboItem)
            addedItem?.let { item ->
                val abcItem = buildSubmittedItem(addedItem, template)
                submittedItems.add(abcItem)
                logger.info("Add Item-[${addedItem.id}.${addedItem.name}]")
            }
        }
        catch (e: Exception) {
            logger.error { "Failure Add QboItem [${e.message}]" }
        }
        return submittedItems
    }
    fun submit(materials : List<IfMaterial>, template : QboItemTemplate) : MutableList<QboItem>{
        val submittedMaterial = mutableMapOf<String, IfMaterial>()
        val qboItems = mutableListOf<Item>()
        materials.forEach { material->
            val qboItem = buildQboItem(material, template)
            submittedMaterial[qboItem.name] = material
            qboItems.add(qboItem)
        }
        val submittedItems = mutableListOf<QboItem>()
        try {
            val addedItems = qboService.batchAdd(template.companyCode, Item::class.java,qboItems)
            addedItems?.let {
                addedItems.forEach { addedItem ->
                    val material = submittedMaterial[addedItem.name]
                    val abcItem = buildSubmittedItem(material!!, addedItem, template)
                    submittedItems.add(abcItem)
                    logger.info("Add Item-[${addedItem.id}.${addedItem.name}]")
                }
            }
        }
        catch (e: Exception) {
            logger.error { "Failure Add QboItem [${e.message}]" }
        }
        return submittedItems
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun bulkInsert(submittedItems : MutableList<QboItem>) {
        itemRepository.saveAll(submittedItems)
    }

    fun submit(trailer: AsyncEventTrailer){

        val from = trailer.queries().get("startDateTime") as LocalDateTime
        val to = trailer.queries().get("endDateTime") as LocalDateTime
        val timezone = trailer.queries().get("timezone") as TimeZoneCode
        val reversing = trailer.reversing()
        logger.info { "QBO-START[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }
        val materials = ifMaterialRepository.findAll()
        val templates = createTemplateRepository.findAll()
        templates.forEach { template ->
            val submittedItems = mutableListOf<QboItem>()

            if(StringUtils.hasText(template.managementUnit) &&
                template.managementUnit.equals("SKU", ignoreCase = true) ) {

                materials.chunked(QBOService.BATCH_SIZE).forEach { chunk ->
                    submittedItems.addAll( submit(chunk,template) )
                    bulkInsert(submittedItems)
                }
            }
            else {
                submittedItems.addAll(submit(template))
                bulkInsert(submittedItems)
            }

        }
        logger.info { "QBO-END[${this::class.java.simpleName}]-From(${from}) ~ To(${to})" }
    }
}