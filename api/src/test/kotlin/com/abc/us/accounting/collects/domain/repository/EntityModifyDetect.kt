package com.abc.us.accounting.collects.domain.repository

import io.kotest.core.spec.style.AnnotationSpec
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.Column
import jakarta.persistence.EntityManager
import jakarta.persistence.Table
import jakarta.persistence.metamodel.Attribute
import jakarta.persistence.metamodel.EntityType
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.*

@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
abstract class EntityModifyDetect() : AnnotationSpec() {

    @Autowired
    protected lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var dataSource: javax.sql.DataSource
    fun <T> entityFields(clazz: Class<T>): Set<String> {
        val entityType: EntityType<T> = entityManager.metamodel.entity(clazz)

        val fields = mutableSetOf<String>()

        // 기본 필드 및 Embedded 필드 처리
        entityType.declaredAttributes.forEach { attribute ->
            fields.addAll(processAttribute(attribute))
        }

        return fields
    }

    private fun processAttribute(attribute: Attribute<*, *>): List<String> {
        val fieldNames = mutableListOf<String>()

        if (attribute.isAssociation || attribute.isCollection) {
            // 연관관계나 컬렉션 필드는 무시
            return fieldNames
        }

        val member = attribute.javaMember
        when (member) {
            is java.lang.reflect.Field -> {
                // @Column 어노테이션이 있는 경우 처리
                val columnAnnotation = member.getDeclaredAnnotation(Column::class.java)
                if (columnAnnotation != null) {
                    fieldNames.add(columnAnnotation.name.lowercase(Locale.getDefault()))
                } else if (attribute.persistentAttributeType != Attribute.PersistentAttributeType.EMBEDDED) {
                    // Embedded가 아닌 경우, 필드 이름을 snake_case로 변환
                    fieldNames.add(toSnakeCase(member.name))
                }

                // Embedded 필드 처리
                if (attribute.persistentAttributeType == Attribute.PersistentAttributeType.EMBEDDED) {
                    processEmbeddedField(member, fieldNames)
                }
            }
            is java.lang.reflect.Method -> {
                // 메서드 기반 필드 처리
                val columnAnnotation = member.getDeclaredAnnotation(Column::class.java)
                if (columnAnnotation != null) {
                    fieldNames.add(columnAnnotation.name.lowercase(Locale.getDefault()))
                }
            }
            else -> {
                // 다른 타입은 무시
            }
        }

        return fieldNames
    }
    private fun processEmbeddedField(member: java.lang.reflect.Field, fieldNames: MutableList<String>) {
        // AttributeOverrides 처리
        val attributeOverrides = member.getDeclaredAnnotation(AttributeOverrides::class.java)?.value.orEmpty()
        attributeOverrides.forEach { override ->
            fieldNames.add(override.column.name.lowercase(Locale.getDefault()))
        }

        // 기본 Embedded 필드 처리
        val embeddableFields = member.type.declaredFields
        embeddableFields.forEach { embeddableField ->
            embeddableField.getDeclaredAnnotation(Column::class.java)?.let { column ->
                fieldNames.add(column.name.lowercase(Locale.getDefault()))
            } ?: run {
                // Column이 없는 필드는 snake_case로 처리
                fieldNames.add(toSnakeCase(embeddableField.name))
            }
        }
    }
    private fun toSnakeCase(input: String): String {
        return input.split("(?=[A-Z])".toRegex())
            .joinToString("_") { it.lowercase(Locale.getDefault()) }
            .removePrefix("_") // 맨 앞의 "_" 제거
    }

    fun <T> tableFields(clazz: Class<T>): Set<String> {
        // @Table 애너테이션에서 테이블 이름 추출, 없으면 클래스 이름을 snake_case로 변환
        val tableName = clazz.getAnnotation(Table::class.java)?.name
            ?: toSnakeCase(clazz.simpleName)

        val connection = dataSource.connection
        val dbMetaData = connection.metaData
        val columns = dbMetaData.getColumns(null, null, tableName, null)

        val tableFields = mutableSetOf<String>()
        while (columns.next()) {
            tableFields.add(columns.getString("COLUMN_NAME").lowercase(Locale.getDefault()))
        }
        columns.close()
        connection.close()

        return tableFields
    }


    fun <T> detect(clazz: Class<T>) {

        val entityFields = entityFields(clazz)
        val tableFields = tableFields(clazz)

        // 엔터티에만 존재하는 필드
        val entityOnlyFields = entityFields - tableFields

        // 테이블에만 존재하는 필드
        val tableOnlyFields = tableFields - entityFields

        // 결과 출력 및 검증
        println("Entity-only fields: $entityOnlyFields")
        println("Table-only fields: $tableOnlyFields")

        assertThat(entityOnlyFields).isEmpty()
        assertThat(tableOnlyFields).isEmpty()

    }
}