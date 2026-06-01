package com.blacksmith.metalstore.client.domain.dto.response

import com.blacksmith.metalstore.client.domain.entity.Client
import com.blacksmith.metalstore.client.domain.entity.ClientStatus
import java.time.LocalDateTime
import java.util.UUID

data class ClientResponse(
    val id: UUID,
    val tenantId: UUID,
    val name: String,
    val email: String?,
    val phone: String?,
    val address: String?,
    val vatNumber: String?,
    val notes: String?,
    val status: ClientStatus,
    val createdDate: LocalDateTime,
    val lastModifiedDate: LocalDateTime
) {
    companion object {
        fun from(e: Client) = ClientResponse(
            id = e.id, tenantId = e.tenantId, name = e.name, email = e.email,
            phone = e.phone, address = e.address, vatNumber = e.vatNumber,
            notes = e.notes, status = e.status, createdDate = e.createdDate,
            lastModifiedDate = e.lastModifiedDate
        )
    }
}
