package com.blacksmith.metalstore.client.controller

import com.blacksmith.metalstore.auth.config.CurrentTenantId
import com.blacksmith.metalstore.client.application.ClientService
import com.blacksmith.metalstore.client.domain.dto.request.CreateClientRequest
import com.blacksmith.metalstore.client.domain.dto.request.UpdateClientRequest
import com.blacksmith.metalstore.client.domain.dto.response.ClientResponse
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/clients")
class ClientController(
    private val service: ClientService
) {
    @GetMapping
    fun list(
        @CurrentTenantId tenantId: UUID,
        @PageableDefault(size = 20) pageable: Pageable,
        @RequestParam(name = "q", required = false) nameFilter: String?
    ): Page<ClientResponse> =
        service.findAll(tenantId, pageable, nameFilter).map { ClientResponse.from(it) }

    @GetMapping("/{id}")
    fun get(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<ClientResponse> {
        val client = service.findById(tenantId, id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ClientResponse.from(client))
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@CurrentTenantId tenantId: UUID, @Valid @RequestBody request: CreateClientRequest): ClientResponse {
        val saved = service.create(request.toEntity(tenantId))
        return ClientResponse.from(saved)
    }

    @PutMapping("/{id}")
    fun update(
        @CurrentTenantId tenantId: UUID,
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateClientRequest
    ): ResponseEntity<ClientResponse> {
        val updated = service.update(tenantId, id, request.toEntity(tenantId))
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ClientResponse.from(updated))
    }

    @DeleteMapping("/{id}")
    fun delete(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<Unit> {
        val deleted = service.delete(tenantId, id)
        return if (deleted) ResponseEntity.noContent().build()
        else ResponseEntity.notFound().build()
    }

    @PostMapping("/{id}/activate")
    fun activate(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<ClientResponse> {
        val client = service.activate(tenantId, id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ClientResponse.from(client))
    }

    @PostMapping("/{id}/deactivate")
    fun deactivate(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<ClientResponse> {
        val client = service.deactivate(tenantId, id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ClientResponse.from(client))
    }
}
