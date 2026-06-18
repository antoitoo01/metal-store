package com.blacksmith.metalstore.auth.domain.entity

import com.blacksmith.metalstore.auth.domain.dto.response.UserOrganization
import com.blacksmith.metalstore.auth.domain.dto.response.UserResponse
import com.blacksmith.metalstore.shared.BaseEntity
import jakarta.persistence.*
import jakarta.validation.constraints.Email
import java.util.Objects
import java.util.UUID

@Entity
@Table(name = "users")
class User(
    @Id
    val id: UUID,

    @Column(name = "organization_id", nullable = false)
    var organizationId: UUID,

    @Column(nullable = true)
    var username: String?,

    @Email
    @Column(nullable = false, unique = true)
    var email: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role = Role.COMPANY,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: UserState = UserState.INACTIVE
) : BaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as User
        return id == that.id
    }

    override fun hashCode(): Int = Objects.hash(id)
}
