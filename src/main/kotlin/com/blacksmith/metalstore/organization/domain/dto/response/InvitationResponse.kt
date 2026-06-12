package com.blacksmith.metalstore.organization.domain.dto.response

import com.blacksmith.metalstore.organization.domain.entity.Invitation
import com.blacksmith.metalstore.organization.domain.entity.InvitationStatus
import com.blacksmith.metalstore.organization.domain.entity.OrganizationRole
import java.util.UUID

data class InvitationResponse(
    val id: UUID,
    val organizationId: UUID,
    val email: String,
    val role: OrganizationRole,
    val status: InvitationStatus,
    val token: UUID? = null,
) {
    companion object {
        fun from(inv: Invitation, includeToken: Boolean = false) = InvitationResponse(
            id = inv.id,
            organizationId = inv.organizationId,
            email = inv.email,
            role = inv.role,
            status = inv.status,
            token = if (includeToken) inv.token else null,
        )
    }
}
