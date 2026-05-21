package com.blacksmith.metalstore.auth.domain.entity

import jakarta.persistence.*
import jakarta.validation.constraints.Email
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "users")
data class User(
    @Id
    @Column(name = "id")
    val id: UUID,

    @Column(nullable = true)
    var username: String?,

    @Email
    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    var role: Role = Role.USER,

    @Column(nullable = false)
    var status: UserState = UserState.INACTIVE,

    @Column(nullable = false, updatable = false)
    var createdDate: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var lastModifiedDate: LocalDateTime = LocalDateTime.now()
)
