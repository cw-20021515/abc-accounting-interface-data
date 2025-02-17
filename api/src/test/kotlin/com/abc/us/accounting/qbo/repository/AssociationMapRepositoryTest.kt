//package com.abc.us.accounting.qbo.repository
//
//import com.abc.us.accounting.qbo.domain.entity.associate.AssociationMap
//import io.mockk.every
//import io.mockk.mockk
//import org.junit.jupiter.api.Assertions.assertEquals
//import org.junit.jupiter.api.Assertions.assertTrue
//import org.junit.jupiter.api.Test
//import org.springframework.data.domain.Example
//import java.util.*
//
//class AssociationMapRepositoryTest {
//
//    private val repository: AssociationMapRepository = mockk()
//
//    @Test
//    fun `should return true if entity exists`() {
//        // Arrange
//        val realmId = "testRealmId"
//        val associatedId = "testAssociatedId"
//        val associatedEntity = "testEntity"
//
//        every {
//            repository.existsEntity(realmId, associatedId, associatedEntity)
//        } returns true
//
//        // Act
//        val exists = repository.existsEntity(realmId, associatedId, associatedEntity)
//
//        // Assert
//        assertTrue(exists, "Expected entity to exist but it does not.")
//    }
//
//    @Test
//    fun `should return entity if it exists`() {
//        // Arrange
//        val realmId = "testRealmId"
//        val associatedEntity = "testEntity"
//        val associatedValue = "testValue"
//
//        val expectedEntity = AssociationMap().apply {
//            id = "testId"
//            this.realmId = realmId
//            this.associatedEntity = associatedEntity
//            this.associatedValue = associatedValue
//            this.isActive = true
//        }
//
//        every {
//            repository.findEntity(realmId, associatedEntity, associatedValue)
//        } returns Optional.of(expectedEntity)
//
//        // Act
//        val result = repository.findEntity(realmId, associatedEntity, associatedValue)
//
//        // Assert
//        assertTrue(result.isPresent, "Expected entity to be present but it is not.")
//        assertEquals(expectedEntity, result.get(), "The returned entity does not match the expected one.")
//    }
//
//    @Test
//    fun `should count active entities by company code and associated entity`() {
//        // Arrange
//        val companyCode = "testCompanyCode"
//        val associatedEntity = "testEntity"
//        val expectedCount = 5L
//
//        every {
//            repository.countByCompanyCodeAndAssociatedEntityAndActiveIsTrue(companyCode, associatedEntity)
//        } returns expectedCount
//
//        // Act
//        val count = repository.countByCompanyCodeAndAssociatedEntityAndActiveIsTrue(companyCode, associatedEntity)
//
//        // Assert
//        assertEquals(expectedCount, count, "The returned count does not match the expected count.")
//    }
//}
