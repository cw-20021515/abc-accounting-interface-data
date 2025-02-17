package com.abc.us.accounting.supports.entity

//import org.springframework.data.jpa.repository.JpaRepository
//import java.time.OffsetDateTime
//import kotlin.reflect.KMutableProperty1
//import kotlin.reflect.full.functions
//import kotlin.reflect.full.memberProperties
//import kotlin.reflect.jvm.isAccessible
//
//class EntitySaver<T : Any>(
//    private val repository: JpaRepository<T, *>,
//    private val hashCodeProperty: String = "hashCode",
//    private val isActiveProperty: String = "isActive",
//    private val createTimeProperty: String = "createTime",
//    private val updateTimeProperty: String = "updateTime"
//) {
//
//    private fun getProperty(entity: T, propertyName: String): Any? {
//        return entity::class.memberProperties.find { it.name == propertyName }
//            ?.apply { isAccessible = true }
//            ?.getter
//            ?.call(entity)
//    }
//
//    private fun setProperty(entity: T, propertyName: String, value: Any?) {
//        val property = entity::class.memberProperties.find { it.name == propertyName } as? KMutableProperty1<T, Any?>
//        property?.apply { isAccessible = true }?.setter?.call(entity, value)
//    }
//
//    private fun generateHashCodeIfMissing(entity: T) {
//        val currentHashCode = getProperty(entity, hashCodeProperty)?.toString()
//        if (currentHashCode.isNullOrEmpty()) {
//            val hashCode = entity.toEntityHash()
//            setProperty(entity, hashCodeProperty, hashCode)
//        }
//    }
//
//    private fun setTimestamps(entity: T, createTime: Any?, updateTime: OffsetDateTime) {
//        setProperty(entity, createTimeProperty, createTime)
//        setProperty(entity, updateTimeProperty, updateTime)
//    }
//
//    private fun replaceCollectionsWithMutable(entity: T) {
//        entity::class.memberProperties.forEach { property ->
//            val value = getProperty(entity, property.name)
//            if (value is Collection<*>) {
//                val mutableValue = value.toMutableList()
//                setProperty(entity, property.name, mutableValue)
//            }
//        }
//    }
//
//    private fun extractHashCodes(entities: Iterable<T>): List<String> {
//        return entities.mapNotNull { getProperty(it, hashCodeProperty)?.toString() }
//    }
//
//    private fun prepareEntities(entities: Iterable<T>): Pair<List<T>, Map<String?, T>> {
//        val newEntities = entities.filterNotNull().onEach { generateHashCodeIfMissing(it) }
//        val hashCodes = extractHashCodes(newEntities)
//
//        val existingEntities = findByHashCodes(hashCodes)
//        val existingEntitiesMap = existingEntities.associateBy { getProperty(it, hashCodeProperty)?.toString() }
//
//        return Pair(newEntities, existingEntitiesMap)
//    }
//
//    private fun handleExistingEntity(existingEntity: T, newEntity: T) {
//        setProperty(existingEntity, isActiveProperty, false)
//
//        val existingCreateTime = getProperty(existingEntity, createTimeProperty)
//        setTimestamps(newEntity, existingCreateTime, OffsetDateTime.now())
//
//        // 연관된 컬렉션 필드 갱신
//        existingEntity::class.memberProperties.forEach { property ->
//            if (property.returnType.classifier == MutableList::class || property.returnType.classifier == List::class) {
//                val existingCollection = property.getter.call(existingEntity) as? MutableCollection<Any?>
//                val newCollection = property.getter.call(newEntity) as? Collection<Any?>
//                existingCollection?.apply {
//                    clear()
//                    if (newCollection != null) {
//                        addAll(newCollection)
//                    }
//                }
//            }
//        }
//    }
//
//    private fun handleNewEntity(newEntity: T) {
//        val now = OffsetDateTime.now()
//        setTimestamps(newEntity, now, now)
//        setProperty(newEntity, isActiveProperty, true)
//
//        // 새 엔티티의 컬렉션 필드 초기화
//        replaceCollectionsWithMutable(newEntity)
//    }
//
//    private fun processEntities(
//        newEntities: List<T>,
//        existingEntitiesMap: Map<String?, T>,
//        includeUpdates: Boolean = false
//    ): Pair<MutableList<T>, MutableList<T>> {
//        val entitiesToSave = mutableListOf<T>()
//        val entitiesToUpdateOrDelete = mutableListOf<T>()
//
//        newEntities.forEach { newEntity ->
//            val hashCodeValue = getProperty(newEntity, hashCodeProperty)?.toString()
//            val existingEntity = existingEntitiesMap[hashCodeValue]
//
//            if (existingEntity != null) {
//                handleExistingEntity(existingEntity, newEntity)
//                if (includeUpdates) entitiesToUpdateOrDelete.add(existingEntity)
//            } else {
//                handleNewEntity(newEntity)
//            }
//
//            entitiesToSave.add(newEntity)
//        }
//
//        return Pair(entitiesToSave, entitiesToUpdateOrDelete)
//    }
//
//    private fun saveAndDelete(
//        entitiesToSave: List<T>,
//        entitiesToDelete: List<T>
//    ): MutableList<T> {
//        if (entitiesToDelete.isNotEmpty()) {
//            repository.deleteAll(entitiesToDelete)
//        }
//        return repository.saveAll(entitiesToSave)
//    }
//
//    private fun saveAndUpdate(
//        entitiesToSave: List<T>,
//        entitiesToUpdate: List<T>
//    ): MutableList<T> {
//        if (entitiesToUpdate.isNotEmpty()) {
//            repository.saveAll(entitiesToUpdate)
//        }
//        return repository.saveAll(entitiesToSave)
//    }
//
//    private fun findByHashCodes(hashCodes: List<String>): List<T> {
//        val findByHashCodeInMethod = repository::class.functions.find { it.name == "findByHashCodeIn" }
//        @Suppress("UNCHECKED_CAST")
//        return findByHashCodeInMethod?.call(repository, hashCodes) as? List<T> ?: emptyList()
//    }
//
//
//
//    fun saveAndDeleteAll(entities: MutableIterable<T>): MutableList<T> {
//        val (newEntities, existingEntitiesMap) = prepareEntities(entities)
//        val (entitiesToSave, entitiesToDelete) = processEntities(newEntities, existingEntitiesMap)
//        return saveAndDelete(entitiesToSave, entitiesToDelete)
//    }
//
//    fun saveAndUpdateAll(entities: MutableIterable<T>): MutableList<T> {
//        val (newEntities, existingEntitiesMap) = prepareEntities(entities)
//        val (entitiesToSave, entitiesToUpdate) = processEntities(newEntities, existingEntitiesMap, includeUpdates = true)
//        return saveAndUpdate(entitiesToSave, entitiesToUpdate)
//    }
//}
