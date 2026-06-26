package com.blacksmith.metalstore.purchase.domain.dto.request

import com.blacksmith.metalstore.purchase.domain.entity.Supplier
import com.blacksmith.metalstore.purchase.domain.entity.SupplierStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class UpdateSupplierRequest(
    @field:Schema(description = "Nombre del proveedor")
    val name: String? = null,

    @field:Schema(description = "Email")
    val email: String? = null,

    @field:Schema(description = "Teléfono")
    val phone: String? = null,

    @field:Schema(description = "Dirección")
    val address: String? = null,

    @field:Schema(description = "NIF / CIF")
    val vatNumber: String? = null,

    @field:Schema(description = "Notas")
    val notes: String? = null,

    @field:Schema(description = "Estado", example = "ACTIVE, INACTIVE")
    val status: String? = null
) {
    fun toEntity(organizationId: UUID) = Supplier(
        organizationId = organizationId,
        name = name ?: "",
        email = email,
        phone = phone,
        address = address,
        vatNumber = vatNumber,
        notes = notes,
        status = status?.let { SupplierStatus.valueOf(it) } ?: SupplierStatus.ACTIVE
    )
}
