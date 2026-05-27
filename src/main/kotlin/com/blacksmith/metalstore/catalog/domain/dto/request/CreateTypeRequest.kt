package com.blacksmith.metalstore.catalog.domain.dto.request

import com.blacksmith.metalstore.catalog.domain.entity.CatalogItemType
import java.util.UUID

data class CreateTypeRequest(
    val name: String,
    val description: String? = null,
    val schemaDefinition: String? = null
) {
    fun toEntity(tenantId: UUID) = CatalogItemType(
        tenantId = tenantId,
        name = name,
        description = description,
        schemaDefinition = schemaDefinition
    )
}
