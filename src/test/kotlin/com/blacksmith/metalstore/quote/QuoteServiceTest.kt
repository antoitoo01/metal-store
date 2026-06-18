package com.blacksmith.metalstore.quote

import com.blacksmith.metalstore.auth.audit.AuditLogger
import com.blacksmith.metalstore.quote.application.QuoteService
import com.blacksmith.metalstore.shared.exception.ApiException
import com.blacksmith.metalstore.quote.domain.entity.Quote
import com.blacksmith.metalstore.quote.domain.entity.QuoteLine
import com.blacksmith.metalstore.quote.domain.entity.QuoteStatus
import com.blacksmith.metalstore.quote.domain.repository.QuoteLineRepository
import com.blacksmith.metalstore.quote.domain.repository.QuoteRepository
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
import java.time.LocalDate
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class QuoteServiceTest {

    @Autowired
    private lateinit var quoteRepo: QuoteRepository

    @Autowired
    private lateinit var lineRepo: QuoteLineRepository
    @Autowired
    private lateinit var numberSequenceRepo: NumberSequenceRepository

    private lateinit var service: QuoteService
    private val organizationId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        quoteRepo.deleteAll()
        lineRepo.deleteAll()
        service = QuoteService(quoteRepo, lineRepo, numberSequenceRepo, mock(AuditLogger::class.java))
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
    fun `add line to issued quote throws`() {
        val quote = service.createDraft(organizationId, Quote(organizationId = organizationId, quoteNumber = "", customerName = "Test"))
        service.issue(organizationId, quote.id)

        val line = QuoteLine(quoteId = quote.id, lineNumber = 1, description = "Perfil IPN 200", quantity = BigDecimal.ONE, unitPrice = BigDecimal.TEN, totalPrice = BigDecimal.ZERO)
        try {
            service.addLine(organizationId, quote.id, line)
            assert(false) { "Expected ApiException" }
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `issue transitions from draft`() {
        val quote = service.createDraft(organizationId, Quote(organizationId = organizationId, quoteNumber = "", customerName = "Test"))
        val issued = service.issue(organizationId, quote.id)

        assert(issued != null)
        assert(issued!!.status == QuoteStatus.ISSUED)
    }

    @Test
    fun `issue from issued throws`() {
        val quote = service.createDraft(organizationId, Quote(organizationId = organizationId, quoteNumber = "", customerName = "Test"))
        service.issue(organizationId, quote.id)

        try {
            service.issue(organizationId, quote.id)
            assert(false) { "Expected IllegalArgumentException" }
        } catch (e: IllegalArgumentException) {
            // expected
        }
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
    fun `accept from draft throws`() {
        val quote = service.createDraft(organizationId, Quote(organizationId = organizationId, quoteNumber = "", customerName = "Test"))
        try {
            service.accept(organizationId, quote.id)
            assert(false) { "Expected IllegalArgumentException" }
        } catch (e: IllegalArgumentException) {
            // expected
        }
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
    fun `cancel from accepted throws`() {
        val quote = service.createDraft(organizationId, Quote(organizationId = organizationId, quoteNumber = "", customerName = "Test"))
        service.issue(organizationId, quote.id)
        service.accept(organizationId, quote.id)
        try {
            service.cancel(organizationId, quote.id)
            assert(false) { "Expected IllegalArgumentException" }
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }
}