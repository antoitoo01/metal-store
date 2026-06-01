package com.blacksmith.metalstore.client.domain.dto.request

import com.blacksmith.metalstore.client.domain.entity.Client
import java.util.UUID

data class UpdateClientRequest(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val vatNumber: String? = null,
    val notes: String? = null,
    val status: String? = null
) {
    fun toEntity(tenantId: UUID) = Client(
        tenantId = tenantId,
        name = name ?: "",
        email = email,
        phone = phone,
        address = address,
        vatNumber = vatNumber,
        notes = notes,
        status = status?.let { com.blacksmith.metalstore.client.domain.entity.ClientStatus.valueOf(it) }
            ?: com.blacksmith.metalstore.client.domain.entity.ClientStatus.ACTIVE
    )
}
