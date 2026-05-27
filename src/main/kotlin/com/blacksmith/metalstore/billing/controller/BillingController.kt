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
import java.util.UUID

@RestController
@RequestMapping("/api/billing")
class BillingController(
    private val service: BillingService
) {
    // ── Price List ──────────────────────────────────────────────
    @GetMapping("/prices")
    fun listPrices(@CurrentTenantId tenantId: UUID, @PageableDefault(size = 20) pageable: Pageable): Page<PriceResponse> =
        service.listPrices(tenantId, pageable).map { PriceResponse.from(it) }

    @PostMapping("/prices")
    @ResponseStatus(HttpStatus.CREATED)
    fun upsertPrice(@CurrentTenantId tenantId: UUID, @Valid @RequestBody request: UpsertPriceRequest): PriceResponse {
        val saved = service.upsertPrice(request.toEntity(tenantId))
        return PriceResponse.from(saved)
    }

    @DeleteMapping("/prices/{id}")
    fun deletePrice(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<Unit> {
        val deleted = service.deletePrice(tenantId, id)
        return if (deleted) ResponseEntity.noContent().build()
        else ResponseEntity.notFound().build()
    }

    // ── Invoices ────────────────────────────────────────────────
    @GetMapping("/invoices")
    fun listInvoices(@CurrentTenantId tenantId: UUID, @PageableDefault(size = 20) pageable: Pageable): Page<InvoiceResponse> =
        service.listInvoices(tenantId, pageable).map { InvoiceResponse.from(it) }

    @GetMapping("/invoices/{id}")
    fun getInvoice(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<InvoiceResponse> {
        val inv = service.findInvoice(tenantId, id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(InvoiceResponse.from(inv))
    }

    @GetMapping("/invoices/{id}/lines")
    fun getLines(@PathVariable id: UUID): List<LineResponse> =
        service.getLines(id).map { LineResponse.from(it) }

    @PostMapping("/invoices")
    @ResponseStatus(HttpStatus.CREATED)
    fun createDraft(
        @CurrentTenantId tenantId: UUID,
        @RequestParam customerName: String? = null,
        @RequestParam customerVat: String? = null
    ): InvoiceResponse = InvoiceResponse.from(service.createDraft(tenantId, customerName, customerVat))

    @PostMapping("/invoices/{id}/lines")
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
    fun removeLine(@CurrentTenantId tenantId: UUID, @PathVariable invoiceId: UUID, @PathVariable lineId: UUID) {
        service.removeLine(tenantId, invoiceId, lineId)
    }

    @PostMapping("/invoices/{id}/issue")
    fun issue(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<InvoiceResponse> {
        val inv = service.issue(tenantId, id) ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(InvoiceResponse.from(inv))
    }

    @PostMapping("/invoices/{id}/pay")
    fun markPaid(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<InvoiceResponse> {
        val inv = service.markPaid(tenantId, id) ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(InvoiceResponse.from(inv))
    }

    @PostMapping("/invoices/{id}/cancel")
    fun cancel(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<InvoiceResponse> {
        val inv = service.cancel(tenantId, id) ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(InvoiceResponse.from(inv))
    }
}
