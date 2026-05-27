package com.blacksmith.metalstore.auth.controller

import com.blacksmith.metalstore.auth.domain.dto.request.UpdateUserRequest
import com.blacksmith.metalstore.auth.domain.dto.response.UserResponse
import com.blacksmith.metalstore.auth.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): ResponseEntity<UserResponse> {
        val user = userService.findById(id)
        return ResponseEntity.ok(user.toResponse())
    }

    @PutMapping
    fun update(
        @Valid @RequestBody request: UpdateUserRequest,
        @AuthenticationPrincipal jwt: Jwt?
    ): ResponseEntity<UserResponse> {
        val authenticatedId = jwt?.subject?.let { UUID.fromString(it) }
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val user = userService.update(request, authenticatedId)
        return ResponseEntity.ok(user.toResponse())
    }

    @DeleteMapping("/{id}")
    fun deleteById(
        @PathVariable id: UUID,
        @AuthenticationPrincipal jwt: Jwt?
    ): ResponseEntity<Void> {
        val authenticatedId = jwt?.subject?.let { UUID.fromString(it) }
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        userService.deleteById(id, authenticatedId)
        return ResponseEntity.noContent().build()
    }
}
