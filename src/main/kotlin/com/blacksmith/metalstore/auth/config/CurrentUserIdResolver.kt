package com.blacksmith.metalstore.auth.config

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.annotation.Configuration
import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.*

data class CurrentUserInfo(
    val id: UUID,
    val email: String,
)

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class CurrentUserId

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class CurrentUserEmail

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class CurrentUser

@Component
class CurrentUserArgumentResolver : HandlerMethodArgumentResolver {

    private val objectMapper = ObjectMapper()

    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(CurrentUserId::class.java) ||
            parameter.hasParameterAnnotation(CurrentUser::class.java) ||
            parameter.hasParameterAnnotation(CurrentUserEmail::class.java)

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any? {
        val claims = resolveClaims(webRequest) ?: return null

        return when {
            parameter.hasParameterAnnotation(CurrentUserId::class.java) -> {
                val sub = claims["sub"] as? String ?: return null
                try { UUID.fromString(sub) } catch (_: Exception) { null }
            }
            parameter.hasParameterAnnotation(CurrentUserEmail::class.java) -> {
                claims["email"] as? String
            }
            parameter.hasParameterAnnotation(CurrentUser::class.java) -> {
                val sub = claims["sub"] as? String ?: return null
                val id = try { UUID.fromString(sub) } catch (_: Exception) { return null }
                val email = claims["email"] as? String ?: ""
                CurrentUserInfo(id, email)
            }
            else -> null
        }
    }

    private fun resolveClaims(webRequest: NativeWebRequest): Map<String, Any>? {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth != null && auth.isAuthenticated && auth.principal is Jwt) {
            return (auth.principal as Jwt).claims
        }

        val request = webRequest.getNativeRequest(HttpServletRequest::class.java) ?: return null
        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION) ?: return null
        return try {
            val token = authHeader.removePrefix("Bearer ")
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payload = String(Base64.getUrlDecoder().decode(parts[1]))
            objectMapper.readValue(payload, Map::class.java) as? Map<String, Any>
        } catch (_: Exception) {
            null
        }
    }
}

@Configuration
class AuthWebConfig(
    private val currentUserResolver: CurrentUserArgumentResolver,
) : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(currentUserResolver)
    }
}
