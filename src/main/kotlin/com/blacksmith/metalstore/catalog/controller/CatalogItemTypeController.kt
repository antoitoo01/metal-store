package com.blacksmith.metalstore.catalog.controller

import com.blacksmith.metalstore.organization.config.CurrentOrganizationId
import com.blacksmith.metalstore.organization.config.RequiresRole
import com.blacksmith.metalstore.organization.domain.entity.OrganizationRole
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
    fun list(@CurrentOrganizationId organizationId: UUID, @PageableDefault(size = 20) pageable: Pageable): Page<TypeResponse> =
        service.list(organizationId, pageable).map { TypeResponse.from(it) }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de ítem por ID", description = "Retorna un tipo de ítem de catálogo por su UUID.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun get(@CurrentOrganizationId organizationId: UUID, @PathVariable id: UUID): TypeResponse =
        TypeResponse.from(service.findById(organizationId, id))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequiresRole(OrganizationRole.EDITOR)
    @Operation(summary = "Crear tipo de ítem", description = "Crea un nuevo tipo de ítem de catálogo.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Recurso creado"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun create(@CurrentOrganizationId organizationId: UUID, @Valid @RequestBody request: CreateTypeRequest): TypeResponse {
        val saved = service.create(request.toEntity(organizationId))
        return TypeResponse.from(saved)
    }

    @PutMapping("/{id}")
    @RequiresRole(OrganizationRole.EDITOR)
    @Operation(summary = "Actualizar tipo de ítem", description = "Actualiza los datos de un tipo de ítem de catálogo existente.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun update(
        @CurrentOrganizationId organizationId: UUID,
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateTypeRequest
    ): TypeResponse =
        TypeResponse.from(service.update(organizationId, id, request.toEntity(organizationId)))

    @DeleteMapping("/{id}")
    @RequiresRole(OrganizationRole.ADMIN)
    @Operation(summary = "Eliminar tipo de ítem", description = "Elimina un tipo de ítem de catálogo por su UUID.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Sin contenido"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun delete(@CurrentOrganizationId organizationId: UUID, @PathVariable id: UUID): ResponseEntity<Unit> {
        service.delete(organizationId, id)
        return ResponseEntity.noContent().build()
    }
}
