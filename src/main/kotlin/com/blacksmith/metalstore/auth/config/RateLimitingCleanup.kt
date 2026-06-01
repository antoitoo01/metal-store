package com.blacksmith.metalstore.auth.config

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class RateLimitingCleanup(private val filter: RateLimitingFilter) {

    companion object {
        private val log = LoggerFactory.getLogger(RateLimitingCleanup::class.java)
    }

    @Scheduled(fixedRate = 300_000)
    fun cleanup() {
        val count = filter.activeBucketCount()
        filter.clearBuckets()
        if (count > 0) {
            log.debug("Cleared {} rate limiting buckets", count)
        }
    }
}
