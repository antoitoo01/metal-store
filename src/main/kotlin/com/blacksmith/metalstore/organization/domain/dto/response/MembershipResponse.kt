package com.blacksmith.metalstore.organization.domain.dto.response

import com.blacksmith.metalstore.organization.domain.entity.Membership
import com.blacksmith.metalstore.organization.domain.entity.MembershipStatus
import com.blacksmith.metalstore.organization.domain.entity.OrganizationRole
import java.util.UUID

data class MembershipResponse(
    val id: UUID,
    val userId: UUID,
    val organizationId: UUID,
    val role: OrganizationRole,
    val status: MembershipStatus,
) {
    companion object {
        fun from(m: Membership) = MembershipResponse(
            id = m.id,
            userId = m.userId,
            organizationId = m.organizationId,
            role = m.role,
            status = m.status,
        )
    }
}
