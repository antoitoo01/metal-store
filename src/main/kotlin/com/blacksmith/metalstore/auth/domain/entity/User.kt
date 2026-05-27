package com.blacksmith.metalstore.auth.domain.entity

import com.blacksmith.metalstore.auth.domain.dto.response.UserResponse
import jakarta.persistence.*
import jakarta.validation.constraints.Email
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "users")
data class User(
    @Id
    val id: UUID,

    @Column(nullable = false)
    var tenantId: UUID,

    @Column(nullable = true)
    var username: String?,

    @Email
    @Column(nullable = false, unique = true)
    var email: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role = Role.USER,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: UserState = UserState.INACTIVE,

    @Column(nullable = false, updatable = false)
    var createdDate: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var lastModifiedDate: LocalDateTime = LocalDateTime.now()
) {
    fun toResponse(tenantName: String = "") = UserResponse(
        id = id,
        username = username ?: "",
        email = email,
        role = role,
        tenantId = tenantId,
        tenantName = tenantName
    )
}
