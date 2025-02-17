package com.abc.us.accounting.collects.trigger.audit

import org.springframework.context.ApplicationEvent

class AsyncEventTrailer private constructor(
    private var publisher : Any,
    private var configure: Builder,
) : ApplicationEvent(publisher) {

    class Builder {

        var queries: MutableMap<String, Any> = mutableMapOf()
        var freights: MutableMap<String, Any> = mutableMapOf()
        var listener : String = ""
        var test : Boolean = false
        var reversing : Boolean = false

        fun freight(id : String, freight : Any ) = apply { this.freights[id] =  freight}
        fun freights(others: MutableMap<String, Any> ) = apply { this.freights =  others}
        fun listener(listener : String) = apply { this.listener =  listener}
        fun test(t : Boolean) = apply { this.test =  t}
        fun reversing(r : Boolean) = apply { this.reversing =  r}
        fun addQuery(key : String, value : Any) = apply { this.queries[key] = value }
        fun addFreight(key : String, value : Any) = apply { this.freights[key] = value }
        fun build(publisher : Any): AsyncEventTrailer {
            return AsyncEventTrailer(publisher,this)
        }
    }


    fun addFreight(id : String, freight : Any) {
        configure.freights[id] = freight
    }
    fun freights() : MutableMap<String, Any> {
        return configure.freights
    }
    fun test() : Boolean {
        return configure.test
    }

    fun reversing() : Boolean {
        return configure.reversing
    }

    fun queries() : MutableMap<String, Any> {
        return configure.queries
    }

    fun listener() : String {
        return configure.listener
    }
}