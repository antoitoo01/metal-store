package com.blacksmith.metalstore.inbound.domain.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.util.UUID

data class UpdateInboundDeliveryNoteRequest(
    @field:Schema(description = "ID del proveedor", example = "550e8400-e29b-41d4-a716-446655440000")
    val supplierId: UUID? = null,

    @field:Schema(description = "Nombre del proveedor")
    val supplierName: String? = null,

    @field:Schema(description = "NIF del proveedor", example = "B12345678")
    val supplierVat: String? = null,

    @field:Schema(description = "Dirección del proveedor")
    val supplierAddress: String? = null,

    @field:Schema(description = "ID de la orden de compra asociada")
    val poId: UUID? = null,

    @field:Schema(description = "Número de la orden de compra asociada")
    val poNumber: String? = null,

    @field:Schema(description = "Fecha del albarán", example = "2026-07-15")
    val issueDate: LocalDate? = null,

    @field:Schema(description = "Notas")
    val notes: String? = null
)
