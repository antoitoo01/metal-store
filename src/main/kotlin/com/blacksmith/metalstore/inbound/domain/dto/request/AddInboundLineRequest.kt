package com.blacksmith.metalstore.inbound.domain.dto.request

import com.blacksmith.metalstore.inbound.domain.entity.InboundDeliveryNoteLine
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal
import java.util.UUID

data class AddInboundLineRequest(
    @field:Schema(description = "Número de línea", example = "1")
    val lineNumber: Int,

    @field:Schema(description = "ID del perfil del catálogo")
    val profileId: UUID? = null,

    @field:Schema(description = "ID del ítem del catálogo")
    val itemId: UUID? = null,

    @field:NotBlank
    @field:Schema(description = "Descripción del material", example = "IPN 200 x 6m")
    val description: String,

    @field:Positive
    @field:Schema(description = "Cantidad recibida", example = "10.00")
    val quantity: BigDecimal,

    @field:PositiveOrZero
    @field:Schema(description = "Precio unitario (€)", example = "45.50")
    val unitPrice: BigDecimal = BigDecimal.ZERO,

    @field:Schema(description = "Tipo de IVA (%)", example = "21.00")
    val vatRate: BigDecimal = BigDecimal("21.00"),

    @field:Schema(description = "Notas de la línea")
    val notes: String? = null
) {
    fun toEntity(deliveryNoteId: UUID) = InboundDeliveryNoteLine(
        deliveryNoteId = deliveryNoteId,
        lineNumber = lineNumber,
        profileId = profileId,
        itemId = itemId,
        description = description,
        quantity = quantity,
        unitPrice = unitPrice,
        vatRate = vatRate,
        notes = notes
    )
}
