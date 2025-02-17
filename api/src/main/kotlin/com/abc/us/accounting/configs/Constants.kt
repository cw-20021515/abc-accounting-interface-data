package com.abc.us.accounting.config

import com.abc.us.accounting.documents.domain.type.CompanyCode
import java.math.BigDecimal
import java.math.MathContext

object Constants {
    const val APP_NAME = "abc-accounting"

    const val ACCOUNTING_PRECISION = 38
    const val ACCOUNTING_SCALE = 2              // 일반 숫자 저장할때는 소숫점 2자리 까지
    const val RATIO_SCALE = 4                    // 비율계산시 유효 자리수는 % * 2자리
    const val PERCENTAGE_SCALE = 2              // % 계산시에는 소숫점 2자리
    const val EXCHANGE_RATE_SCALE = 12          // 환율변환 할때는 소숫점 12자리 까지
    const val QBO_SCALE = 2                     // 퀵북 journal-entry 소수점 2자리까지
    const val MATH_ROUNDING_MODE = "HALF_UP"
    val MATH_CONTEXT = MathContext.DECIMAL128

    val SALES_TAX_TOLERANCE = BigDecimal("0.1")     // +/- 0.1 USD가 단수차 한계

    const val MAX_ATTEMPT = 3
    const val BACKOFF_DELAY = 100L


    const val BASE_YEAR = 2000
    const val HASH_LENGTH = 32


    const val PAGE_SIZE = 100
    const val JPA_BATCH_SIZE = 1000
    const val DOCUMENT_BATCH_SIZE = 100

    val TEST_COMPANY_CODE = CompanyCode.T200
    const val DEFAULT_VERSION:Long = 1
}
