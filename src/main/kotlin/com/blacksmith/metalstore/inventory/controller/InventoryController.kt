package com.blacksmith.metalstore.inventory.controller

import com.blacksmith.metalstore.organization.config.CurrentOrganizationId
import com.blacksmith.metalstore.organization.config.RequiresRole
import com.blacksmith.metalstore.organization.domain.entity.OrganizationRole
import com.blacksmith.metalstore.inventory.application.InventoryService
import com.blacksmith.metalstore.inventory.domain.dto.request.CreateItemRequest
import com.blacksmith.metalstore.inventory.domain.dto.request.UpdateItemRequest
import com.blacksmith.metalstore.inventory.domain.dto.response.ItemResponse
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID

@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory", description = "Gestión de inventario")
class InventoryController(
    private val service: InventoryService
) {
    @GetMapping
    @Operation(summary = "Listar inventario", description = "Retorna una lista paginada de ítems de inventario.")
    @ApiResponse(responseCode = "200", description = "Operación exitosa")
    fun list(@CurrentOrganizationId organizationId: UUID, @PageableDefault(size = 20) pageable: Pageable): Page<ItemResponse> =
        service.findAll(organizationId, pageable).map { ItemResponse.from(it) }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener ítem por ID", description = "Retorna los datos de un ítem de inventario por su UUID.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun get(@CurrentOrganizationId organizationId: UUID, @PathVariable id: UUID): ItemResponse =
        ItemResponse.from(service.findById(organizationId, id))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequiresRole(OrganizationRole.STAFF)
    @Operation(summary = "Crear ítem", description = "Crea un nuevo ítem de inventario.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Recurso creado"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun create(@CurrentOrganizationId organizationId: UUID, @Valid @RequestBody request: CreateItemRequest): ItemResponse {
        val saved = service.create(request.toEntity(organizationId))
        return ItemResponse.from(saved)
    }

    @PutMapping("/{id}")
    @RequiresRole(OrganizationRole.STAFF)
    @Operation(summary = "Actualizar ítem", description = "Actualiza los datos de un ítem de inventario existente.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun update(
        @CurrentOrganizationId organizationId: UUID,
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateItemRequest
    ): ItemResponse =
        ItemResponse.from(service.update(organizationId, id, request.toEntity(organizationId)))

    @DeleteMapping("/{id}")
    @RequiresRole(OrganizationRole.ADMIN)
    @Operation(summary = "Eliminar ítem", description = "Elimina un ítem de inventario por su UUID.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Sin contenido"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun delete(@CurrentOrganizationId organizationId: UUID, @PathVariable id: UUID): ResponseEntity<Unit> {
        service.delete(organizationId, id)
        return ResponseEntity.noContent().build()
    }
}
