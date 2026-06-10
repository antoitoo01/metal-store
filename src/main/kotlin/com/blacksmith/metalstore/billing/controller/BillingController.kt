package com.blacksmith.metalstore.billing.controller

import com.blacksmith.metalstore.auth.config.CurrentTenantId
import com.blacksmith.metalstore.billing.application.BillingService
import com.blacksmith.metalstore.billing.domain.dto.request.CreateLineRequest
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
    fun listPrices(@CurrentTenantId tenantId: UUID, @PageableDefault(size = 20) pageable: Pageable): Page<PriceResponse> =
        service.listPrices(tenantId, pageable).map { PriceResponse.from(it) }

    @PostMapping("/prices")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear o actualizar precio", description = "Inserta o actualiza un precio.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Recurso creado"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun upsertPrice(@CurrentTenantId tenantId: UUID, @Valid @RequestBody request: UpsertPriceRequest): PriceResponse {
        val saved = service.upsertPrice(request.toEntity(tenantId))
        return PriceResponse.from(saved)
    }

    @DeleteMapping("/prices/{id}")
    @Operation(summary = "Eliminar precio", description = "Elimina un precio por su UUID.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Sin contenido"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun deletePrice(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<Unit> {
        val deleted = service.deletePrice(tenantId, id)
        return if (deleted) ResponseEntity.noContent().build()
        else ResponseEntity.notFound().build()
    }

    // ── Invoices ────────────────────────────────────────────────
    @GetMapping("/invoices")
    @Operation(summary = "Listar facturas", description = "Retorna una lista paginada de facturas.")
    @ApiResponse(responseCode = "200", description = "Operación exitosa")
    fun listInvoices(@CurrentTenantId tenantId: UUID, @PageableDefault(size = 20) pageable: Pageable, @RequestParam(name = "q", required = false) q: String?): Page<InvoiceResponse> =
        service.listInvoices(tenantId, pageable, q).map { InvoiceResponse.from(it) }

    @GetMapping("/invoices/{id}")
    @Operation(summary = "Obtener factura por ID", description = "Retorna los datos de una factura por su UUID.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun getInvoice(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<InvoiceResponse> {
        val inv = service.findInvoice(tenantId, id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(InvoiceResponse.from(inv))
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
        @CurrentTenantId tenantId: UUID,
        @RequestParam customerName: String? = null,
        @RequestParam customerVat: String? = null
    ): InvoiceResponse = InvoiceResponse.from(service.createDraft(tenantId, customerName, customerVat))

    @PostMapping("/invoices/{id}/lines")
    @Operation(summary = "Agregar línea a factura", description = "Agrega una nueva línea a una factura existente.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun addLine(
        @CurrentTenantId tenantId: UUID,
        @PathVariable id: UUID,
        @Valid @RequestBody request: CreateLineRequest
    ): ResponseEntity<LineResponse> {
        val saved = service.addLine(tenantId, id, request.toEntity(id))
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
    fun removeLine(@CurrentTenantId tenantId: UUID, @PathVariable invoiceId: UUID, @PathVariable lineId: UUID) {
        service.removeLine(tenantId, invoiceId, lineId)
    }

    @PostMapping("/invoices/{id}/issue")
    @Operation(summary = "Emitir factura", description = "Cambia el estado de la factura a emitida.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun issue(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<InvoiceResponse> {
        val inv = service.issue(tenantId, id) ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(InvoiceResponse.from(inv))
    }

    @PostMapping("/invoices/{id}/pay")
    @Operation(summary = "Pagar factura", description = "Cambia el estado de la factura a pagada.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun markPaid(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<InvoiceResponse> {
        val inv = service.markPaid(tenantId, id) ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(InvoiceResponse.from(inv))
    }

    @PostMapping("/invoices/{id}/cancel")
    @Operation(summary = "Cancelar factura", description = "Cambia el estado de la factura a cancelada.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun cancel(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<InvoiceResponse> {
        val inv = service.cancel(tenantId, id) ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(InvoiceResponse.from(inv))
    }
}
