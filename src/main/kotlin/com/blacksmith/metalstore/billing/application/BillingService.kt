package com.blacksmith.metalstore.billing.application

import com.blacksmith.metalstore.auth.audit.AuditLogger
import com.blacksmith.metalstore.billing.domain.entity.Invoice
import com.blacksmith.metalstore.billing.domain.entity.InvoiceLine
import com.blacksmith.metalstore.billing.domain.entity.InvoiceStatus
import com.blacksmith.metalstore.billing.domain.entity.PriceListItem
import com.blacksmith.metalstore.billing.domain.repository.InvoiceLineRepository
import com.blacksmith.metalstore.billing.domain.repository.InvoiceRepository
import com.blacksmith.metalstore.billing.domain.repository.PriceListRepository
import com.blacksmith.metalstore.shared.NumberSequence
import com.blacksmith.metalstore.shared.NumberSequenceId
import com.blacksmith.metalstore.shared.NumberSequenceRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Service
@Transactional
class BillingService(
    private val priceListRepo: PriceListRepository,
    private val invoiceRepo: InvoiceRepository,
    private val invoiceLineRepo: InvoiceLineRepository,
    private val numberSequenceRepo: NumberSequenceRepository,
    private val audit: AuditLogger
) {
    // ── Price List ──────────────────────────────────────────────
    @Transactional(readOnly = true)
    fun listPrices(organizationId: UUID, pageable: Pageable): Page<PriceListItem> =
        priceListRepo.findByOrganizationId(organizationId, pageable)

    fun upsertPrice(item: PriceListItem): PriceListItem {
        val saved = priceListRepo.save(item)
        audit.log(AuditLogger.AuditEvent(
            action = "PRICE_UPSERT",
            entityType = "PriceListItem",
            entityId = saved.id.toString(),
            organizationId = saved.organizationId.toString(),
            details = mapOf("profileId" to (saved.profileId?.toString()), "unitPrice" to saved.unitPrice.toString())
        ))
        return saved
    }

    fun deletePrice(organizationId: UUID, id: UUID): Boolean {
        val p = priceListRepo.findById(id).filter { it.organizationId == organizationId }.orElse(null) ?: return false
        priceListRepo.delete(p)
        audit.log(AuditLogger.AuditEvent(
            action = "PRICE_DELETE",
            entityType = "PriceListItem",
            entityId = id.toString(),
            organizationId = organizationId.toString()
        ))
        return true
    }

    fun updatePrice(organizationId: UUID, priceId: UUID, unitPrice: BigDecimal?, validFrom: LocalDate?, validTo: LocalDate?, notes: String?): PriceListItem? {
        val existing = priceListRepo.findById(priceId).filter { it.organizationId == organizationId }.orElse(null) ?: return null
        val merged = existing.copy(
            unitPrice = unitPrice ?: existing.unitPrice,
            validFrom = validFrom ?: existing.validFrom,
            validTo = validTo ?: existing.validTo,
            notes = notes ?: existing.notes
        )
        val saved = priceListRepo.save(merged)
        audit.log(AuditLogger.AuditEvent(
            action = "PRICE_UPDATED",
            entityType = "PriceListItem",
            entityId = priceId.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("unitPrice" to saved.unitPrice.toString())
        ))
        return saved
    }

    // ── Invoices ────────────────────────────────────────────────
    @Transactional(readOnly = true)
    fun listInvoices(organizationId: UUID, pageable: Pageable, q: String? = null, status: InvoiceStatus? = null): Page<Invoice> =
        invoiceRepo.findAllFiltered(organizationId, q, status, pageable)

    @Transactional(readOnly = true)
    fun findInvoice(organizationId: UUID, invoiceId: UUID): Invoice? =
        invoiceRepo.findById(invoiceId).filter { it.organizationId == organizationId }.orElse(null)

    @Transactional(readOnly = true)
    fun getLines(invoiceId: UUID): List<InvoiceLine> =
        invoiceLineRepo.findByInvoiceIdOrderByLineNumber(invoiceId)

    fun createDraft(organizationId: UUID, customerName: String? = null, customerVat: String? = null): Invoice {
        val number = nextInvoiceNumber(organizationId)
        val invoice = Invoice(
            organizationId = organizationId,
            invoiceNumber = number,
            customerName = customerName,
            customerVat = customerVat,
            status = InvoiceStatus.DRAFT
        )
        val saved = invoiceRepo.save(invoice)
        audit.log(AuditLogger.AuditEvent(
            action = "INVOICE_CREATED",
            entityType = "Invoice",
            entityId = saved.id.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("number" to number, "customerName" to customerName)
        ))
        return saved
    }

    fun addLine(organizationId: UUID, invoiceId: UUID, line: InvoiceLine): InvoiceLine? {
        val invoice = invoiceRepo.findById(invoiceId).filter { it.organizationId == organizationId }.orElse(null) ?: return null
        if (invoice.status != InvoiceStatus.DRAFT) return null
        val computedTotal = line.quantity * line.unitPrice
        val lineWithTotal = line.copy(invoiceId = invoiceId, totalPrice = computedTotal)
        val saved = invoiceLineRepo.save(lineWithTotal)
        recalcTotals(invoiceId)
        audit.log(AuditLogger.AuditEvent(
            action = "INVOICE_LINE_ADDED",
            entityType = "InvoiceLine",
            entityId = saved.id.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("invoiceId" to invoiceId.toString(), "quantity" to saved.quantity.toString(), "unitPrice" to saved.unitPrice.toString())
        ))
        return saved
    }

    fun removeLine(organizationId: UUID, invoiceId: UUID, lineId: UUID): Boolean {
        val invoice = invoiceRepo.findById(invoiceId).filter { it.organizationId == organizationId }.orElse(null) ?: return false
        if (invoice.status != InvoiceStatus.DRAFT) return false
        val line = invoiceLineRepo.findById(lineId).filter { it.invoiceId == invoiceId }.orElse(null) ?: return false
        invoiceLineRepo.delete(line)
        recalcTotals(invoiceId)
        audit.log(AuditLogger.AuditEvent(
            action = "INVOICE_LINE_REMOVED",
            entityType = "InvoiceLine",
            entityId = lineId.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("invoiceId" to invoiceId.toString())
        ))
        return true
    }

    fun issue(organizationId: UUID, invoiceId: UUID): Invoice? {
        val invoice = invoiceRepo.findById(invoiceId).filter { it.organizationId == organizationId }.orElse(null) ?: return null
        if (invoice.status != InvoiceStatus.DRAFT) return null
        invoice.status = InvoiceStatus.ISSUED
        val saved = invoiceRepo.save(invoice)
        audit.log(AuditLogger.AuditEvent(
            action = "INVOICE_ISSUED",
            entityType = "Invoice",
            entityId = invoiceId.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("number" to invoice.invoiceNumber)
        ))
        return saved
    }

    fun markPaid(organizationId: UUID, invoiceId: UUID): Invoice? {
        val invoice = invoiceRepo.findById(invoiceId).filter { it.organizationId == organizationId }.orElse(null) ?: return null
        if (invoice.status != InvoiceStatus.ISSUED) return null
        invoice.status = InvoiceStatus.PAID
        val saved = invoiceRepo.save(invoice)
        audit.log(AuditLogger.AuditEvent(
            action = "INVOICE_PAID",
            entityType = "Invoice",
            entityId = invoiceId.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("number" to invoice.invoiceNumber)
        ))
        return saved
    }

    fun cancel(organizationId: UUID, invoiceId: UUID): Invoice? {
        val invoice = invoiceRepo.findById(invoiceId).filter { it.organizationId == organizationId }.orElse(null) ?: return null
        if (invoice.status == InvoiceStatus.PAID) return null
        invoice.status = InvoiceStatus.CANCELLED
        val saved = invoiceRepo.save(invoice)
        audit.log(AuditLogger.AuditEvent(
            action = "INVOICE_CANCELLED",
            entityType = "Invoice",
            entityId = invoiceId.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("number" to invoice.invoiceNumber)
        ))
        return saved
    }

    // ── Internal ────────────────────────────────────────────────
    fun update(organizationId: UUID, invoiceId: UUID, customerName: String?, customerVat: String?, customerAddress: String?, notes: String?): Invoice? {
        val invoice = invoiceRepo.findById(invoiceId).filter { it.organizationId == organizationId }.orElse(null) ?: return null
        if (invoice.status != InvoiceStatus.DRAFT) return null
        val merged = invoice.copy(
            customerName = customerName ?: invoice.customerName,
            customerVat = customerVat ?: invoice.customerVat,
            customerAddress = customerAddress ?: invoice.customerAddress,
            notes = notes ?: invoice.notes
        )
        val saved = invoiceRepo.save(merged)
        audit.log(AuditLogger.AuditEvent(
            action = "INVOICE_UPDATED",
            entityType = "Invoice",
            entityId = invoiceId.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("number" to invoice.invoiceNumber)
        ))
        return saved
    }

    private fun recalcTotals(invoiceId: UUID) {
        val lines = invoiceLineRepo.findByInvoiceId(invoiceId)
        val subtotal = lines.sumOf { it.totalPrice }
        val vatTotal = lines.sumOf { it.totalPrice * it.vatRate / BigDecimal("100") }
        val total = subtotal + vatTotal
        invoiceRepo.findById(invoiceId).ifPresent { inv ->
            invoiceRepo.save(inv.copy(subtotal = subtotal, vatTotal = vatTotal, total = total))
        }
    }

    private fun nextInvoiceNumber(organizationId: UUID): String {
        val year = LocalDate.now().year
        val seq = numberSequenceRepo.findWithLock(organizationId, "FAC", year)
            .orElse(NumberSequence(NumberSequenceId(organizationId, "FAC", year), 0))
        seq.counter += 1
        numberSequenceRepo.save(seq)
        return "FAC-$year-${organizationId.toString().take(8).uppercase()}-${seq.counter}"
    }
}
