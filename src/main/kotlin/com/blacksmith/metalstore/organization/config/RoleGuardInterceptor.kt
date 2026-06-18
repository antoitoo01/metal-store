package com.blacksmith.metalstore.organization.config

import com.blacksmith.metalstore.auth.repository.UserRepository
import com.blacksmith.metalstore.organization.domain.entity.MembershipStatus
import com.blacksmith.metalstore.organization.domain.repository.MembershipRepository
import com.blacksmith.metalstore.organization.exception.RoleRequiredException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import java.util.UUID

@Component
class RoleGuardInterceptor(
    private val membershipRepository: MembershipRepository,
    private val userRepository: UserRepository,
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler !is HandlerMethod) return true
        val annotation = handler.getMethodAnnotation(RequiresRole::class.java)
            ?: handler.beanType.getAnnotation(RequiresRole::class.java)
            ?: return true

        val requiredRole = annotation.value

        val auth = SecurityContextHolder.getContext().authentication
        if (auth == null || !auth.isAuthenticated || auth.principal !is Jwt) return true
        val userId = (auth.principal as Jwt).subject?.let {
            try { UUID.fromString(it) } catch (_: IllegalArgumentException) { null }
        } ?: throw RoleRequiredException(requiredRole.name)

        val orgId = if (annotation.orgIdFromArg >= 0) {
            extractOrgIdFromPath(request, annotation.orgIdFromArg) ?: throw RoleRequiredException(requiredRole.name)
        } else {
            val user = userRepository.findById(userId).orElse(null)
                ?: throw RoleRequiredException(requiredRole.name)
            user.organizationId
        }

        val membership = membershipRepository.findByUserIdAndOrganizationIdAndStatus(userId, orgId, MembershipStatus.ACTIVE)
            ?: throw RoleRequiredException(requiredRole.name)

        if (membership.role.ordinal > requiredRole.ordinal) {
            throw RoleRequiredException(requiredRole.name)
        }

        return true
    }

    private fun extractOrgIdFromPath(request: HttpServletRequest, pathIndex: Int): UUID? {
        val path = request.requestURI.removePrefix(request.contextPath).trim('/').split('/')
        return path.getOrNull(pathIndex)?.let {
            try { UUID.fromString(it) } catch (_: IllegalArgumentException) { null }
        }
    }
}
