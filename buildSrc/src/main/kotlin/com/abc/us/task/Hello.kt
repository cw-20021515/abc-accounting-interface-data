//package com.abc.us.task
//
//import org.gradle.api.DefaultTask
//import org.gradle.api.provider.Property
//import org.gradle.api.tasks.Input
//import org.gradle.api.tasks.TaskAction
//
//abstract class Hello : DefaultTask() {
//    @get:Input
//    abstract val username: Property<String>
//
//    @TaskAction
//    fun sayHello() {
//        println("hello ${username.get()}")
//    }
//}
