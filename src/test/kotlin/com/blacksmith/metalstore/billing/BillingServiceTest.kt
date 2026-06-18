package com.blacksmith.metalstore.billing

import com.blacksmith.metalstore.auth.audit.AuditLogger
import com.blacksmith.metalstore.billing.application.BillingService
import com.blacksmith.metalstore.shared.exception.ApiException
import com.blacksmith.metalstore.billing.domain.entity.*
import com.blacksmith.metalstore.billing.domain.repository.InvoiceLineRepository
import com.blacksmith.metalstore.billing.domain.repository.InvoiceRepository
import com.blacksmith.metalstore.billing.domain.repository.PriceListRepository
import com.blacksmith.metalstore.shared.NumberSequenceRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BillingServiceTest {

    @Autowired
    private lateinit var priceListRepo: PriceListRepository
    @Autowired
    private lateinit var invoiceRepo: InvoiceRepository
    @Autowired
    private lateinit var invoiceLineRepo: InvoiceLineRepository
    @Autowired
    private lateinit var numberSequenceRepo: NumberSequenceRepository

    private lateinit var service: BillingService
    private val organizationId = UUID.randomUUID()
    private val profileId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        priceListRepo.deleteAll()
        invoiceRepo.deleteAll()
        invoiceLineRepo.deleteAll()
        service = BillingService(priceListRepo, invoiceRepo, invoiceLineRepo, numberSequenceRepo, mock(AuditLogger::class.java))
    }

    @Test
    fun `create and list price list items`() {
        val price = service.upsertPrice(
            PriceListItem(organizationId = organizationId, profileId = profileId, unitPrice = BigDecimal("150.00"))
        )
        assert(price.id != null)
        assert(service.listPrices(organizationId, PageRequest.of(0, 100)).totalElements == 1L)
    }

    @Test
    fun `create draft invoice`() {
        val inv = service.createDraft(organizationId, "Cliente Test", "B12345678")
        assert(inv.status == InvoiceStatus.DRAFT)
        assert(inv.invoiceNumber.startsWith("FAC-"))
    }

    @Test
    fun `add line to draft recalculates totals`() {
        val inv = service.createDraft(organizationId)
        val line = InvoiceLine(
            invoiceId = inv.id,
            lineNumber = 1,
            description = "Viga HEB200",
            profileId = profileId,
            quantity = BigDecimal("150.00"),
            unitPrice = BigDecimal("2.50"),
            totalPrice = BigDecimal("375.00")
        )
        service.addLine(organizationId, inv.id, line)

        val updated = service.findInvoice(organizationId, inv.id)!!
        assert(updated.subtotal.compareTo(BigDecimal("375.00")) == 0)
        assert(updated.total > BigDecimal.ZERO)
    }

    @Test
    fun `issue invoice changes status`() {
        val inv = service.createDraft(organizationId)
        val issued = service.issue(organizationId, inv.id)!!
        assert(issued.status == InvoiceStatus.ISSUED)
    }

    @Test
    fun `cannot add lines to issued invoice`() {
        val inv = service.createDraft(organizationId)
        service.issue(organizationId, inv.id)
        val line = InvoiceLine(
            invoiceId = inv.id, lineNumber = 1, description = "Test",
            quantity = BigDecimal.ONE, unitPrice = BigDecimal.TEN, totalPrice = BigDecimal.TEN
        )
        try {
            service.addLine(organizationId, inv.id, line)
            assert(false) { "Expected IllegalArgumentException" }
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `mark paid transitions from issued`() {
        val inv = service.createDraft(organizationId)
        service.issue(organizationId, inv.id)
        val paid = service.markPaid(organizationId, inv.id)!!
        assert(paid.status == InvoiceStatus.PAID)
    }

    @Test
    fun `cancel transitions from draft`() {
        val inv = service.createDraft(organizationId)
        val cancelled = service.cancel(organizationId, inv.id)!!
        assert(cancelled.status == InvoiceStatus.CANCELLED)
    }
}