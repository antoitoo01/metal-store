package com.blacksmith.metalstore.auth.service

import com.blacksmith.metalstore.auth.domain.dto.request.UpdateUserRequest
import com.blacksmith.metalstore.auth.domain.entity.User
import com.blacksmith.metalstore.auth.exception.UserAlreadyExistsException
import com.blacksmith.metalstore.auth.exception.UserNotFoundException
import com.blacksmith.metalstore.auth.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository
) {
    fun findById(id: UUID): User {
        return userRepository.findById(id)
            .orElseThrow { UserNotFoundException("Usuario con id $id no encontrado") }
    }

    fun findByUsername(username: String): User {
        return userRepository.findByUsername(username)
            .orElseThrow { UserNotFoundException("Usuario $username no encontrado") }
    }

    fun update(request: UpdateUserRequest): User {
        val user = findById(request.id)

        request.username?.takeIf { it.isNotBlank() }?.let { username ->
            if (username != user.username && userRepository.existsByUsername(username)) {
                throw UserAlreadyExistsException("El usuario $username ya existe")
            }
            user.username = username
        }

        request.email.takeIf { it.isNotBlank() }?.let { email ->
            if (email != user.email && userRepository.existsByEmail(email)) {
                throw UserAlreadyExistsException("El email $email ya está en uso")
            }
            user.email = email
        }

        return userRepository.save(user)
    }

    fun deleteById(id: UUID) {
        userRepository.deleteById(id)
    }

    fun delete(user: User) {
        userRepository.delete(user)
    }
}
