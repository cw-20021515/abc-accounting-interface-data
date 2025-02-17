//package com.abc.us.accounting.rentals.flease.service
//
//import io.kotest.core.spec.style.FunSpec
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
//import org.springframework.test.context.ActiveProfiles
//
//@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@ActiveProfiles("test")
//class RentalFinancialTemplateServiceTest(
//    rentalFinancialTemplateService:RentalFinancialTemplateService,
//): FunSpec( {
//
//    // [금융리스] 제품출고
//    test("template_CFOR002") {
//        rentalFinancialTemplateService.cFOR002()
//    }
//
//    // [금융리스] 설치완료-재화매출 인식
//    test("template_CFOR003") {
//        rentalFinancialTemplateService.cFOR003()
//    }
//
//    // [금융리스] 설치완료-매출원가 인식
//    test("template_CFOR004") {
//        rentalFinancialTemplateService.cFOR004()
//    }
//
//    // [금융리스] 설치완료-재고가액 확정
//    test("template_CFOR006") {
//        rentalFinancialTemplateService.cFOR006()
//    }
//
//    // [금융리스] 청구
//    test("template_CFCP001") {
//        rentalFinancialTemplateService.cFCP001()
//    }
//
//    // [금융리스] 수납
//    test("template_CFCP002") {
//        rentalFinancialTemplateService.cFCP002()
//    }
//
//    // [금융리스] 입금
//    test("template_CFCP003") {
//        rentalFinancialTemplateService.cFCP003()
//    }
//
//    // [금융리스:상각] 금융리스상각
//    test("template_CFDP001") {
//        rentalFinancialTemplateService.cFDP001()
//    }
//
//    // [금융리스:서비스매출] 필터배송
//    test("template_CFSS001") {
//        rentalFinancialTemplateService.cFSS001()
//    }
//})