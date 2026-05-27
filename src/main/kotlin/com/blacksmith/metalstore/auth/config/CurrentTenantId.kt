package com.blacksmith.metalstore.auth.config

import com.blacksmith.metalstore.auth.exception.InvalidTenantIdException
import com.blacksmith.metalstore.auth.exception.MissingTenantIdException
import com.blacksmith.metalstore.auth.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.annotation.Configuration
import org.springframework.core.MethodParameter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.UUID

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class CurrentTenantId

@Component
class CurrentTenantIdArgumentResolver(
    private val userRepository: UserRepository
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(CurrentTenantId::class.java)

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): UUID {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth != null && auth.isAuthenticated && auth.principal is Jwt) {
            val jwt = auth.principal as Jwt
            val userId = try {
                jwt.subject?.let { UUID.fromString(it) }
            } catch (_: IllegalArgumentException) {
                throw InvalidTenantIdException("Invalid user ID in JWT subject")
            }
            if (userId != null) {
                val user = userRepository.findById(userId).orElse(null)
                if (user != null) return user.tenantId
                throw InvalidTenantIdException("Authenticated user not found in local database")
            }
        }
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java)
            ?: throw MissingTenantIdException()
        val header = request.getHeader("X-Tenant-Id")
            ?: throw MissingTenantIdException()
        return try {
            UUID.fromString(header)
        } catch (_: IllegalArgumentException) {
            throw InvalidTenantIdException("X-Tenant-Id header must be a valid UUID, got: '$header'")
        }
    }
}

@Configuration
class WebConfig(private val resolver: CurrentTenantIdArgumentResolver) : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(resolver)
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            .allowedHeaders("*")
    }
}
