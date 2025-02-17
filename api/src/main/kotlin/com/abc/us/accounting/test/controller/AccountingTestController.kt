package com.abc.us.accounting.test.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/accounting/v1/test")
class AccountingTestController {
    @RequestMapping("")
    fun main(model: Model): String {
        return "test/main"
    }
}