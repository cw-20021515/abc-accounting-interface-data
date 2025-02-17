package com.abc.us.accounting.documents.model.deprecated

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "차변_대변_합계_금액")
class DebitCreditAmount(
    var accountCode: String? = null,
    var accountName: String? = null,
    var groupCode: String? = null,
    var level: Int? = null,
    var parentAccountCode: String? = null,
    var debitAmount: Double? = null,
    var creditAmount: Double? = null,
    var isAccount: String? = null
)