package com.blacksmith.metalstore.outbound.domain.dto.response

import com.blacksmith.metalstore.outbound.domain.entity.OutboundDeliveryNote
import com.blacksmith.metalstore.outbound.domain.entity.OutboundDeliveryNoteStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class OutboundDeliveryNoteResponse(
    @field:Schema(description = "ID del albarán")
    val id: UUID,

    @field:Schema(description = "Número de albarán")
    val number: String,

    @field:Schema(description = "ID del cliente")
    val customerId: UUID?,

    @field:Schema(description = "Nombre del cliente")
    val customerName: String?,

    @field:Schema(description = "NIF del cliente")
    val customerVat: String?,

    @field:Schema(description = "Dirección del cliente")
    val customerAddress: String?,

    @field:Schema(description = "Fecha del albarán")
    val issueDate: LocalDate,

    @field:Schema(description = "Estado")
    val status: OutboundDeliveryNoteStatus,

    @field:Schema(description = "Importe total")
    val totalAmount: BigDecimal,

    @field:Schema(description = "Notas")
    val notes: String?,

    @field:Schema(description = "Fecha de creación")
    val createdAt: LocalDateTime,

    @field:Schema(description = "Fecha de actualización")
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(e: OutboundDeliveryNote) = OutboundDeliveryNoteResponse(
            id = e.id,
            number = e.number,
            customerId = e.customerId,
            customerName = e.customerName,
            customerVat = e.customerVat,
            customerAddress = e.customerAddress,
            issueDate = e.issueDate,
            status = e.status,
            totalAmount = e.totalAmount,
            notes = e.notes,
            createdAt = e.createdAt,
            updatedAt = e.updatedAt
        )
    }
}
