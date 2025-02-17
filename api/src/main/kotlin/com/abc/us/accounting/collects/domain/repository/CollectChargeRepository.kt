package com.abc.us.accounting.collects.domain.repository

//import com.abc.us.accounting.collects.domain.entity.collect.CollectChargeItem
//import com.abc.us.accounting.collects.domain.entity.collect.ar.ARLocation
//import com.abc.us.accounting.collects.domain.entity.collect.ar.ARPayment


//@Repository
//interface CollectChargeRepository : JpaRepository<CollectCharge, String> {
//
//    @Query(
//        """
    //    SELECT ar
    //    FROM CollectCharge ar
    //    WHERE ar.chargeId = :chargeId AND ar.isActive = true
//      """
//    )
//    fun findByChargeIdAndIsActive(@Param("chargeId") chargeId: String): Optional<CollectCharge>
//
//    @Query(
//        """
//    SELECT ai
//    FROM CollectChargeItem ai
//    WHERE ai.chargeId = :arId AND ai.isActive = true
//"""
//    )
//    fun findItemsByChargeId(@Param("chargeId") chargeId: String): List<CollectChargeItem>
//
//    @Query(
//        """
//    SELECT ar, ai, pd, tl
//    FROM CollectCharge ar
//    LEFT JOIN CollectChargeItem ai ON ai.chargeId = ar.chargeId
//    LEFT JOIN FETCH ai.embeddablePriceDetail pd
//    LEFT JOIN FETCH pd.embeddableTaxLines tl
//    WHERE ar.isActive = true
//"""
//    )
//    fun findAllActive(): Optional<List<CollectCharge>>
//}
//
//
//    @Query(
//        """
//    SELECT ai
//    FROM CollectChargeItem ai
//    LEFT JOIN FETCH ai.embeddablePriceDetail.embeddableTaxLines tl
//    WHERE ai.chargeId = :arId AND ai.isActive = true
//"""
//    )
//    fun findItemsWithTaxLinesByChargeId(@Param("ChargeId") ChargeId: String): List<CollectChargeItem>
//
//    @Query(
//        """
//    SELECT ai.chargeItemId AS chargeItemId,
//           ai.chargeItemType AS chargeItemType,
//           ai.serviceFlowId AS serviceFlowId,
//           ai.quantity AS quantity,
//           ai.totalPrice AS totalPrice,
//           ai.isTaxExempt AS isTaxExempt,
//           ai.createTime AS createTime,
//           pd.discountPrice AS discountPrice,
//           pd.itemPrice AS itemPrice,
//           pd.prepaidAmount AS prepaidAmount,
//           pd.tax AS tax,
//           pd.currency AS currency,
//           tl.title AS taxLineTitle,
//           tl.rate AS taxLineRate,
//           tl.price AS taxLinePrice
//    FROM CollectChargeItem ai
//    LEFT JOIN ai.embeddablePriceDetail pd
//    LEFT JOIN pd.embeddableTaxLines tl
//    WHERE ai.chargeId = :chargeId AND ai.isActive = true
//"""
//    )
//    fun findFlatItemsByChargeId(@Param("chargeId") arId: String): List<Map<String, Any>>
//
//
//
//    @Query(
//        """
//    SELECT ai
//    FROM CollectChargeItem ai
//    LEFT JOIN FETCH ai.embeddablePriceDetail pd
//    LEFT JOIN FETCH pd.embeddableTaxLines tl
//    WHERE ai.chargeId = :chargeId AND ai.isActive = true
//"""
//    )
//    fun findItemsWithDetailsByChargeId(@Param("chargeId") arId: String): List<CollectChargeItem>
//
//    @Query("SELECT i FROM CollectChargeItem i WHERE i.isActive = true AND i.chargeId = :chargeId")
//    fun findAllByChargeIdAndIsActive(@Param("chargeId") chargeId: String): List<CollectChargeItem>
//
//
//    @Query("SELECT i FROM CollectChargeItem i WHERE i.isActive = true AND i.chargeItemId = :chargeItemId")
//    fun findByChargeItemIdAndIsActive(@Param("chargeItemId") chargeItemId: String): Optional<CollectChargeItem>
//
//    @Query("SELECT i FROM CollectChargeItem i WHERE i.isActive = true")
//    fun findAllActive(): List<CollectChargeItem>
//
//    @Query("SELECT i FROM CollectChargeItem i WHERE i.isActive = true AND i.paymentId = :paymentId")
//    fun findByPaymentId(@Param("paymentId") paymentId: String): List<CollectChargeItem>
//}
//
//@Repository
//interface PaymentRepository : JpaRepository<ARPayment, String> {
//
//    @Query("SELECT p FROM ARPayment p WHERE p.isActive = true AND p.paymentId = :paymentId")
//    fun findByPaymentIdAndIsActive(paymentId: String): Optional<ARPayment>
//
//    @Query("SELECT p FROM ARPayment p WHERE p.isActive = true")
//    fun findAllActive(): List<ARPayment>
//}
//
//@Repository
//interface LocationRepository : JpaRepository<ARLocation, String> {
//
//    @Query("SELECT l FROM ARLocation l WHERE l.isActive = true AND l.hashCode = :hashCode")
//    fun findByHashCodeAndIsActive(hashCode: String): Optional<ARLocation>
//
//    @Query("SELECT l FROM ARLocation l WHERE l.isActive = true")
//    fun findAllActive(): List<ARLocation>
//}
//
