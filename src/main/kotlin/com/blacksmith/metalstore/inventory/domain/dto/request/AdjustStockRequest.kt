package com.blacksmith.metalstore.inventory.domain.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class AdjustStockRequest(
    @field:Schema(description = "Cantidad a añadir/retirar", example = "50.00")
    @field:Positive
    val quantity: BigDecimal,

    @field:Schema(description = "Motivo del ajuste", example = "Regularización de inventario")
    val notes: String? = null
)
