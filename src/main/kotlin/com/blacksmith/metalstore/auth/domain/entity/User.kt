package com.blacksmith.metalstore.auth.domain.entity

import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = true)
    var username: String?,

    @NotBlank
    @Column(nullable = false)
    var password: String,

    @Email
    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    var role: Role = Role.USER,

    @Column(nullable = false)
    var status: UserState = UserState.INACTIVE,

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    var createdDate: LocalDateTime? = null,

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    var lastModifiedDate: LocalDateTime? = null
)
