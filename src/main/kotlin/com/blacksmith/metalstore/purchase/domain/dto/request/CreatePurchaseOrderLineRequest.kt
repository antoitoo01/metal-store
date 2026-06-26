package com.blacksmith.metalstore.purchase.domain.dto.request

import com.blacksmith.metalstore.purchase.domain.entity.PurchaseOrderLine
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.util.UUID

data class CreatePurchaseOrderLineRequest(
    @field:Schema(description = "Número de línea", example = "1")
    val lineNumber: Int,

    @field:Schema(description = "ID del perfil del catálogo", example = "550e8400-e29b-41d4-a716-446655440000")
    val profileId: UUID? = null,

    @field:Schema(description = "ID del item del catálogo", example = "550e8400-e29b-41d4-a716-446655440001")
    val itemId: UUID? = null,

    @field:Schema(description = "Descripción", example = "Viga HEB 200")
    val description: String,

    @field:Schema(description = "Cantidad", example = "150.0000")
    @field:Positive
    val quantity: BigDecimal,

    @field:Schema(description = "Precio unitario (€)", example = "2.50")
    @field:Positive
    val unitPrice: BigDecimal,

    @field:Schema(description = "Tipo de IVA (%)", example = "21.00")
    val vatRate: BigDecimal = BigDecimal("21.00")
) {
    fun toEntity(poId: UUID) = PurchaseOrderLine(
        poId = poId,
        lineNumber = lineNumber,
        profileId = profileId,
        itemId = itemId,
        description = description,
        quantity = quantity,
        unitPrice = unitPrice,
        vatRate = vatRate,
        totalPrice = BigDecimal.ZERO
    )
}
