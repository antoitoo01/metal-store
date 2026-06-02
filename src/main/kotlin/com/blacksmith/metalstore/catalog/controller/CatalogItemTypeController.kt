package com.blacksmith.metalstore.catalog.controller

import com.blacksmith.metalstore.auth.config.CurrentTenantId
import com.blacksmith.metalstore.catalog.application.CatalogItemTypeService
import com.blacksmith.metalstore.catalog.domain.dto.request.CreateTypeRequest
import com.blacksmith.metalstore.catalog.domain.dto.request.UpdateTypeRequest
import com.blacksmith.metalstore.catalog.domain.dto.response.TypeResponse
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID

@RestController
@RequestMapping("/api/catalog/item-types")
@Tag(name = "Catalog", description = "Tipos de ítem de catálogo")
class CatalogItemTypeController(
    private val service: CatalogItemTypeService
) {
    @GetMapping
    @Operation(summary = "Listar tipos de ítem", description = "Retorna una lista paginada de tipos de ítem de catálogo.")
    @ApiResponse(responseCode = "200", description = "Operación exitosa")
    fun list(@CurrentTenantId tenantId: UUID, @PageableDefault(size = 20) pageable: Pageable): Page<TypeResponse> =
        service.list(tenantId, pageable).map { TypeResponse.from(it) }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de ítem por ID", description = "Retorna un tipo de ítem de catálogo por su UUID.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun get(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<TypeResponse> {
        val type = service.findById(tenantId, id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(TypeResponse.from(type))
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear tipo de ítem", description = "Crea un nuevo tipo de ítem de catálogo.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Recurso creado"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun create(@CurrentTenantId tenantId: UUID, @Valid @RequestBody request: CreateTypeRequest): TypeResponse {
        val saved = service.create(request.toEntity(tenantId))
        return TypeResponse.from(saved)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de ítem", description = "Actualiza los datos de un tipo de ítem de catálogo existente.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun update(
        @CurrentTenantId tenantId: UUID,
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateTypeRequest
    ): ResponseEntity<TypeResponse> {
        val updated = service.update(tenantId, id, request.toEntity(tenantId))
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(TypeResponse.from(updated))
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo de ítem", description = "Elimina un tipo de ítem de catálogo por su UUID.")
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
