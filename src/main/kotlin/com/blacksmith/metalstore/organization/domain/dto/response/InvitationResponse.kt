package com.blacksmith.metalstore.organization.domain.dto.response

import com.blacksmith.metalstore.organization.domain.entity.Invitation
import com.blacksmith.metalstore.organization.domain.entity.InvitationStatus
import com.blacksmith.metalstore.organization.domain.entity.OrganizationRole
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

data class InvitationResponse(
    val id: UUID,
    val organizationId: UUID,
    val organizationName: String,
    val email: String,
    val role: OrganizationRole,
    val status: InvitationStatus,
    val token: String,
    val link: String,
    val expiresAt: Instant,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(inv: Invitation, orgName: String, baseUrl: String) = InvitationResponse(
            id = inv.id,
            organizationId = inv.organizationId,
            organizationName = orgName,
            email = inv.email,
            role = inv.role,
            status = inv.status,
            token = inv.token,
            link = "$baseUrl/invitations/accept?token=${inv.token}",
            expiresAt = inv.expiresAt,
            createdAt = inv.createdAt,
        )
    }
}
