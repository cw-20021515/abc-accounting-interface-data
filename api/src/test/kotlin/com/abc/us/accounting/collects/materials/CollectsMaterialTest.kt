package com.abc.us.accounting.collects.materials

import io.kotest.core.spec.style.AnnotationSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(properties = ["spring.profiles.active=test"])
@ActiveProfiles("test")
class CollectsMaterialTest(
//    private val CollectMaterial : CollectsMaterialWork
) : AnnotationSpec() {
    @Test
    fun `generate material for accounting`() {

//        val newEntity = CollectMaterial.generateEntity("WP_113818")
//        newEntity.shouldNotBeNull()
    }

    @Test
    fun `collect material for accounting`() {

//        val collectedMaterials = CollectMaterial.collectMaterials()
//        collectedMaterials.shouldNotBeNull()
//        collectedMaterials.shouldNotBeEmpty()
//
//        val savedMaterials = CollectMaterial.saveIfNotExistsMaterials(collectedMaterials)
//        savedMaterials.shouldNotBeNull()
//        savedMaterials.shouldNotBeEmpty()
    }

}
