package com.blacksmith.metalstore.quote

import com.blacksmith.metalstore.auth.audit.AuditLogger
import com.blacksmith.metalstore.quote.application.QuoteService
import com.blacksmith.metalstore.quote.domain.entity.Quote
import com.blacksmith.metalstore.quote.domain.entity.QuoteLine
import com.blacksmith.metalstore.quote.domain.entity.QuoteStatus
import com.blacksmith.metalstore.quote.domain.repository.QuoteLineRepository
import com.blacksmith.metalstore.quote.domain.repository.QuoteRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
class QuoteServiceTest {

    @Autowired
    private lateinit var quoteRepo: QuoteRepository

    @Autowired
    private lateinit var lineRepo: QuoteLineRepository

    private lateinit var service: QuoteService
    private val organizationId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        quoteRepo.deleteAll()
        lineRepo.deleteAll()
        service = QuoteService(quoteRepo, lineRepo, mock(AuditLogger::class.java))
    }

    @Test
    fun `create and find quote`() {
        val saved = service.createDraft(organizationId, Quote(organizationId = organizationId, quoteNumber = "", customerName = "Cliente Test"))
        assert(saved.id != null)
        assert(saved.quoteNumber.startsWith("PRES-"))
        assert(saved.status == QuoteStatus.DRAFT)

        val found = service.findQuote(organizationId, saved.id)
        assert(found != null)
        assert(found!!.customerName == "Cliente Test")
    }

    @Test
    fun `list returns only quotes for organization`() {
        val otherTenant = UUID.randomUUID()
        service.createDraft(organizationId, Quote(organizationId = organizationId, quoteNumber = "", customerName = "A"))
        service.createDraft(otherTenant, Quote(organizationId = otherTenant, quoteNumber = "", customerName = "B"))

        val quotes = service.listQuotes(organizationId, PageRequest.of(0, 100))
        assert(quotes.totalElements == 1L)
    }

    @Test
    fun `add line to draft recalculates totals`() {
        val quote = service.createDraft(organizationId, Quote(organizationId = organizationId, quoteNumber = "", customerName = "Test"))
        val line = QuoteLine(quoteId = quote.id, lineNumber = 1, description = "Perfil IPN 200", quantity = BigDecimal("10"), unitPrice = BigDecimal("25.50"), totalPrice = BigDecimal.ZERO)
        service.addLine(organizationId, quote.id, line)

    val updated = service.findQuote(organizationId, quote.id)
    assert(updated!!.subtotal.compareTo(BigDecimal("255.00")) == 0)
    }

    @Test
    fun `add line to issued quote returns null`() {
        val quote = service.createDraft(organizationId, Quote(organizationId = organizationId, quoteNumber = "", customerName = "Test"))
        service.issue(organizationId, quote.id)

        val line = QuoteLine(quoteId = quote.id, lineNumber = 1, description = "Perfil IPN 200", quantity = BigDecimal.ONE, unitPrice = BigDecimal.TEN, totalPrice = BigDecimal.ZERO)
        val result = service.addLine(organizationId, quote.id, line)
        assert(result == null)
    }

    @Test
    fun `issue transitions from draft`() {
        val quote = service.createDraft(organizationId, Quote(organizationId = organizationId, quoteNumber = "", customerName = "Test"))
        val issued = service.issue(organizationId, quote.id)

        assert(issued != null)
        assert(issued!!.status == QuoteStatus.ISSUED)
    }

    @Test
    fun `issue from issued returns null`() {
        val quote = service.createDraft(organizationId, Quote(organizationId = organizationId, quoteNumber = "", customerName = "Test"))
        service.issue(organizationId, quote.id)

        val result = service.issue(organizationId, quote.id)
        assert(result == null)
    }

    @Test
    fun `accept transitions from issued`() {
        val quote = service.createDraft(organizationId, Quote(organizationId = organizationId, quoteNumber = "", customerName = "Test"))
        service.issue(organizationId, quote.id)
        val accepted = service.accept(organizationId, quote.id)

        assert(accepted != null)
        assert(accepted!!.status == QuoteStatus.ACCEPTED)
    }

    @Test
    fun `accept from draft returns null`() {
        val quote = service.createDraft(organizationId, Quote(organizationId = organizationId, quoteNumber = "", customerName = "Test"))
        val result = service.accept(organizationId, quote.id)
        assert(result == null)
    }

    @Test
    fun `reject transitions from issued`() {
        val quote = service.createDraft(organizationId, Quote(organizationId = organizationId, quoteNumber = "", customerName = "Test"))
        service.issue(organizationId, quote.id)
        val rejected = service.reject(organizationId, quote.id)

        assert(rejected != null)
        assert(rejected!!.status == QuoteStatus.REJECTED)
    }

    @Test
    fun `cancel works from draft or issued`() {
        val q1 = service.createDraft(organizationId, Quote(organizationId = organizationId, quoteNumber = "", customerName = "Test"))
        service.issue(organizationId, q1.id)
        val cancelled = service.cancel(organizationId, q1.id)
        assert(cancelled!!.status == QuoteStatus.CANCELLED)
    }

    @Test
    fun `cancel from accepted returns null`() {
        val quote = service.createDraft(organizationId, Quote(organizationId = organizationId, quoteNumber = "", customerName = "Test"))
        service.issue(organizationId, quote.id)
        service.accept(organizationId, quote.id)
        val result = service.cancel(organizationId, quote.id)
        assert(result == null)
    }
}