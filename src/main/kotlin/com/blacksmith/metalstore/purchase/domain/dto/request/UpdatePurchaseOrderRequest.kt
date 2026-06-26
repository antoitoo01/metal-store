package com.blacksmith.metalstore.purchase.domain.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.util.UUID

data class UpdatePurchaseOrderRequest(
    @field:Schema(description = "ID del proveedor")
    val supplierId: UUID? = null,

    @field:Schema(description = "Nombre del proveedor")
    val supplierName: String? = null,

    @field:Schema(description = "NIF del proveedor")
    val supplierVat: String? = null,

    @field:Schema(description = "Dirección del proveedor")
    val supplierAddress: String? = null,

    @field:Schema(description = "Fecha prevista de recepción")
    val expectedDate: LocalDate? = null,

    @field:Schema(description = "Notas")
    val notes: String? = null
)
