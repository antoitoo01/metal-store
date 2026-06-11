package com.blacksmith.metalstore.quote.domain.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class UpdateQuoteRequest(
    @field:Schema(description = "Nombre del cliente", example = "Aceros del Norte S.A.")
    val customerName: String? = null,

    @field:Schema(description = "CIF/NIF del cliente", example = "B-12345678")
    val customerVat: String? = null,

    @field:Schema(description = "Dirección del cliente", example = "Av. Industrial 1234, Buenos Aires")
    val customerAddress: String? = null,

    @field:Schema(description = "Fecha de validez", example = "2026-07-15")
    val validUntil: LocalDate? = null,

    @field:Schema(description = "Notas", example = "Presupuesto válido por 30 días")
    val notes: String? = null
)
