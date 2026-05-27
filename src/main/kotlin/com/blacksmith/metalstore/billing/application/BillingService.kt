package com.blacksmith.metalstore.billing.application

import com.blacksmith.metalstore.billing.domain.entity.Invoice
import com.blacksmith.metalstore.billing.domain.entity.InvoiceLine
import com.blacksmith.metalstore.billing.domain.entity.InvoiceStatus
import com.blacksmith.metalstore.billing.domain.entity.PriceListItem
import com.blacksmith.metalstore.billing.domain.repository.InvoiceLineRepository
import com.blacksmith.metalstore.billing.domain.repository.InvoiceRepository
import com.blacksmith.metalstore.billing.domain.repository.PriceListRepository
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
    private val invoiceLineRepo: InvoiceLineRepository
) {
    // ── Price List ──────────────────────────────────────────────
    @Transactional(readOnly = true)
    fun listPrices(tenantId: UUID, pageable: Pageable): Page<PriceListItem> =
        priceListRepo.findByTenantId(tenantId, pageable)

    fun upsertPrice(item: PriceListItem): PriceListItem = priceListRepo.save(item)

    fun deletePrice(tenantId: UUID, id: UUID): Boolean {
        val p = priceListRepo.findById(id).filter { it.tenantId == tenantId }.orElse(null) ?: return false
        priceListRepo.delete(p)
        return true
    }

    // ── Invoices ────────────────────────────────────────────────
    @Transactional(readOnly = true)
    fun listInvoices(tenantId: UUID, pageable: Pageable): Page<Invoice> =
        invoiceRepo.findByTenantIdOrderByIssueDateDesc(tenantId, pageable)

    @Transactional(readOnly = true)
    fun findInvoice(tenantId: UUID, invoiceId: UUID): Invoice? =
        invoiceRepo.findById(invoiceId).filter { it.tenantId == tenantId }.orElse(null)

    @Transactional(readOnly = true)
    fun getLines(invoiceId: UUID): List<InvoiceLine> =
        invoiceLineRepo.findByInvoiceIdOrderByLineNumber(invoiceId)

    fun createDraft(tenantId: UUID, customerName: String? = null, customerVat: String? = null): Invoice {
        val number = nextInvoiceNumber(tenantId)
        val invoice = Invoice(
            tenantId = tenantId,
            invoiceNumber = number,
            customerName = customerName,
            customerVat = customerVat,
            status = InvoiceStatus.DRAFT
        )
        return invoiceRepo.save(invoice)
    }

    fun addLine(tenantId: UUID, invoiceId: UUID, line: InvoiceLine): InvoiceLine? {
        val invoice = invoiceRepo.findById(invoiceId).filter { it.tenantId == tenantId }.orElse(null) ?: return null
        if (invoice.status != InvoiceStatus.DRAFT) return null
        val computedTotal = line.quantity * line.unitPrice
        val lineWithTotal = line.copy(invoiceId = invoiceId, totalPrice = computedTotal)
        val saved = invoiceLineRepo.save(lineWithTotal)
        recalcTotals(invoiceId)
        return saved
    }

    fun removeLine(tenantId: UUID, invoiceId: UUID, lineId: UUID): Boolean {
        val invoice = invoiceRepo.findById(invoiceId).filter { it.tenantId == tenantId }.orElse(null) ?: return false
        if (invoice.status != InvoiceStatus.DRAFT) return false
        val line = invoiceLineRepo.findById(lineId).filter { it.invoiceId == invoiceId }.orElse(null) ?: return false
        invoiceLineRepo.delete(line)
        recalcTotals(invoiceId)
        return true
    }

    fun issue(tenantId: UUID, invoiceId: UUID): Invoice? {
        val invoice = invoiceRepo.findById(invoiceId).filter { it.tenantId == tenantId }.orElse(null) ?: return null
        if (invoice.status != InvoiceStatus.DRAFT) return null
        invoice.status = InvoiceStatus.ISSUED
        return invoiceRepo.save(invoice)
    }

    fun markPaid(tenantId: UUID, invoiceId: UUID): Invoice? {
        val invoice = invoiceRepo.findById(invoiceId).filter { it.tenantId == tenantId }.orElse(null) ?: return null
        if (invoice.status != InvoiceStatus.ISSUED) return null
        invoice.status = InvoiceStatus.PAID
        return invoiceRepo.save(invoice)
    }

    fun cancel(tenantId: UUID, invoiceId: UUID): Invoice? {
        val invoice = invoiceRepo.findById(invoiceId).filter { it.tenantId == tenantId }.orElse(null) ?: return null
        if (invoice.status == InvoiceStatus.PAID) return null
        invoice.status = InvoiceStatus.CANCELLED
        return invoiceRepo.save(invoice)
    }

    // ── Internal ────────────────────────────────────────────────
    private fun recalcTotals(invoiceId: UUID) {
        val lines = invoiceLineRepo.findByInvoiceId(invoiceId)
        val subtotal = lines.sumOf { it.totalPrice }
        val vatTotal = lines.sumOf { it.totalPrice * it.vatRate / BigDecimal("100") }
        val total = subtotal + vatTotal
        invoiceRepo.findById(invoiceId).ifPresent { inv ->
            invoiceRepo.save(inv.copy(subtotal = subtotal, vatTotal = vatTotal, total = total))
        }
    }

    private fun nextInvoiceNumber(tenantId: UUID): String {
        val year = LocalDate.now().year
        val count = invoiceRepo.countByTenantId(tenantId) + 1
        return "FAC-$year-${tenantId.toString().take(8).uppercase()}-$count"
    }
}
