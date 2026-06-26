package com.blacksmith.metalstore.inbound.domain.dto.response

import com.blacksmith.metalstore.inbound.domain.entity.InboundDeliveryNoteLine
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.UUID

data class InboundDeliveryNoteLineResponse(
    @field:Schema(description = "ID de la línea")
    val id: UUID,

    @field:Schema(description = "ID del albarán")
    val deliveryNoteId: UUID,

    @field:Schema(description = "Número de línea")
    val lineNumber: Int,

    @field:Schema(description = "ID del perfil del catálogo")
    val profileId: UUID?,

    @field:Schema(description = "ID del ítem del catálogo")
    val itemId: UUID?,

    @field:Schema(description = "Descripción")
    val description: String,

    @field:Schema(description = "Cantidad recibida")
    val quantity: BigDecimal,

    @field:Schema(description = "Precio unitario (€)")
    val unitPrice: BigDecimal,

    @field:Schema(description = "Tipo de IVA (%)")
    val vatRate: BigDecimal,

    @field:Schema(description = "Notas")
    val notes: String?
) {
    companion object {
        fun from(e: InboundDeliveryNoteLine) = InboundDeliveryNoteLineResponse(
            id = e.id,
            deliveryNoteId = e.deliveryNoteId,
            lineNumber = e.lineNumber,
            profileId = e.profileId,
            itemId = e.itemId,
            description = e.description,
            quantity = e.quantity,
            unitPrice = e.unitPrice,
            vatRate = e.vatRate,
            notes = e.notes
        )
    }
}
