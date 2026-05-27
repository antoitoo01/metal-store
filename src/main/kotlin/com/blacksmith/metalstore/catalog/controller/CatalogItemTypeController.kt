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
import java.util.UUID

@RestController
@RequestMapping("/api/catalog/item-types")
class CatalogItemTypeController(
    private val service: CatalogItemTypeService
) {
    @GetMapping
    fun list(@CurrentTenantId tenantId: UUID, @PageableDefault(size = 20) pageable: Pageable): Page<TypeResponse> =
        service.list(tenantId, pageable).map { TypeResponse.from(it) }

    @GetMapping("/{id}")
    fun get(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<TypeResponse> {
        val type = service.findById(tenantId, id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(TypeResponse.from(type))
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@CurrentTenantId tenantId: UUID, @Valid @RequestBody request: CreateTypeRequest): TypeResponse {
        val saved = service.create(request.toEntity(tenantId))
        return TypeResponse.from(saved)
    }

    @PutMapping("/{id}")
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
    fun delete(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<Unit> {
        val deleted = service.delete(tenantId, id)
        return if (deleted) ResponseEntity.noContent().build()
        else ResponseEntity.notFound().build()
    }
}
