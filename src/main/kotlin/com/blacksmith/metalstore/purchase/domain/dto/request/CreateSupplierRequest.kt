package com.blacksmith.metalstore.purchase.domain.dto.request

import com.blacksmith.metalstore.purchase.domain.entity.Supplier
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class CreateSupplierRequest(
    @field:Schema(description = "Nombre del proveedor", example = "Aceros Inoxidables S.L.")
    @field:NotBlank
    val name: String,

    @field:Schema(description = "Email", example = "info@aceros.com")
    val email: String? = null,

    @field:Schema(description = "Teléfono", example = "+34 965 123 456")
    val phone: String? = null,

    @field:Schema(description = "Dirección", example = "Pol. Ind. Las Atalayas, Av. del Acero, 15")
    val address: String? = null,

    @field:Schema(description = "NIF / CIF", example = "B12345678")
    val vatNumber: String? = null,

    @field:Schema(description = "Notas", example = "Proveedor habitual de perfiles IPN")
    val notes: String? = null
) {
    fun toEntity(organizationId: UUID) = Supplier(
        organizationId = organizationId,
        name = name,
        email = email,
        phone = phone,
        address = address,
        vatNumber = vatNumber,
        notes = notes
    )
}
