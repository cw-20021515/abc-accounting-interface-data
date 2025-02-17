package com.abc.us.accounting.qbo.repository

import com.abc.us.accounting.MockBeanHandler
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

//@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@ImportAutoConfiguration(exclude = [SecurityAutoConfiguration::class, WebMvcAutoConfiguration::class])
//@Import(MockBeanHandler::class)
//@ActiveProfiles("test")
//class CredentialsRepositoryTest(
//    val companyMasterRepository: CompanyMasterRepository,
//    val companyCredentialsRepository : CompanyCredentialsRepository
//)  : FunSpec({
//    test("Credentials repository") {
//        val allMasters = companyMasterRepository.findAll()
//        logger.info("data size:${allMasters.size}")
//
//        allMasters.forEach { master ->
//            companyCredentialsRepository.findCompanyCredentialsByCompanyIdIn()
//        }
//
//    }
//}) {
//    companion object {
//        private val logger = LoggerFactory.getLogger(this::class.java)
//    }
//}