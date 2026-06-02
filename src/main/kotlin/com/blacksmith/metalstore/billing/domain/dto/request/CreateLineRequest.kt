package com.blacksmith.metalstore.billing.domain.dto.request

import com.blacksmith.metalstore.billing.domain.entity.InvoiceLine
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.util.UUID

data class CreateLineRequest(
    @field:Schema(description = "Número de línea dentro de la factura", example = "1")
    val lineNumber: Int,

    @field:Schema(description = "Identificador del perfil de artículo (opcional)", example = "550e8400-e29b-41d4-a716-446655440000")
    val profileId: UUID? = null,

    @field:Schema(description = "Identificador del artículo (opcional)", example = "550e8400-e29b-41d4-a716-446655440001")
    val itemId: UUID? = null,

    @field:Schema(description = "Descripción de la línea", example = "Plancha de acero inoxidable 3mm 2x1m")
    val description: String,

    @field:Schema(description = "Cantidad facturada", example = "10.00")
    @field:Positive
    val quantity: BigDecimal,

    @field:Schema(description = "Precio unitario sin IVA", example = "125.50")
    @field:Positive
    val unitPrice: BigDecimal,

    @field:Schema(description = "Porcentaje de IVA aplicado", example = "21.00")
    val vatRate: BigDecimal = BigDecimal("21.00")
) {
    fun toEntity(invoiceId: UUID) = InvoiceLine(
        invoiceId = invoiceId,
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
