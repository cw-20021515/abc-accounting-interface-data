package com.abc.us.accounting.collects.domain.entity.embeddable

import com.abc.us.accounting.supports.entity.Hashable
import com.abc.us.accounting.supports.utils.Hashs
import jakarta.persistence.Embeddable
import org.hibernate.annotations.Comment

@Embeddable
class EmbeddableName  : Hashable {

    @Comment("name 제목")
    var titleName : String?=null
    @Comment("성")
    var firstName : String?=null

    @Comment("성")
    var middleName : String?=null

    var lastName : String?=null

    var familyName : String?=null;
    @Comment("이름 앞에 붙는 수식 (예 : sir)" )
    var nameSuffix : String?=null

    var fullyQualifiedName : String?=null

    @Comment("회사 이름" )
    var companyName : String?=null

    @Comment("")
    var displayName : String?=null

    @Comment("AMS id" )
    var userId : String?=null

    @Comment("첫번째 phone 번호" )
    var primaryPhone : String?=null

    @Comment("대체 phone 번호" )
    var alternatePhone : String?=null

    @Comment("모바일 번호" )
    var mobile : String?=null

    @Comment("fax 번호" )
    var fax : String?=null

    @Comment("첫번째 이메일 주소" )
    var primaryEmail : String?=null

    @Comment("대체 이메일 번호" )
    var alternateEmail : String?=null

    @Comment("web site 주소" )
    var webAddr : String?=null

    override fun hashValue(): String {
        val builder = StringBuilder()
        val inputStr = builder
            .append(titleName).append("|")
            .append(firstName).append("|")
            .append(middleName).append("|")
            .append(lastName).append("|")
            .append(familyName).append("|")
            .append(nameSuffix).append("|")
            .append(fullyQualifiedName).append("|")
            .append(companyName).append("|")
            .append(displayName).append("|")
            .append(userId).append("|")
            .append(primaryPhone).append("|")
            .append(alternatePhone).append("|")
            .append(mobile).append("|")
            .append(fax).append("|")
            .append(primaryEmail).append("|")
            .append(alternateEmail).append("|")
            .append(webAddr)
            .toString()
        return Hashs.sha256Hash(inputStr)
    }
}