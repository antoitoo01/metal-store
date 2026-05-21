package com.blacksmith.metalstore.auth.domain.dto.response

import com.blacksmith.metalstore.auth.domain.entity.Role

data class LoginResponse(
    val accessToken: String = "",
    val tokenType: String = "Bearer",
    val email: String,
    val role: Role
)
