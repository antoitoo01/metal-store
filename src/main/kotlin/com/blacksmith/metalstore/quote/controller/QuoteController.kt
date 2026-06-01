package com.blacksmith.metalstore.quote.controller

import com.blacksmith.metalstore.auth.config.CurrentTenantId
import com.blacksmith.metalstore.quote.application.QuoteService
import com.blacksmith.metalstore.quote.domain.dto.request.CreateQuoteLineRequest
import com.blacksmith.metalstore.quote.domain.dto.request.CreateQuoteRequest
import com.blacksmith.metalstore.quote.domain.dto.response.QuoteLineResponse
import com.blacksmith.metalstore.quote.domain.dto.response.QuoteResponse
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/quotes")
class QuoteController(
    private val service: QuoteService
) {
    @GetMapping
    fun list(@CurrentTenantId tenantId: UUID, @PageableDefault(size = 20) pageable: Pageable): Page<QuoteResponse> =
        service.listQuotes(tenantId, pageable).map { QuoteResponse.from(it) }

    @GetMapping("/{id}")
    fun get(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<QuoteResponse> {
        val quote = service.findQuote(tenantId, id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(QuoteResponse.from(quote))
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createDraft(
        @CurrentTenantId tenantId: UUID,
        @Valid @RequestBody request: CreateQuoteRequest
    ): QuoteResponse {
        val quote = service.createDraft(tenantId, request.toEntity(tenantId, ""))
        return QuoteResponse.from(quote)
    }

    @GetMapping("/{id}/lines")
    fun getLines(@PathVariable id: UUID): List<QuoteLineResponse> =
        service.getLines(id).map { QuoteLineResponse.from(it) }

    @PostMapping("/{id}/lines")
    fun addLine(
        @CurrentTenantId tenantId: UUID,
        @PathVariable id: UUID,
        @Valid @RequestBody request: CreateQuoteLineRequest
    ): ResponseEntity<QuoteLineResponse> {
        val saved = service.addLine(tenantId, id, request.toEntity(id))
            ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(QuoteLineResponse.from(saved))
    }

    @DeleteMapping("/{quoteId}/lines/{lineId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeLine(@CurrentTenantId tenantId: UUID, @PathVariable quoteId: UUID, @PathVariable lineId: UUID) {
        service.removeLine(tenantId, quoteId, lineId)
    }

    @PostMapping("/{id}/issue")
    fun issue(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<QuoteResponse> {
        val quote = service.issue(tenantId, id) ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(QuoteResponse.from(quote))
    }

    @PostMapping("/{id}/accept")
    fun accept(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<QuoteResponse> {
        val quote = service.accept(tenantId, id) ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(QuoteResponse.from(quote))
    }

    @PostMapping("/{id}/reject")
    fun reject(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<QuoteResponse> {
        val quote = service.reject(tenantId, id) ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(QuoteResponse.from(quote))
    }

    @PostMapping("/{id}/cancel")
    fun cancel(@CurrentTenantId tenantId: UUID, @PathVariable id: UUID): ResponseEntity<QuoteResponse> {
        val quote = service.cancel(tenantId, id) ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.ok(QuoteResponse.from(quote))
    }
}
