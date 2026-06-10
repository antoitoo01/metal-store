package com.blacksmith.metalstore.auth.service

import com.blacksmith.metalstore.auth.client.SupabaseAuthClient
import com.blacksmith.metalstore.auth.domain.dto.request.UpdateUserRequest
import com.blacksmith.metalstore.auth.domain.entity.Role
import com.blacksmith.metalstore.auth.domain.entity.User
import com.blacksmith.metalstore.auth.exception.UserAlreadyExistsException
import com.blacksmith.metalstore.auth.exception.UserNotFoundException
import com.blacksmith.metalstore.auth.repository.UserRepository
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val supabase: SupabaseAuthClient
) {
    @Transactional(readOnly = true)
    fun findAll(tenantId: UUID, pageable: Pageable, q: String? = null): Page<User> =
        if (q.isNullOrBlank()) userRepository.findByTenantId(tenantId, pageable)
        else userRepository.findByTenantIdAndUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(tenantId, q, q, pageable)

    @Transactional(readOnly = true)
    fun findById(id: UUID): User {
        return userRepository.findById(id)
            .orElseThrow { UserNotFoundException("Usuario con id $id no encontrado") }
    }

    @Transactional(readOnly = true)
    fun findByUsername(username: String): User {
        return userRepository.findByUsername(username)
            .orElseThrow { UserNotFoundException("Usuario $username no encontrado") }
    }

    @Transactional(readOnly = true)
    fun verifyOwnerOrAdmin(targetId: UUID, authenticatedId: UUID) {
        if (targetId == authenticatedId) return
        val currentUser = userRepository.findById(authenticatedId)
            .orElseThrow { UserNotFoundException("Usuario autenticado no encontrado") }
        if (currentUser.role != Role.ADMIN && currentUser.role != Role.TENANT_OWNER) {
            throw AccessDeniedException("No tienes permisos para realizar esta operación")
        }
    }

    @Transactional
    fun update(request: UpdateUserRequest, authenticatedId: UUID): User {
        verifyOwnerOrAdmin(request.id, authenticatedId)
        val user = findById(request.id)

        request.username?.takeIf { it.isNotBlank() }?.let { username ->
            if (username != user.username && userRepository.existsByUsername(username)) {
                throw UserAlreadyExistsException("El usuario $username ya existe")
            }
            user.username = username
        }

        request.email.takeIf { it.isNotBlank() }?.let { email ->
            if (email != user.email) {
                if (userRepository.existsByEmail(email)) {
                    throw UserAlreadyExistsException("El email $email ya está en uso")
                }
                // Sync with Supabase Auth first
                supabase.updateUserEmail(request.id, email)
                user.email = email
            }
        }

        return userRepository.save(user)
    }

    @Transactional
    fun deleteById(id: UUID, authenticatedId: UUID) {
        verifyOwnerOrAdmin(id, authenticatedId)
        // Sync delete with Supabase Auth first
        supabase.deleteUser(id)
        userRepository.deleteById(id)
    }

    @Transactional
    fun delete(user: User, authenticatedId: UUID) {
        verifyOwnerOrAdmin(user.id, authenticatedId)
        supabase.deleteUser(user.id)
        userRepository.delete(user)
    }
}
