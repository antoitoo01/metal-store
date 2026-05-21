package com.blacksmith.metalstore.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "supabase")
data class SupabaseProperties(
    val url: String = "",
    val publishableKey: String = "",
    val secretKey: String = ""
)
