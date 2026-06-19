package com.blacksmith.metalstore.auth.domain.dto.response

import com.blacksmith.metalstore.auth.domain.entity.Role
import com.blacksmith.metalstore.auth.domain.entity.User
import com.blacksmith.metalstore.organization.domain.entity.OrganizationRole
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class UserResponse(
    @field:Schema(description = "Identificador único del usuario", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,

    @field:Schema(description = "Nombre de usuario", example = "jperez")
    val username: String,

    @field:Schema(description = "Correo electrónico del usuario", example = "jperez@acerosdelnorte.com")
    val email: String,

    @field:Schema(description = "Rol del usuario", example = "ADMIN")
    val role: Role,

    @field:Schema(description = "Identificador de la organización activa", example = "550e8400-e29b-41d4-a716-446655440000")
    val organizationId: UUID,

    @field:Schema(description = "Nombre de la organización activa", example = "Aceros del Norte S.A.")
    val organizationName: String,

    @field:Schema(description = "Organizaciones a las que pertenece el usuario")
    val organizations: List<UserOrganization>,
) {
    companion object {
        fun from(user: User, organizationName: String = "", activeOrgId: UUID = user.organizationId, organizations: List<UserOrganization> = emptyList()) = UserResponse(
            id = user.id,
            username = user.username ?: user.email.substringBefore("@"),
            email = user.email,
            role = user.role,
            organizationId = activeOrgId,
            organizationName = organizationName,
            organizations = organizations,
        )
    }
}

data class UserOrganization(
    @field:Schema(description = "Identificador de la organización", example = "550e8400-e29b-41d4-a716-446655440000")
    val organizationId: UUID,

    @field:Schema(description = "Nombre de la organización", example = "Aceros del Norte S.A.")
    val organizationName: String,

    @field:Schema(description = "Rol del usuario en la organización", example = "OWNER")
    val role: OrganizationRole,
)
