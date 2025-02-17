package com.abc.us.accounting.collects.helper.builder

import com.abc.us.accounting.collects.domain.entity.embeddable.EmbeddableRelation
import com.abc.us.accounting.collects.domain.type.SalesTaxType
import com.abc.us.generated.models.TaxLineProperties
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.equals.shouldNotBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull

class TaxLineBuilderTest : AnnotationSpec() {
    @Test
    fun `tax converting test`() {

        val titles = listOf(
            "Texas State Tax",
            "Houston City Tax",
            "Dallas City Tax",
            "Houston Mta Transit",
            "Austin Mta Transit",
            "Dallas Mta Transit",
            "Harris Co Emergency Services District 29 Sp",
            "Special Purpose Tax"
        )

        titles.forEach { title ->
            val taxLine = TaxLineBuilder.buildFromProperty(EmbeddableRelation()
                                           .apply {
                                               entity="test"
                                               field = "test"
                                               value = "test"
                                                  }
                                       ,TaxLineProperties(title,0.0625,16.0))
            taxLine.shouldNotBeNull()
            taxLine.salesTaxType.shouldNotBeNull()
            taxLine.salesTaxType.shouldNotBeEqual(SalesTaxType.NONE)
            SalesTaxType.values().map { it.name }.shouldContain(taxLine.salesTaxType!!.name)
        }
    }

}
