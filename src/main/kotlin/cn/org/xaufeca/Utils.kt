package cn.org.xaufeca

import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

private val servletResponseField = ServletApplicationResponse::class.java.getDeclaredField("servletResponse").also {
    it.trySetAccessible()
}

private val servletRequestField = ServletApplicationRequest::class.java.getDeclaredField("servletRequest").also {
    it.trySetAccessible()
}

fun ApplicationResponse.toHttpServletResponse() =
    servletResponseField.get(((call.response as RoutingApplicationResponse).engineResponse as ServletApplicationResponse)) as HttpServletResponse

fun ApplicationRequest.toHttpServletRequest() =
    servletRequestField.get(((call.request as RoutingApplicationRequest).engineRequest as ServletApplicationRequest)) as HttpServletRequest
