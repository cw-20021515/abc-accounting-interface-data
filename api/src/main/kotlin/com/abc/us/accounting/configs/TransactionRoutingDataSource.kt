package com.abc.us.accounting.config

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import javax.sql.DataSource
//
//class TransactionRoutingDataSource (mainDataSource: DataSource, replicaDataSource: DataSource) : AbstractRoutingDataSource() {
//    private val currentDataSource = ThreadLocal<DataSourceType>().apply { this.set(DataSourceType.MAIN) }
//
//    init {
//        setTargetDataSources(
//            mapOf(
//                DataSourceType.MAIN to mainDataSource,
//                DataSourceType.REPLICA to replicaDataSource,
//            ),
//        )
//        setDefaultTargetDataSource(mainDataSource)
//    }
//
//    fun setReadonlyDataSource(isReadonly: Boolean) {
//        currentDataSource.set(if (isReadonly) DataSourceType.REPLICA else DataSourceType.MAIN)
//    }
//
//    override fun determineCurrentLookupKey(): Any? = currentDataSource.get()
//}
//
//enum class DataSourceType {
//    MAIN,
//    REPLICA
//}