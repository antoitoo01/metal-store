package com.blacksmith.metalstore.organization.config

import com.blacksmith.metalstore.auth.repository.UserRepository
import com.blacksmith.metalstore.organization.domain.entity.MembershipStatus
import com.blacksmith.metalstore.organization.domain.repository.MembershipRepository
import com.blacksmith.metalstore.organization.exception.MissingOrganizationHeaderException
import com.blacksmith.metalstore.organization.exception.InvalidOrganizationIdException
import com.blacksmith.metalstore.organization.exception.NotOrganizationMemberException
import org.springframework.context.annotation.Configuration
import org.springframework.core.MethodParameter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.*
import jakarta.servlet.http.HttpServletRequest

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class CurrentOrganizationId

@Component
class CurrentOrganizationIdArgumentResolver(
    private val userRepository: UserRepository,
    private val membershipRepository: MembershipRepository,
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(CurrentOrganizationId::class.java)

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): UUID {
        val orgId = resolveOrganizationId(webRequest)

        val auth = SecurityContextHolder.getContext().authentication
        if (auth != null && auth.isAuthenticated && auth.principal is Jwt) {
            val jwt = auth.principal as Jwt
            val userId = jwt.subject?.let {
                try { UUID.fromString(it) } catch (_: IllegalArgumentException) { null }
            }
            if (userId != null) {
                val membership = membershipRepository
                    .findByUserIdAndOrganizationIdAndStatus(userId, orgId, MembershipStatus.ACTIVE)
                if (membership == null) throw NotOrganizationMemberException()
            }
        }

        return orgId
    }

    private fun resolveOrganizationId(webRequest: NativeWebRequest): UUID {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth != null && auth.isAuthenticated && auth.principal is Jwt) {
            val jwt = auth.principal as Jwt
            val userId = jwt.subject?.let {
                try { UUID.fromString(it) } catch (_: IllegalArgumentException) { null }
            }
            if (userId != null) {
                val user = userRepository.findById(userId).orElse(null)
                if (user != null) return user.organizationId
            }
        }
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java)
            ?: throw MissingOrganizationHeaderException()
        val header = request.getHeader("X-Organization-Id")
            ?: request.getHeader("X-Tenant-Id")
            ?: throw MissingOrganizationHeaderException()
        return try {
            UUID.fromString(header)
        } catch (_: IllegalArgumentException) {
            throw InvalidOrganizationIdException(header)
        }
    }
}

@Configuration
class OrganizationWebConfig(
    private val resolver: CurrentOrganizationIdArgumentResolver,
    private val roleGuard: RoleGuardInterceptor,
) : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(resolver)
    }

    override fun addInterceptors(registry: org.springframework.web.servlet.config.annotation.InterceptorRegistry) {
        registry.addInterceptor(roleGuard)
    }
}
