package com.abc.us.accounting.collects.domain.entity.embeddable

import com.abc.us.accounting.supports.entity.Hashable
import com.abc.us.accounting.supports.utils.Hashs
import jakarta.persistence.Embeddable
import org.hibernate.annotations.Comment

@Embeddable
class EmbeddableLocation : Hashable {

    @Comment("barnch ID")
    var branchId: String? = null

    @Comment("창고 ID")
    var warehouseId: String? = null

    @Comment("위도")
    var latitude: Double? = null

    @Comment("경도")
    var longitude: Double? = null

    @Comment("주소가 위치한 도시 이름")
    var city: String? = null

    @Comment("주소가 위치한 국가의 코드(예 : US,KR)")
    var country: String? = null

    @Comment("주소가 위치한 국가의 코드(예 : US,KR)")
    var countryCode: String? = null

    @Comment("우편번호")
    var zipCode: String? = null

    @Comment("주, 도, 광역시 등 행정 구역의 코드를 나타냄 (예 : CA)")
    var state: String? = null

    @Comment("소가 위치한 군(county) 또는 구(district)와 같은 하위 행정 구역")
    var county: String? = null

    @Comment("주소정보")
    var address1: String? = null

    var address2: String? = null

    var locationRemark: String? = null

    override fun hashValue(): String {
        val builder = StringBuilder()
        val inputStr = builder
            .append(branchId).append("|")
            .append(warehouseId).append("|")
            .append(latitude).append("|")
            .append(longitude).append("|")
            .append(city).append("|")
            .append(country).append("|")
            .append(countryCode).append("|")
            .append(zipCode).append("|")
            .append(state).append("|")
            .append(county).append("|")
            .append(address1).append("|")
            .append(address2).append("|")
            .append(locationRemark)
            .toString()
        return Hashs.sha256Hash(inputStr)
    }
}