package com.blacksmith.metalstore.inventory.controller

import com.blacksmith.metalstore.auth.config.CurrentTenantId
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
    fun list(@CurrentTenantId tenantId: UUID, @PageableDefault(size = 20) pageable: Pageable): Page<ItemResponse> =
        service.findAll(tenantId, pageable).map { ItemResponse.from(it) }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener ítem por ID", description = "Retorna los datos de un ítem de inventario por su UUID.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun get(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<ItemResponse> {
        val item = service.findById(tenantId, id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ItemResponse.from(item))
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear ítem", description = "Crea un nuevo ítem de inventario.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Recurso creado"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun create(@CurrentTenantId tenantId: UUID, @Valid @RequestBody request: CreateItemRequest): ItemResponse {
        val saved = service.create(request.toEntity(tenantId))
        return ItemResponse.from(saved)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar ítem", description = "Actualiza los datos de un ítem de inventario existente.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun update(
        @CurrentTenantId tenantId: UUID,
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateItemRequest
    ): ResponseEntity<ItemResponse> {
        val updated = service.update(tenantId, id, request.toEntity(tenantId))
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ItemResponse.from(updated))
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar ítem", description = "Elimina un ítem de inventario por su UUID.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Sin contenido"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun delete(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<Unit> {
        val deleted = service.delete(tenantId, id)
        return if (deleted) ResponseEntity.noContent().build()
        else ResponseEntity.notFound().build()
    }
}
