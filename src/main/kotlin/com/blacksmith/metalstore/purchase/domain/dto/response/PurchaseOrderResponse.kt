package com.blacksmith.metalstore.purchase.domain.dto.response

import com.blacksmith.metalstore.purchase.domain.entity.PurchaseOrder
import com.blacksmith.metalstore.purchase.domain.entity.PurchaseOrderStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class PurchaseOrderResponse(
    @field:Schema(description = "ID de la orden de compra")
    val id: UUID,

    @field:Schema(description = "ID de la organización")
    val organizationId: UUID,

    @field:Schema(description = "Número de orden de compra")
    val poNumber: String,

    @field:Schema(description = "ID del proveedor")
    val supplierId: UUID?,

    @field:Schema(description = "Nombre del proveedor")
    val supplierName: String?,

    @field:Schema(description = "NIF del proveedor")
    val supplierVat: String?,

    @field:Schema(description = "Dirección del proveedor")
    val supplierAddress: String?,

    @field:Schema(description = "Fecha de emisión")
    val issueDate: LocalDate,

    @field:Schema(description = "Fecha prevista de recepción")
    val expectedDate: LocalDate?,

    @field:Schema(description = "Estado")
    val status: PurchaseOrderStatus,

    @field:Schema(description = "Base imponible")
    val subtotal: BigDecimal,

    @field:Schema(description = "Total IVA")
    val vatTotal: BigDecimal,

    @field:Schema(description = "Total")
    val total: BigDecimal,

    @field:Schema(description = "Notas")
    val notes: String?,

    @field:Schema(description = "Fecha de creación")
    val createdAt: LocalDateTime,

    @field:Schema(description = "Fecha de actualización")
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(e: PurchaseOrder) = PurchaseOrderResponse(
            id = e.id,
            organizationId = e.organizationId,
            poNumber = e.poNumber,
            supplierId = e.supplierId,
            supplierName = e.supplierName,
            supplierVat = e.supplierVat,
            supplierAddress = e.supplierAddress,
            issueDate = e.issueDate,
            expectedDate = e.expectedDate,
            status = e.status,
            subtotal = e.subtotal,
            vatTotal = e.vatTotal,
            total = e.total,
            notes = e.notes,
            createdAt = e.createdAt,
            updatedAt = e.updatedAt
        )
    }
}
