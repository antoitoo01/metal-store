package com.blacksmith.metalstore.billing.domain.dto.request

import com.blacksmith.metalstore.billing.domain.entity.PriceListItem
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class UpsertPriceRequest(
    @field:Schema(description = "Identificador del perfil de artículo (opcional)", example = "550e8400-e29b-41d4-a716-446655440000")
    val profileId: UUID? = null,

    @field:Schema(description = "Identificador del artículo (opcional)", example = "550e8400-e29b-41d4-a716-446655440001")
    val itemId: UUID? = null,

    @field:Schema(description = "Precio unitario sin IVA", example = "125.50")
    @field:Positive
    val unitPrice: BigDecimal,

    @field:Schema(description = "Fecha de inicio de vigencia (opcional)", example = "2026-01-01")
    val validFrom: LocalDate? = null,

    @field:Schema(description = "Fecha de fin de vigencia (opcional)", example = "2026-12-31")
    val validTo: LocalDate? = null,

    @field:Schema(description = "Notas internas sobre el precio (opcional)", example = "Precio especial para cliente VIP")
    val notes: String? = null
) {
    fun toEntity(organizationId: UUID) = PriceListItem(
        organizationId = organizationId,
        profileId = profileId,
        itemId = itemId,
        unitPrice = unitPrice,
        validFrom = validFrom,
        validTo = validTo,
        notes = notes
    )
}
