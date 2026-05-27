package com.blacksmith.metalstore.auth.domain.dto.response

import com.blacksmith.metalstore.auth.domain.entity.Role
import java.util.UUID

data class LoginResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val refreshToken: String? = null,
    val expiresIn: Int = 3600,
    val email: String,
    val role: Role,
    val tenantId: UUID,
    val tenantName: String
)
