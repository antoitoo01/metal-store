package com.blacksmith.metalstore.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "rate-limiting")
data class RateLimitingProperties(
    val defaultLimit: Long = 100,
    val defaultWindowSeconds: Long = 60,
    val excludedPaths: List<String> = emptyList()
)
