package com.blacksmith.metalstore.auth.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
class MdcFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val forwarded = request.getHeader("X-Forwarded-For")
            val clientIp = if (!forwarded.isNullOrBlank()) forwarded.split(",").first().trim()
            else request.remoteAddr ?: "unknown"

            MDC.put("clientIp", clientIp)
            MDC.put("method", request.method)
            MDC.put("path", request.requestURI)

            filterChain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }
}
