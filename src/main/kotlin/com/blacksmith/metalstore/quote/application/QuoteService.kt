package com.blacksmith.metalstore.quote.application

import com.blacksmith.metalstore.auth.audit.AuditLogger
import com.blacksmith.metalstore.quote.domain.entity.Quote
import com.blacksmith.metalstore.quote.domain.entity.QuoteLine
import com.blacksmith.metalstore.quote.domain.entity.QuoteStatus
import com.blacksmith.metalstore.quote.domain.repository.QuoteLineRepository
import com.blacksmith.metalstore.quote.domain.repository.QuoteRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Service
@Transactional
class QuoteService(
    private val quoteRepo: QuoteRepository,
    private val lineRepo: QuoteLineRepository,
    private val audit: AuditLogger
) {
    @Transactional(readOnly = true)
    fun listQuotes(organizationId: UUID, pageable: Pageable): Page<Quote> =
        quoteRepo.findByOrganizationIdOrderByIssueDateDesc(organizationId, pageable)

    @Transactional(readOnly = true)
    fun listQuotesByClient(organizationId: UUID, clientId: UUID, pageable: Pageable): Page<Quote> =
        quoteRepo.findByOrganizationIdAndClientIdOrderByIssueDateDesc(organizationId, clientId, pageable)

    @Transactional(readOnly = true)
    fun findQuote(organizationId: UUID, quoteId: UUID): Quote? =
        quoteRepo.findById(quoteId).filter { it.organizationId == organizationId }.orElse(null)

    @Transactional(readOnly = true)
    fun getLines(quoteId: UUID): List<QuoteLine> =
        lineRepo.findByQuoteIdOrderByLineNumber(quoteId)

    fun createDraft(organizationId: UUID, quote: Quote): Quote {
        val number = nextQuoteNumber(organizationId)
        val saved = quoteRepo.save(quote.copy(quoteNumber = number))
        audit.log(AuditLogger.AuditEvent(
            action = "QUOTE_CREATED",
            entityType = "Quote",
            entityId = saved.id.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("number" to number, "customerName" to (quote.customerName ?: ""))
        ))
        return saved
    }

    fun addLine(organizationId: UUID, quoteId: UUID, line: QuoteLine): QuoteLine? {
        val quote = quoteRepo.findById(quoteId).filter { it.organizationId == organizationId }.orElse(null) ?: return null
        if (quote.status != QuoteStatus.DRAFT) return null
        val computedTotal = line.quantity * line.unitPrice
        val lineWithTotal = line.copy(quoteId = quoteId, totalPrice = computedTotal)
        val saved = lineRepo.save(lineWithTotal)
        recalcTotals(quoteId)
        audit.log(AuditLogger.AuditEvent(
            action = "QUOTE_LINE_ADDED",
            entityType = "QuoteLine",
            entityId = saved.id.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("quoteId" to quoteId.toString(), "quantity" to saved.quantity.toString(), "unitPrice" to saved.unitPrice.toString())
        ))
        return saved
    }

    fun removeLine(organizationId: UUID, quoteId: UUID, lineId: UUID): Boolean {
        val quote = quoteRepo.findById(quoteId).filter { it.organizationId == organizationId }.orElse(null) ?: return false
        if (quote.status != QuoteStatus.DRAFT) return false
        val line = lineRepo.findById(lineId).filter { it.quoteId == quoteId }.orElse(null) ?: return false
        lineRepo.delete(line)
        recalcTotals(quoteId)
        audit.log(AuditLogger.AuditEvent(
            action = "QUOTE_LINE_REMOVED",
            entityType = "QuoteLine",
            entityId = lineId.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("quoteId" to quoteId.toString())
        ))
        return true
    }

    fun issue(organizationId: UUID, quoteId: UUID): Quote? {
        val quote = quoteRepo.findById(quoteId).filter { it.organizationId == organizationId }.orElse(null) ?: return null
        if (quote.status != QuoteStatus.DRAFT) return null
        quote.status = QuoteStatus.ISSUED
        val saved = quoteRepo.save(quote)
        audit.log(AuditLogger.AuditEvent(
            action = "QUOTE_ISSUED",
            entityType = "Quote",
            entityId = quoteId.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("number" to quote.quoteNumber)
        ))
        return saved
    }

    fun accept(organizationId: UUID, quoteId: UUID): Quote? {
        val quote = quoteRepo.findById(quoteId).filter { it.organizationId == organizationId }.orElse(null) ?: return null
        if (quote.status != QuoteStatus.ISSUED) return null
        quote.status = QuoteStatus.ACCEPTED
        val saved = quoteRepo.save(quote)
        audit.log(AuditLogger.AuditEvent(
            action = "QUOTE_ACCEPTED",
            entityType = "Quote",
            entityId = quoteId.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("number" to quote.quoteNumber)
        ))
        return saved
    }

    fun reject(organizationId: UUID, quoteId: UUID): Quote? {
        val quote = quoteRepo.findById(quoteId).filter { it.organizationId == organizationId }.orElse(null) ?: return null
        if (quote.status != QuoteStatus.ISSUED) return null
        quote.status = QuoteStatus.REJECTED
        val saved = quoteRepo.save(quote)
        audit.log(AuditLogger.AuditEvent(
            action = "QUOTE_REJECTED",
            entityType = "Quote",
            entityId = quoteId.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("number" to quote.quoteNumber)
        ))
        return saved
    }

    fun cancel(organizationId: UUID, quoteId: UUID): Quote? {
        val quote = quoteRepo.findById(quoteId).filter { it.organizationId == organizationId }.orElse(null) ?: return null
        if (quote.status == QuoteStatus.ACCEPTED || quote.status == QuoteStatus.REJECTED) return null
        quote.status = QuoteStatus.CANCELLED
        val saved = quoteRepo.save(quote)
        audit.log(AuditLogger.AuditEvent(
            action = "QUOTE_CANCELLED",
            entityType = "Quote",
            entityId = quoteId.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("number" to quote.quoteNumber)
        ))
        return saved
    }

    private fun recalcTotals(quoteId: UUID) {
        val lines = lineRepo.findByQuoteId(quoteId)
        val subtotal = lines.sumOf { it.totalPrice }
        val vatTotal = lines.sumOf { it.totalPrice * it.vatRate / BigDecimal("100") }
        val total = subtotal + vatTotal
        quoteRepo.findById(quoteId).ifPresent { q ->
            quoteRepo.save(q.copy(subtotal = subtotal, vatTotal = vatTotal, total = total))
        }
    }

    fun update(organizationId: UUID, quoteId: UUID, customerName: String?, customerVat: String?, customerAddress: String?, validUntil: LocalDate?, notes: String?): Quote? {
        val quote = quoteRepo.findById(quoteId).filter { it.organizationId == organizationId }.orElse(null) ?: return null
        if (quote.status != QuoteStatus.DRAFT) return null
        val merged = quote.copy(
            customerName = customerName ?: quote.customerName,
            customerVat = customerVat ?: quote.customerVat,
            customerAddress = customerAddress ?: quote.customerAddress,
            validUntil = validUntil ?: quote.validUntil,
            notes = notes ?: quote.notes
        )
        val saved = quoteRepo.save(merged)
        audit.log(AuditLogger.AuditEvent(
            action = "QUOTE_UPDATED",
            entityType = "Quote",
            entityId = quoteId.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("number" to quote.quoteNumber)
        ))
        return saved
    }

    private fun nextQuoteNumber(organizationId: UUID): String {
        val year = LocalDate.now().year
        val count = quoteRepo.countByOrganizationId(organizationId) + 1
        return "PRES-$year-${organizationId.toString().take(8).uppercase()}-$count"
    }
}
