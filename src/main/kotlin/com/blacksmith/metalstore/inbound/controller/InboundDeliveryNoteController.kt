package com.blacksmith.metalstore.inbound.controller

import com.blacksmith.metalstore.inbound.application.InboundDeliveryNoteService
import com.blacksmith.metalstore.inbound.domain.dto.request.AddInboundLineRequest
import com.blacksmith.metalstore.inbound.domain.dto.request.CreateInboundDeliveryNoteRequest
import com.blacksmith.metalstore.inbound.domain.dto.response.InboundDeliveryNoteLineResponse
import com.blacksmith.metalstore.inbound.domain.dto.response.InboundDeliveryNoteResponse
import com.blacksmith.metalstore.organization.config.CurrentOrganizationId
import com.blacksmith.metalstore.organization.config.RequiresRole
import com.blacksmith.metalstore.organization.domain.entity.OrganizationRole
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/inbound-delivery-notes")
@Tag(name = "Albaranes de Entrada", description = "Gestión de albaranes de entrada de proveedores")
class InboundDeliveryNoteController(
    private val service: InboundDeliveryNoteService
) {
    @GetMapping
    @Operation(summary = "Listar albaranes", description = "Retorna la lista paginada de albaranes de entrada.")
    fun list(
        @CurrentOrganizationId organizationId: UUID,
        @PageableDefault(size = 20) pageable: Pageable
    ): Page<InboundDeliveryNoteResponse> =
        service.findAll(organizationId, pageable).map { InboundDeliveryNoteResponse.from(it) }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener albarán", description = "Retorna un albarán de entrada por su ID.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun getById(
        @CurrentOrganizationId organizationId: UUID,
        @PathVariable id: UUID
    ): InboundDeliveryNoteResponse =
        InboundDeliveryNoteResponse.from(service.findById(organizationId, id))

    @GetMapping("/{id}/lines")
    @Operation(summary = "Obtener líneas", description = "Retorna las líneas de un albarán de entrada.")
    fun getLines(
        @CurrentOrganizationId organizationId: UUID,
        @PathVariable id: UUID
    ): List<InboundDeliveryNoteLineResponse> {
        service.findById(organizationId, id) // verify existence + org
        return service.getLines(id).map { InboundDeliveryNoteLineResponse.from(it) }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequiresRole(OrganizationRole.STAFF)
    @Operation(summary = "Crear albarán", description = "Crea un nuevo albarán de entrada en estado DRAFT.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Recurso creado"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun create(
        @CurrentOrganizationId organizationId: UUID,
        @Valid @RequestBody request: CreateInboundDeliveryNoteRequest
    ): InboundDeliveryNoteResponse =
        InboundDeliveryNoteResponse.from(service.create(organizationId, request.toEntity(organizationId, "")))

    @PostMapping("/{id}/lines")
    @RequiresRole(OrganizationRole.STAFF)
    @Operation(summary = "Añadir línea", description = "Añade una línea a un albarán en DRAFT.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Línea añadida"),
        ApiResponse(responseCode = "400", description = "El albarán no está en DRAFT")
    ])
    fun addLine(
        @CurrentOrganizationId organizationId: UUID,
        @PathVariable id: UUID,
        @Valid @RequestBody request: AddInboundLineRequest
    ): InboundDeliveryNoteLineResponse =
        InboundDeliveryNoteLineResponse.from(service.addLine(organizationId, id, request.toEntity(id)))

    @DeleteMapping("/{id}/lines/{lineId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequiresRole(OrganizationRole.STAFF)
    @Operation(summary = "Eliminar línea", description = "Elimina una línea de un albarán en DRAFT.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Línea eliminada"),
        ApiResponse(responseCode = "400", description = "El albarán no está en DRAFT")
    ])
    fun removeLine(
        @CurrentOrganizationId organizationId: UUID,
        @PathVariable id: UUID,
        @PathVariable lineId: UUID
    ): ResponseEntity<Void> {
        service.removeLine(organizationId, id, lineId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/confirm")
    @RequiresRole(OrganizationRole.STAFF)
    @Operation(summary = "Confirmar albarán", description = "Confirma el albarán y actualiza el inventario con los materiales recibidos.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Albarán confirmado"),
        ApiResponse(responseCode = "400", description = "No se puede confirmar desde el estado actual")
    ])
    fun confirm(
        @CurrentOrganizationId organizationId: UUID,
        @PathVariable id: UUID
    ): InboundDeliveryNoteResponse =
        InboundDeliveryNoteResponse.from(service.confirm(organizationId, id))

    @PostMapping("/{id}/cancel")
    @RequiresRole(OrganizationRole.STAFF)
    @Operation(summary = "Cancelar albarán", description = "Cancela un albarán de entrada.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Albarán cancelado"),
        ApiResponse(responseCode = "400", description = "No se puede cancelar desde el estado actual")
    ])
    fun cancel(
        @CurrentOrganizationId organizationId: UUID,
        @PathVariable id: UUID
    ): InboundDeliveryNoteResponse =
        InboundDeliveryNoteResponse.from(service.cancel(organizationId, id))
}
