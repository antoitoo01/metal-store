package com.blacksmith.metalstore.purchase.domain.dto.response

import com.blacksmith.metalstore.purchase.domain.entity.Supplier
import com.blacksmith.metalstore.purchase.domain.entity.SupplierStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

data class SupplierResponse(
    @field:Schema(description = "ID del proveedor")
    val id: UUID,

    @field:Schema(description = "ID de la organización")
    val organizationId: UUID,

    @field:Schema(description = "Nombre del proveedor")
    val name: String,

    @field:Schema(description = "Email")
    val email: String?,

    @field:Schema(description = "Teléfono")
    val phone: String?,

    @field:Schema(description = "Dirección")
    val address: String?,

    @field:Schema(description = "NIF / CIF")
    val vatNumber: String?,

    @field:Schema(description = "Notas")
    val notes: String?,

    @field:Schema(description = "Estado")
    val status: SupplierStatus,

    @field:Schema(description = "Fecha de creación")
    val createdAt: LocalDateTime,

    @field:Schema(description = "Fecha de actualización")
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(e: Supplier) = SupplierResponse(
            id = e.id,
            organizationId = e.organizationId,
            name = e.name,
            email = e.email,
            phone = e.phone,
            address = e.address,
            vatNumber = e.vatNumber,
            notes = e.notes,
            status = e.status,
            createdAt = e.createdAt,
            updatedAt = e.updatedAt
        )
    }
}
