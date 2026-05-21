package com.blacksmith.metalstore.auth.domain.dto.request

import java.util.*

data class UpdateUserRequest(
    val id: UUID,
    val username: String?,
    val email: String,
    val password: String
)
