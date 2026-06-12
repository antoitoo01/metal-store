package com.blacksmith.metalstore.organization.domain.dto.response

import com.blacksmith.metalstore.organization.domain.entity.Organization
import java.util.UUID

data class OrganizationResponse(
    val id: UUID,
    val name: String,
    val slug: String,
    val memberCount: Int = 0,
) {
    companion object {
        fun from(org: Organization, memberCount: Int = 0) = OrganizationResponse(
            id = org.id,
            name = org.name,
            slug = org.slug,
            memberCount = memberCount,
        )
    }
}
