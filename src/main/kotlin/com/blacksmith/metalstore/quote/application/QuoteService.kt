package com.blacksmith.metalstore.quote.application

import com.blacksmith.metalstore.auth.audit.AuditLogger
import com.blacksmith.metalstore.quote.domain.entity.Quote
import com.blacksmith.metalstore.quote.domain.entity.QuoteLine
import com.blacksmith.metalstore.quote.domain.entity.QuoteStatus
import com.blacksmith.metalstore.quote.domain.repository.QuoteLineRepository
import com.blacksmith.metalstore.quote.domain.repository.QuoteRepository
import com.blacksmith.metalstore.shared.NumberSequence
import com.blacksmith.metalstore.shared.NumberSequenceId
import com.blacksmith.metalstore.shared.NumberSequenceRepository
import com.blacksmith.metalstore.shared.exception.ResourceNotFoundException
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
    private val numberSequenceRepo: NumberSequenceRepository,
    private val audit: AuditLogger
) {
    @Transactional(readOnly = true)
    fun listQuotes(organizationId: UUID, pageable: Pageable, q: String? = null, status: QuoteStatus? = null, clientId: UUID? = null): Page<Quote> =
        quoteRepo.findAllFiltered(organizationId, q?.lowercase() ?: "", status, clientId, pageable)

    @Transactional(readOnly = true)
    fun findQuote(organizationId: UUID, quoteId: UUID): Quote =
        quoteRepo.findById(quoteId).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("Quote", quoteId) }

    @Transactional(readOnly = true)
    fun getLines(quoteId: UUID): List<QuoteLine> =
        lineRepo.findByQuoteIdOrderByLineNumber(quoteId)

    fun createDraft(organizationId: UUID, quote: Quote): Quote {
        val number = nextQuoteNumber(organizationId)
        val saved = quoteRepo.save(Quote(
            id = quote.id,
            organizationId = quote.organizationId,
            quoteNumber = number,
            clientId = quote.clientId,
            customerName = quote.customerName,
            customerVat = quote.customerVat,
            customerAddress = quote.customerAddress,
            issueDate = quote.issueDate,
            validUntil = quote.validUntil,
            status = quote.status,
            subtotal = quote.subtotal,
            vatTotal = quote.vatTotal,
            total = quote.total,
            notes = quote.notes
        ))
        audit.log(AuditLogger.AuditEvent(
            action = "QUOTE_CREATED",
            entityType = "Quote",
            entityId = saved.id.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("number" to number, "customerName" to (quote.customerName ?: ""))
        ))
        return saved
    }

    fun addLine(organizationId: UUID, quoteId: UUID, line: QuoteLine): QuoteLine {
        val quote = quoteRepo.findById(quoteId).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("Quote", quoteId) }
        require(quote.status == QuoteStatus.DRAFT) { "Quote $quoteId is not in DRAFT status" }
        val computedTotal = line.quantity * line.unitPrice
        val lineWithTotal = QuoteLine(
            id = line.id,
            quoteId = quoteId,
            lineNumber = line.lineNumber,
            profileId = line.profileId,
            itemId = line.itemId,
            description = line.description,
            quantity = line.quantity,
            unitPrice = line.unitPrice,
            vatRate = line.vatRate,
            totalPrice = computedTotal
        )
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

    fun removeLine(organizationId: UUID, quoteId: UUID, lineId: UUID) {
        val quote = quoteRepo.findById(quoteId).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("Quote", quoteId) }
        require(quote.status == QuoteStatus.DRAFT) { "Quote $quoteId is not in DRAFT status" }
        val line = lineRepo.findById(lineId).filter { it.quoteId == quoteId }
            .orElseThrow { ResourceNotFoundException("QuoteLine", lineId) }
        lineRepo.delete(line)
        recalcTotals(quoteId)
        audit.log(AuditLogger.AuditEvent(
            action = "QUOTE_LINE_REMOVED",
            entityType = "QuoteLine",
            entityId = lineId.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("quoteId" to quoteId.toString())
        ))
    }

    fun issue(organizationId: UUID, quoteId: UUID): Quote {
        val quote = quoteRepo.findById(quoteId).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("Quote", quoteId) }
        require(quote.status.canTransitionTo(QuoteStatus.ISSUED)) { "Quote $quoteId cannot be issued from status ${quote.status}" }
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

    fun accept(organizationId: UUID, quoteId: UUID): Quote {
        val quote = quoteRepo.findById(quoteId).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("Quote", quoteId) }
        require(quote.status.canTransitionTo(QuoteStatus.ACCEPTED)) { "Quote $quoteId cannot be accepted from status ${quote.status}" }
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

    fun reject(organizationId: UUID, quoteId: UUID): Quote {
        val quote = quoteRepo.findById(quoteId).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("Quote", quoteId) }
        require(quote.status.canTransitionTo(QuoteStatus.REJECTED)) { "Quote $quoteId cannot be rejected from status ${quote.status}" }
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

    fun cancel(organizationId: UUID, quoteId: UUID): Quote {
        val quote = quoteRepo.findById(quoteId).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("Quote", quoteId) }
        require(quote.status.canTransitionTo(QuoteStatus.CANCELLED)) { "Quote $quoteId cannot be cancelled from status ${quote.status}" }
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
            quoteRepo.save(Quote(
                id = q.id,
                organizationId = q.organizationId,
                quoteNumber = q.quoteNumber,
                clientId = q.clientId,
                customerName = q.customerName,
                customerVat = q.customerVat,
                customerAddress = q.customerAddress,
                issueDate = q.issueDate,
                validUntil = q.validUntil,
                status = q.status,
                subtotal = subtotal,
                vatTotal = vatTotal,
                total = total,
                notes = q.notes
            ))
        }
    }

    fun update(organizationId: UUID, quoteId: UUID, customerName: String?, customerVat: String?, customerAddress: String?, validUntil: LocalDate?, notes: String?): Quote {
        val quote = quoteRepo.findById(quoteId).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("Quote", quoteId) }
        require(quote.status == QuoteStatus.DRAFT) { "Quote $quoteId is not in DRAFT status" }
        val merged = Quote(
            id = quote.id,
            organizationId = quote.organizationId,
            quoteNumber = quote.quoteNumber,
            clientId = quote.clientId,
            customerName = customerName ?: quote.customerName,
            customerVat = customerVat ?: quote.customerVat,
            customerAddress = customerAddress ?: quote.customerAddress,
            issueDate = quote.issueDate,
            validUntil = validUntil ?: quote.validUntil,
            status = quote.status,
            subtotal = quote.subtotal,
            vatTotal = quote.vatTotal,
            total = quote.total,
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
        val seq = numberSequenceRepo.findWithLock(organizationId, "PRES", year)
            .orElse(NumberSequence(NumberSequenceId(organizationId, "PRES", year), 0))
        seq.counter += 1
        numberSequenceRepo.save(seq)
        return "PRES-$year-${organizationId.toString().take(8).uppercase()}-${seq.counter}"
    }
}
