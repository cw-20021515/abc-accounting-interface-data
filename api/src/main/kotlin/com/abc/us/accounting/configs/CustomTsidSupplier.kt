package com.abc.us.accounting.configs

import io.hypersistence.tsid.TSID
import java.util.function.Supplier

// https://vladmihalcea.com/tsid-identifier-jpa-hibernate/
// https://ssdragon.tistory.com/162
class CustomTsidSupplier : Supplier<TSID.Factory> {
    override fun get(): TSID.Factory {
        return TSID.Factory.builder() //                .withNodeBits(1)
            .build()
    }
}
