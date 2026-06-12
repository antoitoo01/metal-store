package com.blacksmith.metalstore.auth.service

import com.blacksmith.metalstore.auth.audit.AuditLogger
import com.blacksmith.metalstore.auth.client.SupabaseAuthClient
import com.blacksmith.metalstore.auth.domain.dto.request.RegisterRequest
import com.blacksmith.metalstore.auth.domain.dto.response.LoginResponse
import com.blacksmith.metalstore.auth.domain.dto.response.UserResponse
import com.blacksmith.metalstore.auth.domain.entity.Role
import com.blacksmith.metalstore.auth.domain.entity.User
import com.blacksmith.metalstore.auth.domain.entity.UserState
import com.blacksmith.metalstore.auth.exception.UserNotFoundException
import com.blacksmith.metalstore.auth.repository.UserRepository
import com.blacksmith.metalstore.organization.application.OrganizationService
import com.blacksmith.metalstore.organization.domain.dto.request.CreateOrganizationRequest
import com.blacksmith.metalstore.organization.domain.repository.OrganizationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AuthService(
    private val supabase: SupabaseAuthClient,
    private val userRepository: UserRepository,
    private val organizationRepository: OrganizationRepository,
    private val organizationService: OrganizationService,
    private val audit: AuditLogger
) {
    @Transactional
    fun register(request: RegisterRequest): LoginResponse {
        val supabaseUser = supabase.signUp(request.email, request.password, request.username)
        val supabaseId = UUID.fromString(supabaseUser.getValue("id") as String)
        val email = supabaseUser.getValue("email") as String

        val orgName = request.organizationName
            ?: request.username
            ?: request.email.substringBefore("@")
        val orgResponse = organizationService.createOrganization(supabaseId, CreateOrganizationRequest(orgName))
        val user = User(
            id = supabaseId,
            organizationId = orgResponse.id,
            username = request.username,
            email = email,
            role = Role.ORGANIZATION_OWNER,
            status = UserState.ACTIVE
        )
        userRepository.save(user)

        val login = supabase.signIn(request.email, request.password)

        audit.log(AuditLogger.AuditEvent(
            action = "REGISTER",
            entityType = "User",
            entityId = user.id.toString(),
            organizationId = orgResponse.id.toString(),
            details = mapOf("email" to email, "username" to request.username, "organizationId" to orgResponse.id.toString())
        ))

        return buildLoginResponse(login, user, orgResponse.name)
    }

    @Transactional
    fun login(email: String, password: String): LoginResponse {
        val supabaseResponse = supabase.signIn(email, password)
        @Suppress("UNCHECKED_CAST")
        val supabaseUser = supabaseResponse.getValue("user") as Map<String, Any?>
        val supabaseId = UUID.fromString(supabaseUser.getValue("id") as String)
        val user = userRepository.findById(supabaseId).orElse(null)

        if (user != null) {
            val org = organizationRepository.findById(user.organizationId)
                .orElseThrow { UserNotFoundException("Organization not found") }

            audit.log(AuditLogger.AuditEvent(
                action = "LOGIN_SUCCESS",
                entityType = "User",
                entityId = user.id.toString(),
                organizationId = user.organizationId.toString(),
                details = mapOf("email" to email)
            ))

            return buildLoginResponse(supabaseResponse, user, org.name)
        }

        audit.warn(AuditLogger.AuditEvent(
            action = "LOGIN_AUTO_CREATE",
            entityType = "User",
            details = mapOf("email" to email)
        ))

        val supabaseEmail = supabaseUser.getValue("email") as String
        val supabaseUsername = (supabaseUser["user_metadata"] as? Map<String, Any?>)?.get("username") as? String

        val orgName = supabaseUsername ?: supabaseEmail.substringBefore("@")
        val orgResponse = organizationService.createOrganization(supabaseId, CreateOrganizationRequest(orgName))

        val newUser = User(
            id = supabaseId,
            organizationId = orgResponse.id,
            username = supabaseUsername,
            email = supabaseEmail,
            role = Role.ORGANIZATION_OWNER,
            status = UserState.ACTIVE
        )
        userRepository.save(newUser)

        return buildLoginResponse(supabaseResponse, newUser, orgResponse.name)
    }

    @Transactional
    fun refresh(refreshToken: String): LoginResponse {
        val response = supabase.refreshToken(refreshToken)
        @Suppress("UNCHECKED_CAST")
        val supabaseUser = response.getValue("user") as Map<String, Any?>
        val userId = UUID.fromString(supabaseUser.getValue("id") as String)
        val user = userRepository.findById(userId).orElse(null)

        if (user != null) {
            val org = organizationRepository.findById(user.organizationId)
                .orElseThrow { UserNotFoundException("Organization not found") }
            return buildLoginResponse(response, user, org.name)
        }

        val supabaseEmail = supabaseUser.getValue("email") as String
        val supabaseUsername = (supabaseUser["user_metadata"] as? Map<String, Any?>)?.get("username") as? String

        val orgName = supabaseUsername ?: supabaseEmail.substringBefore("@")
        val orgResponse = organizationService.createOrganization(userId, CreateOrganizationRequest(orgName))

        val newUser = User(
            id = userId,
            organizationId = orgResponse.id,
            username = supabaseUsername,
            email = supabaseEmail,
            role = Role.ORGANIZATION_OWNER,
            status = UserState.ACTIVE
        )
        userRepository.save(newUser)

        return buildLoginResponse(response, newUser, orgResponse.name)
    }

    @Transactional(readOnly = true)
    fun me(userId: UUID): UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException("User not found") }
        val org = organizationRepository.findById(user.organizationId)
            .orElseThrow { UserNotFoundException("Organization not found") }
        return user.toResponse(org.name)
    }

    fun logout(accessToken: String) {
        supabase.signOut(accessToken)
    }

    private fun buildLoginResponse(supabaseResponse: Map<String, Any?>, user: User, organizationName: String): LoginResponse {
        return LoginResponse(
            accessToken = supabaseResponse.getValue("access_token") as String,
            refreshToken = supabaseResponse["refresh_token"] as? String,
            expiresIn = (supabaseResponse["expires_in"] as? Int) ?: 3600,
            email = user.email,
            role = user.role,
            organizationId = user.organizationId,
            organizationName = organizationName
        )
    }
}
