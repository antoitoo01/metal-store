package com.blacksmith.metalstore.catalog.domain.dto.response

import com.blacksmith.metalstore.catalog.domain.entity.CatalogFamily
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class CatalogFamilyResponse(
    @field:Schema(description = "Identificador único de la familia")
    val id: UUID,
    @field:Schema(description = "Norma (estándar)")
    val standard: String,
    @field:Schema(description = "Código de la familia")
    val code: String,
    @field:Schema(description = "Tipo de forma")
    val shapeType: String,
    @field:Schema(description = "Descripción")
    val description: String?
) {
    companion object {
        fun from(e: CatalogFamily) = CatalogFamilyResponse(
            id = e.id, standard = e.standard, code = e.code,
            shapeType = e.shapeType, description = e.description
        )
    }
}
