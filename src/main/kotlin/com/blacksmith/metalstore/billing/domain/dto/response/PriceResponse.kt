package com.blacksmith.metalstore.billing.domain.dto.response

import com.blacksmith.metalstore.billing.domain.entity.PriceListItem
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class PriceResponse(
    @field:Schema(description = "Identificador único del precio", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,

    @field:Schema(description = "Identificador del inquilino", example = "550e8400-e29b-41d4-a716-446655440000")
    val tenantId: UUID,

    @field:Schema(description = "Identificador del perfil de artículo (opcional)", example = "550e8400-e29b-41d4-a716-446655440001")
    val profileId: UUID?,

    @field:Schema(description = "Identificador del artículo (opcional)", example = "550e8400-e29b-41d4-a716-446655440002")
    val itemId: UUID?,

    @field:Schema(description = "Precio unitario sin IVA", example = "125.50")
    val unitPrice: BigDecimal,

    @field:Schema(description = "Fecha de inicio de vigencia (opcional)", example = "2026-01-01")
    val validFrom: LocalDate?,

    @field:Schema(description = "Fecha de fin de vigencia (opcional)", example = "2026-12-31")
    val validTo: LocalDate?,

    @field:Schema(description = "Notas internas (opcional)", example = "Precio especial temporada 2026")
    val notes: String?
) {
    companion object {
        fun from(e: PriceListItem) = PriceResponse(
            id = e.id, tenantId = e.tenantId, profileId = e.profileId, itemId = e.itemId,
            unitPrice = e.unitPrice, validFrom = e.validFrom, validTo = e.validTo, notes = e.notes
        )
    }
}
