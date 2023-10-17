package cn.org.xaufeca.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import javax.servlet.http.HttpServletRequest

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}
