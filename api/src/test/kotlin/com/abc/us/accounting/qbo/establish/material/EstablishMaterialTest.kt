package com.abc.us.accounting.qbo.syncup.item

//import com.abc.us.accounting.collects.domain.entity.origin.CollectMaterial
//import com.abc.us.accounting.collects.domain.type.*
//import com.abc.us.accounting.qbo.domain.entity.ItemCreateTemplate
//import io.kotest.core.spec.style.AnnotationSpec
//import io.kotest.matchers.equals.shouldBeEqual
//import io.kotest.matchers.nulls.shouldNotBeNull
//import org.springframework.boot.test.context.SpringBootTest
//
//@SpringBootTest(properties = ["spring.profiles.active=test"])
//@ActiveProfiles("test")
//class EstablishMaterialTest(
////    private var cache : AssociationCache,
////    private var collects : CollectsMaterialWork,
//    private var establish : EstablishMaterial
//) : AnnotationSpec() {
//
//    @Test
//    fun `generation item name test`() {
//
//        val material = CollectMaterial().apply {
//            materialId = "WP_113819"
//            materialSeriesCode = "NECOA_NTBI"
//            materialName = "NECOA NT B/I Bronze Beige"
//            materialModelName = "CHP-1110N_BR"
//            materialBrandName = "NECOA"
//            materialType = MaterialType.PRODUCT.symbol
//            materialCategoryCode = MaterialCategoryCode.WATER_PURIFIER.symbol
//            productType = ProductType.PRIMARY.symbol
//            installationType = InstallationType.BUILT_IN.symbol
//            filterType = FilterType.NANO_TRAP.symbol
//            featureCode = FeatureCode.COLD_HOT_PURIFIED.symbol
//            description = "NT filter with Built-in install type & intelligent filter monitoring"
//        }
//        val template = ItemCreateTemplate().apply {
//            createCategory = ItemCreateCategory.FINANCIAL_LEASE
//            createType = ItemCreateType.GOODS
//        }
//        val name = establish.generateItemName(material,template)
//        name.shouldNotBeNull()
//        name.shouldBeEqual("NECOA_NTBI(FL.WP_113819)")
//    }
//    @Test
//    fun `establish qbo items`() {
//
////        cache.configure("N100", Account::class)
////        val materials = collects.collectMaterials()
////        establish.visitGuard{template ->
////            materials.forEach { any ->
////                val CollectMaterial = any
////                //establish.establish("N100", CollectMaterial, template)
////            }
////            true
////        }
//    }
//}
