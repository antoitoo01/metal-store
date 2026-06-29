package com.blacksmith.metalstore.auth.config

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.ConsumptionProbe
import io.github.bucket4j.Refill
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(name = ["rate-limiting.enabled"], havingValue = "true", matchIfMissing = true)
class RateLimitingFilter(
    private val properties: RateLimitingProperties
) : OncePerRequestFilter() {

    companion object {
        private val log = LoggerFactory.getLogger(RateLimitingFilter::class.java)
    }

    private val buckets = ConcurrentHashMap<String, Bucket>()

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return properties.excludedPaths.any { path.startsWith(it) }
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val clientIp = resolveClientIp(request)
        val bucket = buckets.computeIfAbsent(clientIp) { createBucket() }
        val probe: ConsumptionProbe = bucket.tryConsumeAndReturnRemaining(1)

        if (probe.isConsumed) {
            filterChain.doFilter(request, response)
        } else {
            log.warn("Rate limit exceeded for IP: {}", clientIp)
            response.status = 429
            response.setHeader("Retry-After", (probe.nanosToWaitForRefill / 1_000_000_000).toString())
            response.contentType = "application/problem+json"
            response.writer.write(
                """{"type":"about:blank","title":"Too Many Requests","status":429,"detail":"Rate limit exceeded. Try again later."}"""
            )
        }
    }

    private fun resolveClientIp(request: HttpServletRequest): String {
        val forwarded = request.getHeader("X-Forwarded-For")
        if (!forwarded.isNullOrBlank()) {
            return forwarded.split(",").first().trim()
        }
        return request.remoteAddr ?: "unknown"
    }

    private fun createBucket(): Bucket {
        val refill = Refill.greedy(properties.defaultLimit, Duration.ofSeconds(properties.defaultWindowSeconds))
        val limit = Bandwidth.classic(properties.defaultLimit, refill)
        return Bucket.builder().addLimit(limit).build()
    }

    fun clearBuckets() {
        buckets.clear()
    }

    fun activeBucketCount(): Int = buckets.size
}
