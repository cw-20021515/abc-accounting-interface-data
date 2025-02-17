package com.abc.us.accounting.supports.pubsub.annotations

@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class AsyncEventListener(val listener: String = "")