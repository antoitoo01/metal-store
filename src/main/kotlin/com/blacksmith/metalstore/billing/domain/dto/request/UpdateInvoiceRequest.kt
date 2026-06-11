package com.blacksmith.metalstore.billing.domain.dto.request

import io.swagger.v3.oas.annotations.media.Schema

data class UpdateInvoiceRequest(
    @field:Schema(description = "Nombre del cliente", example = "Aceros del Norte S.A.")
    val customerName: String? = null,

    @field:Schema(description = "CIF/NIF del cliente", example = "B-12345678")
    val customerVat: String? = null,

    @field:Schema(description = "Dirección del cliente", example = "Av. Industrial 1234, Buenos Aires")
    val customerAddress: String? = null,

    @field:Schema(description = "Notas", example = "Factura corregida")
    val notes: String? = null
)
