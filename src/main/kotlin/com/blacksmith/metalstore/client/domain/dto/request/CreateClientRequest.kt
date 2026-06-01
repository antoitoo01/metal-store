package com.blacksmith.metalstore.client.domain.dto.request

import com.blacksmith.metalstore.client.domain.entity.Client
import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class CreateClientRequest(
    @field:NotBlank
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val vatNumber: String? = null,
    val notes: String? = null
) {
    fun toEntity(tenantId: UUID) = Client(
        tenantId = tenantId,
        name = name,
        email = email,
        phone = phone,
        address = address,
        vatNumber = vatNumber,
        notes = notes
    )
}
