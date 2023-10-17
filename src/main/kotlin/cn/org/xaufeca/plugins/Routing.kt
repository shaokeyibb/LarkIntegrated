package cn.org.xaufeca.plugins

import cn.org.xaufeca.lark.cachedEventDispatcher
import cn.org.xaufeca.lark.larkServletAdapter
import cn.org.xaufeca.toHttpServletRequest
import cn.org.xaufeca.toHttpServletResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.apache.catalina.connector.Response

fun Application.configureRouting() {
    routing {
        post("/webhook/event/{bot_name}") {
            val httpServletResponse = Response().apply {
                coyoteResponse = org.apache.coyote.Response()
            }
            larkServletAdapter.handleEvent(
                call.request.toHttpServletRequest(),
                httpServletResponse,
                cachedEventDispatcher[call.parameters["bot_name"]]
            )
            call.respondTextWriter(
                ContentType.parse(httpServletResponse.contentType),
                HttpStatusCode.fromValue(httpServletResponse.status)
            ) {
                httpServletResponse.writer
            }
        }
    }
}
