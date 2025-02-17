package com.abc.us.accounting.documents.model.deprecated

import com.abc.us.generated.models.AccountingType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "원장_속성_공통코드_데이터")
class DocumentAttributeCodeData(
    var code: String? = null,
    var name: String? = null,
    var accountType: AccountingType
)