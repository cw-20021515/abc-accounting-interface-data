package com.abc.us.accounting.config

import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionException
import org.springframework.transaction.TransactionStatus

//class ReplicaAwareTransactionManager (
//        private val jpaTransactionManager: JpaTransactionManager,
//        private val dataSource: TransactionRoutingDataSource
//    ) : PlatformTransactionManager {
//
//    @Throws(TransactionException::class)
//    override fun getTransaction(definition: TransactionDefinition?): TransactionStatus {
//        dataSource.setReadonlyDataSource((definition != null) && definition.isReadOnly)
//        return jpaTransactionManager.getTransaction(definition)
//    }
//
//    @Throws(TransactionException::class)
//    override fun commit(status: TransactionStatus) {
//        jpaTransactionManager.commit(status)
//    }
//
//    @Throws(TransactionException::class)
//    override fun rollback(status: TransactionStatus) {
//        jpaTransactionManager.rollback(status)
//    }
//}