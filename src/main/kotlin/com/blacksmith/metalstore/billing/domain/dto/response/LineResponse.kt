package com.blacksmith.metalstore.billing.domain.dto.response

import com.blacksmith.metalstore.billing.domain.entity.InvoiceLine
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.UUID

data class LineResponse(
    @field:Schema(description = "Identificador único de la línea de factura", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,

    @field:Schema(description = "Identificador de la factura asociada", example = "550e8400-e29b-41d4-a716-446655440001")
    val invoiceId: UUID,

    @field:Schema(description = "Número de línea", example = "1")
    val lineNumber: Int,

    @field:Schema(description = "Identificador del perfil de artículo (opcional)", example = "550e8400-e29b-41d4-a716-446655440002")
    val profileId: UUID?,

    @field:Schema(description = "Identificador del artículo (opcional)", example = "550e8400-e29b-41d4-a716-446655440003")
    val itemId: UUID?,

    @field:Schema(description = "Descripción de la línea", example = "Plancha de acero inoxidable 3mm 2x1m")
    val description: String,

    @field:Schema(description = "Cantidad facturada", example = "10.00")
    val quantity: BigDecimal,

    @field:Schema(description = "Precio unitario sin IVA", example = "125.50")
    val unitPrice: BigDecimal,

    @field:Schema(description = "Porcentaje de IVA", example = "21.00")
    val vatRate: BigDecimal,

    @field:Schema(description = "Precio total de la línea", example = "1255.00")
    val totalPrice: BigDecimal
) {
    companion object {
        fun from(e: InvoiceLine) = LineResponse(
            id = e.id, invoiceId = e.invoiceId, lineNumber = e.lineNumber,
            profileId = e.profileId, itemId = e.itemId, description = e.description,
            quantity = e.quantity, unitPrice = e.unitPrice, vatRate = e.vatRate,
            totalPrice = e.totalPrice
        )
    }
}
