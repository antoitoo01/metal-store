package com.blacksmith.metalstore.auth.domain.dto.request

data class LoginRequest(
    val username: String?,
    val email: String?,
    val password: String
)
