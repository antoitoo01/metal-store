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
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID

@RestController
@RequestMapping("/api/clients")
@Tag(name = "Clients", description = "Gestión de clientes")
class ClientController(
    private val service: ClientService
) {
    @GetMapping
    @Operation(summary = "Listar clientes", description = "Retorna una lista paginada de clientes.")
    @ApiResponse(responseCode = "200", description = "Operación exitosa")
    fun list(
        @CurrentTenantId tenantId: UUID,
        @PageableDefault(size = 20) pageable: Pageable,
        @RequestParam(name = "q", required = false) nameFilter: String?
    ): Page<ClientResponse> =
        service.findAll(tenantId, pageable, nameFilter).map { ClientResponse.from(it) }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente por ID", description = "Retorna los datos de un cliente por su UUID.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun get(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<ClientResponse> {
        val client = service.findById(tenantId, id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ClientResponse.from(client))
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear cliente", description = "Crea un nuevo cliente en el sistema.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Recurso creado"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun create(@CurrentTenantId tenantId: UUID, @Valid @RequestBody request: CreateClientRequest): ClientResponse {
        val saved = service.create(request.toEntity(tenantId))
        return ClientResponse.from(saved)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cliente", description = "Actualiza los datos de un cliente existente.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
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
    @Operation(summary = "Eliminar cliente", description = "Elimina un cliente por su UUID.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Sin contenido"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun delete(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<Unit> {
        val deleted = service.delete(tenantId, id)
        return if (deleted) ResponseEntity.noContent().build()
        else ResponseEntity.notFound().build()
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activar cliente", description = "Activa un cliente previamente desactivado.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun activate(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<ClientResponse> {
        val client = service.activate(tenantId, id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ClientResponse.from(client))
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Desactivar cliente", description = "Desactiva un cliente existente.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun deactivate(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<ClientResponse> {
        val client = service.deactivate(tenantId, id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ClientResponse.from(client))
    }
}
