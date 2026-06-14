package com.blacksmith.metalstore.auth.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
class CorsConfig {

    @Bean
    @Profile("dev", "test")
    fun devCorsFilter(): CorsFilter {
        val config = CorsConfiguration().apply {
            addAllowedOrigin("http://localhost:4200")
            addAllowedOrigin("http://127.0.0.1:4200")
            allowCredentials = true
            addAllowedHeader("*")
            addAllowedMethod("*")
        }
        val source = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
        return CorsFilter(source)
    }

    @Bean
    @Profile("prod")
    fun prodCorsFilter(
        @Value("\${cors.allowed-origins}") allowedOrigins: String
    ): CorsFilter {
        val origins = allowedOrigins.split(",").map { it.trim() }
        val config = CorsConfiguration().apply {
            origins.forEach { addAllowedOrigin(it) }
            allowCredentials = true
            addAllowedHeader("*")
            addAllowedMethod("*")
        }
        val source = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
        return CorsFilter(source)
    }
}
