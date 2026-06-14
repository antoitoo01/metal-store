package com.blacksmith.metalstore.catalog.domain.dto.response

import com.blacksmith.metalstore.catalog.domain.entity.CatalogItem
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class CatalogItemResponse(
    @field:Schema(description = "Identificador único del ítem")
    val id: UUID,
    @field:Schema(description = "Tipo de ítem")
    val itemType: String,
    @field:Schema(description = "Código SKU")
    val sku: String?,
    @field:Schema(description = "Designación del ítem")
    val designation: String,
    @field:Schema(description = "Dimensiones")
    val dimensions: String?,
    @field:Schema(description = "Peso por metro (kg/m)")
    val weightKgM: BigDecimal?,
    @field:Schema(description = "Material")
    val material: String?,
    @field:Schema(description = "Precio estimado por kg")
    val estimatedPriceKg: BigDecimal,
    @field:Schema(description = "Ruta de la imagen")
    val imagePath: String?,
    @field:Schema(description = "Fecha de creación")
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(e: CatalogItem) = CatalogItemResponse(
            id = e.id, itemType = e.itemType, sku = e.sku,
            designation = e.designation, dimensions = e.dimensions,
            weightKgM = e.weightKgM, material = e.material,
            estimatedPriceKg = e.estimatedPriceKg, imagePath = e.imagePath,
            createdAt = e.createdAt
        )
    }
}
