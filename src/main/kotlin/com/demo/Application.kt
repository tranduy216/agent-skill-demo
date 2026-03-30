package com.demo

import com.demo.plugins.*
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    configureDatabase()
    configureSecurity()
    configureHTTP()
    configureRouting()
}
