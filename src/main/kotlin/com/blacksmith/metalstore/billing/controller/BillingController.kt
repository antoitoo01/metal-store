package com.blacksmith.metalstore.billing.controller

import com.blacksmith.metalstore.organization.config.CurrentOrganizationId
import com.blacksmith.metalstore.billing.application.BillingService
import com.blacksmith.metalstore.billing.domain.dto.request.CreateLineRequest
import com.blacksmith.metalstore.billing.domain.dto.request.PriceUpdateRequest
import com.blacksmith.metalstore.billing.domain.dto.request.UpdateInvoiceRequest
import com.blacksmith.metalstore.billing.domain.dto.request.UpsertPriceRequest
import com.blacksmith.metalstore.billing.domain.dto.response.InvoiceResponse
import com.blacksmith.metalstore.billing.domain.dto.response.LineResponse
import com.blacksmith.metalstore.billing.domain.dto.response.PriceResponse
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
@RequestMapping("/api/billing")
@Tag(name = "Billing", description = "Facturación y precios")
class BillingController(
    private val service: BillingService
) {
    // ── Price List ──────────────────────────────────────────────
    @GetMapping("/prices")
    @Operation(summary = "Listar precios", description = "Retorna una lista paginada de precios.")
    @ApiResponse(responseCode = "200", description = "Operación exitosa")
    fun listPrices(@CurrentOrganizationId organizationId: UUID, @PageableDefault(size = 20) pageable: Pageable): Page<PriceResponse> =
        service.listPrices(organizationId, pageable).map { PriceResponse.from(it) }

    @PostMapping("/prices")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear o actualizar precio", description = "Inserta o actualiza un precio.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Recurso creado"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun upsertPrice(@CurrentOrganizationId organizationId: UUID, @Valid @RequestBody request: UpsertPriceRequest): PriceResponse {
        val saved = service.upsertPrice(request.toEntity(organizationId))
        return PriceResponse.from(saved)
    }

    @DeleteMapping("/prices/{id}")
    @Operation(summary = "Eliminar precio", description = "Elimina un precio por su UUID.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Sin contenido"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun deletePrice(@CurrentOrganizationId organizationId: UUID, @PathVariable id: UUID): ResponseEntity<Unit> {
        val deleted = service.deletePrice(organizationId, id)
        return if (deleted) ResponseEntity.noContent().build()
        else ResponseEntity.notFound().build()
    }

    @PutMapping("/prices/{id}")
    @Operation(summary = "Actualizar precio", description = "Actualiza los campos de un precio existente.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun updatePrice(
        @CurrentOrganizationId organizationId: UUID,
        @PathVariable id: UUID,
        @Valid @RequestBody request: PriceUpdateRequest
    ): ResponseEntity<PriceResponse> {
        val updated = service.updatePrice(organizationId, id, request.unitPrice, request.validFrom, request.validTo, request.notes)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(PriceResponse.from(updated))
    }

    // ── Invoices ────────────────────────────────────────────────
    @GetMapping("/invoices")
    @Operation(summary = "Listar facturas", description = "Retorna una lista paginada de facturas.")
    @ApiResponse(responseCode = "200", description = "Operación exitosa")
    fun listInvoices(@CurrentOrganizationId organizationId: UUID, @PageableDefault(size = 20) pageable: Pageable, @RequestParam(name = "q", required = false) q: String?): Page<InvoiceResponse> =
        service.listInvoices(organizationId, pageable, q).map { InvoiceResponse.from(it) }

    @GetMapping("/invoices/{id}")
    @Operation(summary = "Obtener factura por ID", description = "Retorna los datos de una factura por su UUID.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun getInvoice(@CurrentOrganizationId organizationId: UUID, @PathVariable id: UUID): ResponseEntity<InvoiceResponse> {
        val inv = service.findInvoice(organizationId, id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(InvoiceResponse.from(inv))
    }

    @PutMapping("/invoices/{id}")
    @Operation(summary = "Actualizar factura", description = "Actualiza los datos de cabecera de una factura en estado DRAFT.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida o no está en DRAFT"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun updateInvoice(
        @CurrentOrganizationId organizationId: UUID,
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateInvoiceRequest
    ): ResponseEntity<InvoiceResponse> {
        val updated = service.update(organizationId, id, request.customerName, request.customerVat, request.customerAddress, request.notes)
            ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(InvoiceResponse.from(updated))
    }

    @GetMapping("/invoices/{id}/lines")
    @Operation(summary = "Obtener líneas de factura", description = "Retorna las líneas de una factura.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun getLines(@PathVariable id: UUID): List<LineResponse> =
        service.getLines(id).map { LineResponse.from(it) }

    @PostMapping("/invoices")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear borrador de factura", description = "Crea un nuevo borrador de factura.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Recurso creado"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun createDraft(
        @CurrentOrganizationId organizationId: UUID,
        @RequestParam customerName: String? = null,
        @RequestParam customerVat: String? = null
    ): InvoiceResponse = InvoiceResponse.from(service.createDraft(organizationId, customerName, customerVat))

    @PostMapping("/invoices/{id}/lines")
    @Operation(summary = "Agregar línea a factura", description = "Agrega una nueva línea a una factura existente.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun addLine(
        @CurrentOrganizationId organizationId: UUID,
        @PathVariable id: UUID,
        @Valid @RequestBody request: CreateLineRequest
    ): ResponseEntity<LineResponse> {
        val saved = service.addLine(organizationId, id, request.toEntity(id))
            ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(LineResponse.from(saved))
    }

    @DeleteMapping("/invoices/{invoiceId}/lines/{lineId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar línea de factura", description = "Elimina una línea de una factura.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Sin contenido"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun removeLine(@CurrentOrganizationId organizationId: UUID, @PathVariable invoiceId: UUID, @PathVariable lineId: UUID) {
        service.removeLine(organizationId, invoiceId, lineId)
    }

    @PostMapping("/invoices/{id}/issue")
    @Operation(summary = "Emitir factura", description = "Cambia el estado de la factura a emitida.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun issue(@CurrentOrganizationId organizationId: UUID, @PathVariable id: UUID): ResponseEntity<InvoiceResponse> {
        val inv = service.issue(organizationId, id) ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(InvoiceResponse.from(inv))
    }

    @PostMapping("/invoices/{id}/pay")
    @Operation(summary = "Pagar factura", description = "Cambia el estado de la factura a pagada.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun markPaid(@CurrentOrganizationId organizationId: UUID, @PathVariable id: UUID): ResponseEntity<InvoiceResponse> {
        val inv = service.markPaid(organizationId, id) ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(InvoiceResponse.from(inv))
    }

    @PostMapping("/invoices/{id}/cancel")
    @Operation(summary = "Cancelar factura", description = "Cambia el estado de la factura a cancelada.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun cancel(@CurrentOrganizationId organizationId: UUID, @PathVariable id: UUID): ResponseEntity<InvoiceResponse> {
        val inv = service.cancel(organizationId, id) ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(InvoiceResponse.from(inv))
    }
}
