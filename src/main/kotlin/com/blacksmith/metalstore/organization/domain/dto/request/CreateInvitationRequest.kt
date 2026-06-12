package com.blacksmith.metalstore.organization.domain.dto.request

import com.blacksmith.metalstore.organization.domain.entity.OrganizationRole

data class CreateInvitationRequest(
    val email: String,
    val role: OrganizationRole,
)
