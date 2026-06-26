package com.blacksmith.metalstore.purchase.domain.dto.request

import com.blacksmith.metalstore.purchase.domain.entity.PurchaseOrder
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.util.UUID

data class CreatePurchaseOrderRequest(
    @field:Schema(description = "ID del proveedor", example = "550e8400-e29b-41d4-a716-446655440000")
    val supplierId: UUID? = null,

    @field:Schema(description = "Nombre del proveedor", example = "Aceros Inoxidables S.L.")
    val supplierName: String? = null,

    @field:Schema(description = "NIF del proveedor", example = "B12345678")
    val supplierVat: String? = null,

    @field:Schema(description = "Dirección del proveedor")
    val supplierAddress: String? = null,

    @field:Schema(description = "Fecha prevista de recepción", example = "2026-07-15")
    val expectedDate: LocalDate? = null,

    @field:Schema(description = "Notas")
    val notes: String? = null
) {
    fun toEntity(organizationId: UUID, poNumber: String) = PurchaseOrder(
        organizationId = organizationId,
        poNumber = poNumber,
        supplierId = supplierId,
        supplierName = supplierName,
        supplierVat = supplierVat,
        supplierAddress = supplierAddress,
        expectedDate = expectedDate,
        notes = notes
    )
}
