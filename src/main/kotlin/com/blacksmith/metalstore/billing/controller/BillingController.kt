package com.blacksmith.metalstore.billing.controller

import com.blacksmith.metalstore.organization.config.CurrentOrganizationId
import com.blacksmith.metalstore.organization.config.RequiresRole
import com.blacksmith.metalstore.organization.domain.entity.OrganizationRole
import com.blacksmith.metalstore.billing.application.BillingService
import com.blacksmith.metalstore.billing.domain.dto.request.CreateLineRequest
import com.blacksmith.metalstore.billing.domain.dto.request.PriceUpdateRequest
import com.blacksmith.metalstore.billing.domain.dto.request.UpdateInvoiceRequest
import com.blacksmith.metalstore.billing.domain.dto.request.UpsertPriceRequest
import com.blacksmith.metalstore.billing.domain.dto.response.InvoiceResponse
import com.blacksmith.metalstore.billing.domain.dto.response.LineResponse
import com.blacksmith.metalstore.billing.domain.dto.response.PriceResponse
import com.blacksmith.metalstore.billing.domain.entity.InvoiceStatus
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
    @RequiresRole(OrganizationRole.STAFF)
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
    @RequiresRole(OrganizationRole.ADMIN)
    @Operation(summary = "Eliminar precio", description = "Elimina un precio por su UUID.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Sin contenido"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun deletePrice(@CurrentOrganizationId organizationId: UUID, @PathVariable id: UUID): ResponseEntity<Unit> {
        service.deletePrice(organizationId, id)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/prices/{id}")
    @RequiresRole(OrganizationRole.STAFF)
    @Operation(summary = "Actualizar precio", description = "Actualiza los campos de un precio existente.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun updatePrice(
        @CurrentOrganizationId organizationId: UUID,
        @PathVariable id: UUID,
        @Valid @RequestBody request: PriceUpdateRequest
    ): PriceResponse =
        PriceResponse.from(service.updatePrice(organizationId, id, request.unitPrice, request.validFrom, request.validTo, request.notes))

    // ── Invoices ────────────────────────────────────────────────
    @GetMapping("/invoices")
    @Operation(summary = "Listar facturas", description = "Retorna una lista paginada de facturas.")
    @ApiResponse(responseCode = "200", description = "Operación exitosa")
    fun listInvoices(
        @CurrentOrganizationId organizationId: UUID,
        @PageableDefault(size = 20) pageable: Pageable,
        @RequestParam(name = "q", required = false) q: String?,
        @RequestParam(name = "status", required = false) status: InvoiceStatus?
    ): Page<InvoiceResponse> =
        service.listInvoices(organizationId, pageable, q, status).map { InvoiceResponse.from(it) }

    @GetMapping("/invoices/{id}")
    @Operation(summary = "Obtener factura por ID", description = "Retorna los datos de una factura por su UUID.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun getInvoice(@CurrentOrganizationId organizationId: UUID, @PathVariable id: UUID): InvoiceResponse =
        InvoiceResponse.from(service.findInvoice(organizationId, id))

    @PutMapping("/invoices/{id}")
    @RequiresRole(OrganizationRole.STAFF)
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
    ): InvoiceResponse =
        InvoiceResponse.from(service.update(organizationId, id, request.customerName, request.customerVat, request.customerAddress, request.notes))

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
    @RequiresRole(OrganizationRole.STAFF)
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
    @RequiresRole(OrganizationRole.STAFF)
    @Operation(summary = "Agregar línea a factura", description = "Agrega una nueva línea a una factura existente.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun addLine(
        @CurrentOrganizationId organizationId: UUID,
        @PathVariable id: UUID,
        @Valid @RequestBody request: CreateLineRequest
    ): LineResponse =
        LineResponse.from(service.addLine(organizationId, id, request.toEntity(id)))

    @DeleteMapping("/invoices/{invoiceId}/lines/{lineId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequiresRole(OrganizationRole.STAFF)
    @Operation(summary = "Eliminar línea de factura", description = "Elimina una línea de una factura.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Sin contenido"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun removeLine(@CurrentOrganizationId organizationId: UUID, @PathVariable invoiceId: UUID, @PathVariable lineId: UUID) {
        service.removeLine(organizationId, invoiceId, lineId)
    }

    @PostMapping("/invoices/{id}/issue")
    @RequiresRole(OrganizationRole.STAFF)
    @Operation(summary = "Emitir factura", description = "Cambia el estado de la factura a emitida.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun issue(@CurrentOrganizationId organizationId: UUID, @PathVariable id: UUID): InvoiceResponse =
        InvoiceResponse.from(service.issue(organizationId, id))

    @PostMapping("/invoices/{id}/pay")
    @RequiresRole(OrganizationRole.STAFF)
    @Operation(summary = "Pagar factura", description = "Cambia el estado de la factura a pagada.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun markPaid(@CurrentOrganizationId organizationId: UUID, @PathVariable id: UUID): InvoiceResponse =
        InvoiceResponse.from(service.markPaid(organizationId, id))

    @PostMapping("/invoices/{id}/cancel")
    @RequiresRole(OrganizationRole.STAFF)
    @Operation(summary = "Cancelar factura", description = "Cambia el estado de la factura a cancelada.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun cancel(@CurrentOrganizationId organizationId: UUID, @PathVariable id: UUID): InvoiceResponse =
        InvoiceResponse.from(service.cancel(organizationId, id))
}
