package com.blacksmith.metalstore.quote.controller

import com.blacksmith.metalstore.organization.config.CurrentOrganizationId
import com.blacksmith.metalstore.quote.application.QuoteService
import com.blacksmith.metalstore.quote.domain.dto.request.CreateQuoteLineRequest
import com.blacksmith.metalstore.quote.domain.dto.request.CreateQuoteRequest
import com.blacksmith.metalstore.quote.domain.dto.request.UpdateQuoteRequest
import com.blacksmith.metalstore.quote.domain.dto.response.QuoteLineResponse
import com.blacksmith.metalstore.quote.domain.dto.response.QuoteResponse
import com.blacksmith.metalstore.quote.domain.entity.QuoteStatus
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
@RequestMapping("/api/quotes")
@Tag(name = "Quotes", description = "Gestión de cotizaciones")
class QuoteController(
    private val service: QuoteService
) {
    @GetMapping
    @Operation(summary = "Listar cotizaciones", description = "Retorna una lista paginada de cotizaciones.")
    @ApiResponse(responseCode = "200", description = "Operación exitosa")
    fun list(
        @CurrentOrganizationId organizationId: UUID,
        @PageableDefault(size = 20) pageable: Pageable,
        @RequestParam(name = "q", required = false) q: String?,
        @RequestParam(name = "status", required = false) status: QuoteStatus?,
        @RequestParam(name = "clientId", required = false) clientId: UUID?
    ): Page<QuoteResponse> =
        service.listQuotes(organizationId, pageable, q, status, clientId).map { QuoteResponse.from(it) }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cotización por ID", description = "Retorna los datos de una cotización por su UUID.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun get(@CurrentOrganizationId organizationId: UUID, @PathVariable id: UUID): ResponseEntity<QuoteResponse> {
        val quote = service.findQuote(organizationId, id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(QuoteResponse.from(quote))
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear borrador de cotización", description = "Crea un nuevo borrador de cotización.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Recurso creado"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun createDraft(
        @CurrentOrganizationId organizationId: UUID,
        @Valid @RequestBody request: CreateQuoteRequest
    ): QuoteResponse {
        val quote = service.createDraft(organizationId, request.toEntity(organizationId, ""))
        return QuoteResponse.from(quote)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cotización", description = "Actualiza los datos de cabecera de una cotización en estado DRAFT.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida o no está en DRAFT"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun update(
        @CurrentOrganizationId organizationId: UUID,
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateQuoteRequest
    ): ResponseEntity<QuoteResponse> {
        val updated = service.update(organizationId, id, request.customerName, request.customerVat, request.customerAddress, request.validUntil, request.notes)
            ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(QuoteResponse.from(updated))
    }

    @GetMapping("/{id}/lines")
    @Operation(summary = "Obtener líneas de cotización", description = "Retorna las líneas de una cotización.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun getLines(@PathVariable id: UUID): List<QuoteLineResponse> =
        service.getLines(id).map { QuoteLineResponse.from(it) }

    @PostMapping("/{id}/lines")
    @Operation(summary = "Agregar línea a cotización", description = "Agrega una nueva línea a una cotización existente.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun addLine(
        @CurrentOrganizationId organizationId: UUID,
        @PathVariable id: UUID,
        @Valid @RequestBody request: CreateQuoteLineRequest
    ): ResponseEntity<QuoteLineResponse> {
        val saved = service.addLine(organizationId, id, request.toEntity(id))
            ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(QuoteLineResponse.from(saved))
    }

    @DeleteMapping("/{quoteId}/lines/{lineId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar línea de cotización", description = "Elimina una línea de una cotización.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Sin contenido"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun removeLine(@CurrentOrganizationId organizationId: UUID, @PathVariable quoteId: UUID, @PathVariable lineId: UUID) {
        service.removeLine(organizationId, quoteId, lineId)
    }

    @PostMapping("/{id}/issue")
    @Operation(summary = "Emitir cotización", description = "Cambia el estado de la cotización a emitida.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun issue(@CurrentOrganizationId organizationId: UUID, @PathVariable id: UUID): ResponseEntity<QuoteResponse> {
        val quote = service.issue(organizationId, id) ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(QuoteResponse.from(quote))
    }

    @PostMapping("/{id}/accept")
    @Operation(summary = "Aceptar cotización", description = "Cambia el estado de la cotización a aceptada.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun accept(@CurrentOrganizationId organizationId: UUID, @PathVariable id: UUID): ResponseEntity<QuoteResponse> {
        val quote = service.accept(organizationId, id) ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(QuoteResponse.from(quote))
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Rechazar cotización", description = "Cambia el estado de la cotización a rechazada.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun reject(@CurrentOrganizationId organizationId: UUID, @PathVariable id: UUID): ResponseEntity<QuoteResponse> {
        val quote = service.reject(organizationId, id) ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(QuoteResponse.from(quote))
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancelar cotización", description = "Cambia el estado de la cotización a cancelada.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun cancel(@CurrentOrganizationId organizationId: UUID, @PathVariable id: UUID): ResponseEntity<QuoteResponse> {
        val quote = service.cancel(organizationId, id) ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(QuoteResponse.from(quote))
    }
}
