package com.blacksmith.metalstore.billing.domain.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

data class PriceUpdateRequest(
    @field:Schema(description = "Precio unitario sin IVA", example = "125.50")
    val unitPrice: BigDecimal? = null,

    @field:Schema(description = "Fecha de inicio de vigencia", example = "2026-01-01")
    val validFrom: LocalDate? = null,

    @field:Schema(description = "Fecha de fin de vigencia", example = "2026-12-31")
    val validTo: LocalDate? = null,

    @field:Schema(description = "Notas internas", example = "Precio actualizado")
    val notes: String? = null
)
