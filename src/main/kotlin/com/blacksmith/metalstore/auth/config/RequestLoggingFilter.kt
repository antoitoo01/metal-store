package com.blacksmith.metalstore.auth.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingResponseWrapper

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
class RequestLoggingFilter : OncePerRequestFilter() {

    companion object {
        private val log = LoggerFactory.getLogger(RequestLoggingFilter::class.java)
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val start = System.currentTimeMillis()
        val wrappedResponse = ContentCachingResponseWrapper(response)

        try {
            filterChain.doFilter(request, wrappedResponse)
        } finally {
            val duration = System.currentTimeMillis() - start
            val status = wrappedResponse.status
            val query = request.queryString?.let { "?$it" } ?: ""

            log.info(
                "{} {} {} {}ms",
                request.method, request.requestURI + query, status, duration
            )

            wrappedResponse.copyBodyToResponse()
        }
    }
}
