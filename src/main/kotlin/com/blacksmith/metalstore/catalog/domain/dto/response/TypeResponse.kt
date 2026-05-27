package com.blacksmith.metalstore.catalog.domain.dto.response

import com.blacksmith.metalstore.catalog.domain.entity.CatalogItemType
import java.time.LocalDateTime
import java.util.UUID

data class TypeResponse(
    val id: UUID,
    val tenantId: UUID,
    val name: String,
    val description: String?,
    val schemaDefinition: String?,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(e: CatalogItemType) = TypeResponse(
            id = e.id, tenantId = e.tenantId, name = e.name,
            description = e.description, schemaDefinition = e.schemaDefinition,
            createdAt = e.createdAt
        )
    }
}
