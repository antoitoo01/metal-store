package com.blacksmith.metalstore.auth.domain.entity

import com.blacksmith.metalstore.auth.domain.dto.response.UserResponse
import com.blacksmith.metalstore.shared.BaseEntity
import jakarta.persistence.*
import jakarta.validation.constraints.Email
import java.util.UUID

@Entity
@Table(name = "users")
data class User(
    @Id
    val id: UUID,

    @Column(nullable = false)
    var organizationId: UUID,

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
    var status: UserState = UserState.INACTIVE
) : BaseEntity() {
    fun toResponse(organizationName: String = "") = UserResponse(
        id = id,
        username = username ?: "",
        email = email,
        role = role,
        organizationId = organizationId,
        organizationName = organizationName
    )
}
