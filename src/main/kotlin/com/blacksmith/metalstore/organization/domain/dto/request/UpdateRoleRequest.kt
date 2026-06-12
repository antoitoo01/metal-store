package com.blacksmith.metalstore.organization.domain.dto.request

import com.blacksmith.metalstore.organization.domain.entity.OrganizationRole

data class UpdateRoleRequest(
    val role: OrganizationRole,
)
