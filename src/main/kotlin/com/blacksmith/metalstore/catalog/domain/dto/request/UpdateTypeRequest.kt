package com.blacksmith.metalstore.catalog.domain.dto.request

import com.blacksmith.metalstore.catalog.domain.entity.CatalogItemType
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class UpdateTypeRequest(
    @field:Schema(description = "Nombre del tipo de artículo", example = "Viga estructural")
    val name: String,

    @field:Schema(description = "Descripción del tipo de artículo (opcional)", example = "Vigas de acero estructural IPN")
    val description: String? = null,

    @field:Schema(description = "Definición del esquema JSON de atributos (opcional)", example = "{\"perfil\":\"string\",\"longitud\":\"number\",\"peso\":\"number\"}")
    val schemaDefinition: String? = null
) {
    fun toEntity(tenantId: UUID) = CatalogItemType(
        tenantId = tenantId,
        name = name,
        description = description,
        schemaDefinition = schemaDefinition
    )
}
