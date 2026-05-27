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
import java.util.UUID

@RestController
@RequestMapping("/api/inventory")
class InventoryController(
    private val service: InventoryService
) {
    @GetMapping
    fun list(@CurrentTenantId tenantId: UUID, @PageableDefault(size = 20) pageable: Pageable): Page<ItemResponse> =
        service.findAll(tenantId, pageable).map { ItemResponse.from(it) }

    @GetMapping("/{id}")
    fun get(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<ItemResponse> {
        val item = service.findById(tenantId, id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ItemResponse.from(item))
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@CurrentTenantId tenantId: UUID, @Valid @RequestBody request: CreateItemRequest): ItemResponse {
        val saved = service.create(request.toEntity(tenantId))
        return ItemResponse.from(saved)
    }

    @PutMapping("/{id}")
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
    fun delete(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<Unit> {
        val deleted = service.delete(tenantId, id)
        return if (deleted) ResponseEntity.noContent().build()
        else ResponseEntity.notFound().build()
    }
}
