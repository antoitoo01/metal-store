package com.blacksmith.metalstore.auth.service

import com.blacksmith.metalstore.auth.client.SupabaseAuthClient
import com.blacksmith.metalstore.auth.domain.dto.request.RegisterRequest
import com.blacksmith.metalstore.auth.domain.dto.response.LoginResponse
import com.blacksmith.metalstore.auth.domain.dto.response.UserResponse
import com.blacksmith.metalstore.auth.domain.entity.Role
import com.blacksmith.metalstore.auth.domain.entity.Tenant
import com.blacksmith.metalstore.auth.domain.entity.User
import com.blacksmith.metalstore.auth.domain.entity.UserState
import com.blacksmith.metalstore.auth.exception.UserNotFoundException
import com.blacksmith.metalstore.auth.repository.TenantRepository
import com.blacksmith.metalstore.auth.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class AuthService(
    private val supabase: SupabaseAuthClient,
    private val userRepository: UserRepository,
    private val tenantRepository: TenantRepository
) {
    @Transactional
    fun register(request: RegisterRequest): LoginResponse {
        val supabaseUser = supabase.signUp(request.email, request.password, request.username)
        val supabaseId = UUID.fromString(supabaseUser.getValue("id") as String)
        val email = supabaseUser.getValue("email") as String

        val tenant = createTenant(request)
        val user = User(
            id = supabaseId,
            tenantId = tenant.id,
            username = request.username,
            email = email,
            role = Role.TENANT_OWNER,
            status = UserState.ACTIVE,
            createdDate = LocalDateTime.now(),
            lastModifiedDate = LocalDateTime.now()
        )
        userRepository.save(user)

        val login = supabase.signIn(request.email, request.password)
        return buildLoginResponse(login, user, tenant.name)
    }

    @Transactional(readOnly = true)
    fun login(email: String, password: String): LoginResponse {
        val supabaseResponse = supabase.signIn(email, password)
        @Suppress("UNCHECKED_CAST")
        val supabaseUser = supabaseResponse.getValue("user") as Map<String, Any?>
        val supabaseId = UUID.fromString(supabaseUser.getValue("id") as String)
        val user = userRepository.findById(supabaseId).orElse(null)

        return if (user != null) {
            val tenant = tenantRepository.findById(user.tenantId)
                .orElseThrow { UserNotFoundException("Tenant not found") }
            buildLoginResponse(supabaseResponse, user, tenant.name)
        } else {
            LoginResponse(
                accessToken = supabaseResponse.getValue("access_token") as String,
                refreshToken = supabaseResponse["refresh_token"] as? String,
                expiresIn = (supabaseResponse["expires_in"] as? Int) ?: 3600,
                email = email,
                role = Role.USER,
                tenantId = UUID(0, 0),
                tenantName = ""
            )
        }
    }

    @Transactional(readOnly = true)
    fun refresh(refreshToken: String): LoginResponse {
        val response = supabase.refreshToken(refreshToken)
        @Suppress("UNCHECKED_CAST")
        val supabaseUser = response.getValue("user") as Map<String, Any?>
        val userId = UUID.fromString(supabaseUser.getValue("id") as String)
        val user = userRepository.findById(userId).orElse(null)

        return if (user != null) {
            val tenant = tenantRepository.findById(user.tenantId)
                .orElseThrow { UserNotFoundException("Tenant not found") }
            buildLoginResponse(response, user, tenant.name)
        } else {
            LoginResponse(
                accessToken = response.getValue("access_token") as String,
                refreshToken = response["refresh_token"] as? String,
                expiresIn = (response["expires_in"] as? Int) ?: 3600,
                email = supabaseUser.getValue("email") as String,
                role = Role.USER,
                tenantId = UUID(0, 0),
                tenantName = ""
            )
        }
    }

    @Transactional(readOnly = true)
    fun me(userId: UUID): UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException("User not found") }
        val tenant = tenantRepository.findById(user.tenantId)
            .orElseThrow { UserNotFoundException("Tenant not found") }
        return user.toResponse(tenant.name)
    }

    fun logout(accessToken: String) {
        supabase.signOut(accessToken)
    }

    private fun createTenant(request: RegisterRequest): Tenant {
        val name = request.tenantName
            ?: request.username
            ?: request.email.substringBefore("@")
        val slug = generateUniqueSlug(name)
        val tenant = Tenant(
            name = name,
            slug = slug,
            createdDate = LocalDateTime.now(),
            lastModifiedDate = LocalDateTime.now()
        )
        return tenantRepository.save(tenant)
    }

    private fun generateUniqueSlug(name: String): String {
        val base = name.lowercase()
            .replace(Regex("[^a-z0-9\\s-]"), "")
            .replace(Regex("\\s+"), "-")
            .trim('-')
            .take(50)
        if (base.isBlank()) return "tenant-${UUID.randomUUID().toString().take(8)}"
        var slug = base
        var counter = 1
        while (tenantRepository.existsBySlug(slug)) {
            slug = "$base-$counter"
            counter++
        }
        return slug
    }

    private fun buildLoginResponse(supabaseResponse: Map<String, Any?>, user: User, tenantName: String): LoginResponse {
        return LoginResponse(
            accessToken = supabaseResponse.getValue("access_token") as String,
            refreshToken = supabaseResponse["refresh_token"] as? String,
            expiresIn = (supabaseResponse["expires_in"] as? Int) ?: 3600,
            email = user.email,
            role = user.role,
            tenantId = user.tenantId,
            tenantName = tenantName
        )
    }
}
