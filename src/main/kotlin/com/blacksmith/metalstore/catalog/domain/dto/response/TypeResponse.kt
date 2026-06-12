package com.blacksmith.metalstore.catalog.domain.dto.response

import com.blacksmith.metalstore.catalog.domain.entity.CatalogItemType
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

data class TypeResponse(
    @field:Schema(description = "Identificador único del tipo de artículo", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,

    @field:Schema(description = "Identificador del organizaci�n", example = "550e8400-e29b-41d4-a716-446655440000")
    val organizationId: UUID,

    @field:Schema(description = "Nombre del tipo de artículo", example = "Plancha de acero")
    val name: String,

    @field:Schema(description = "Descripción del tipo de artículo (opcional)", example = "Planchas de acero laminado en frío")
    val description: String?,

    @field:Schema(description = "Definición del esquema JSON de atributos (opcional)", example = "{\"espesor\":\"number\",\"ancho\":\"number\",\"largo\":\"number\"}")
    val schemaDefinition: String?,

    @field:Schema(description = "Fecha de creación del tipo", example = "2026-01-15T10:30:00")
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(e: CatalogItemType) = TypeResponse(
            id = e.id, organizationId = e.organizationId, name = e.name,
            description = e.description, schemaDefinition = e.schemaDefinition,
            createdAt = e.createdAt
        )
    }
}
