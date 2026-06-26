package com.blacksmith.metalstore.inbound.domain.dto.response

import com.blacksmith.metalstore.inbound.domain.entity.InboundDeliveryNote
import com.blacksmith.metalstore.inbound.domain.entity.InboundDeliveryNoteStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class InboundDeliveryNoteResponse(
    @field:Schema(description = "ID del albarán")
    val id: UUID,

    @field:Schema(description = "Número de albarán")
    val number: String,

    @field:Schema(description = "ID del proveedor")
    val supplierId: UUID?,

    @field:Schema(description = "Nombre del proveedor")
    val supplierName: String?,

    @field:Schema(description = "NIF del proveedor")
    val supplierVat: String?,

    @field:Schema(description = "Dirección del proveedor")
    val supplierAddress: String?,

    @field:Schema(description = "ID de la orden de compra asociada")
    val poId: UUID?,

    @field:Schema(description = "Número de la orden de compra asociada")
    val poNumber: String?,

    @field:Schema(description = "Fecha del albarán")
    val issueDate: LocalDate,

    @field:Schema(description = "Estado")
    val status: InboundDeliveryNoteStatus,

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
        fun from(e: InboundDeliveryNote) = InboundDeliveryNoteResponse(
            id = e.id,
            number = e.number,
            supplierId = e.supplierId,
            supplierName = e.supplierName,
            supplierVat = e.supplierVat,
            supplierAddress = e.supplierAddress,
            poId = e.poId,
            poNumber = e.poNumber,
            issueDate = e.issueDate,
            status = e.status,
            totalAmount = e.totalAmount,
            notes = e.notes,
            createdAt = e.createdAt,
            updatedAt = e.updatedAt
        )
    }
}
