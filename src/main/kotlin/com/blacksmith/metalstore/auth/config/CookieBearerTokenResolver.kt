package com.blacksmith.metalstore.auth.config

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver
import org.springframework.stereotype.Component

@Component
class CookieBearerTokenResolver : BearerTokenResolver {

    override fun resolve(request: HttpServletRequest): String? {
        request.cookies?.firstOrNull { it.name == COOKIE_NAME }?.value?.let { return it }

        val header = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (header != null && header.startsWith("Bearer ")) {
            return header.removePrefix("Bearer ")
        }

        return null
    }

    companion object {
        const val COOKIE_NAME = "access_token"
    }
}
