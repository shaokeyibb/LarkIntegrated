package cn.org.xaufeca

import cn.org.xaufeca.lark.loadLark
import cn.org.xaufeca.plugins.configureRouting
import cn.org.xaufeca.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.tomcat.*

fun main() {
    loadConfiguration()
    loadLark()

    embeddedServer(Tomcat, port = 8089, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureRouting()
}
