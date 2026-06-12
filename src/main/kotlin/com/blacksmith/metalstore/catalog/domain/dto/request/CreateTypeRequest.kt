package com.blacksmith.metalstore.catalog.domain.dto.request

import com.blacksmith.metalstore.catalog.domain.entity.CatalogItemType
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class CreateTypeRequest(
    @field:Schema(description = "Nombre del tipo de artículo", example = "Plancha de acero")
    val name: String,

    @field:Schema(description = "Descripción del tipo de artículo (opcional)", example = "Planchas de acero laminado en frío")
    val description: String? = null,

    @field:Schema(description = "Definición del esquema JSON de atributos (opcional)", example = "{\"espesor\":\"number\",\"ancho\":\"number\",\"largo\":\"number\"}")
    val schemaDefinition: String? = null
) {
    fun toEntity(organizationId: UUID) = CatalogItemType(
        organizationId = organizationId,
        name = name,
        description = description,
        schemaDefinition = schemaDefinition
    )
}
