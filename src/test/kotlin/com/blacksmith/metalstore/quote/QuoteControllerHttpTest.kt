package com.blacksmith.metalstore.quote

import com.blacksmith.metalstore.quote.domain.entity.Quote
import com.blacksmith.metalstore.quote.domain.entity.QuoteLine
import com.blacksmith.metalstore.quote.domain.repository.QuoteLineRepository
import com.blacksmith.metalstore.quote.domain.repository.QuoteRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.hamcrest.Matchers.startsWith
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QuoteControllerHttpTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var quoteRepo: QuoteRepository

    @Autowired
    private lateinit var lineRepo: QuoteLineRepository

    private val organizationId = UUID.randomUUID()
    private val otherOrganizationId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        lineRepo.deleteAll()
        quoteRepo.deleteAll()
    }

    @Test
    fun `list returns paginated quotes for organization`() {
        quoteRepo.save(Quote(organizationId = organizationId, quoteNumber = "PRES-001", customerName = "Cliente A"))
        quoteRepo.save(Quote(organizationId = organizationId, quoteNumber = "PRES-002", customerName = "Cliente B"))
        quoteRepo.save(Quote(organizationId = otherOrganizationId, quoteNumber = "PRES-003", customerName = "Otro"))

        mockMvc.perform(get("/api/quotes")
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page.totalElements").value(2))
    }

    @Test
    fun `create draft quote`() {
        val body = """{"customerName":"Cliente Test","notes":"Presupuesto urgente"}"""

        mockMvc.perform(post("/api/quotes")
            .header("X-Organization-Id", organizationId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.customerName").value("Cliente Test"))
            .andExpect(jsonPath("$.status").value("DRAFT"))
            .andExpect(jsonPath("$.quoteNumber").value(startsWith("PRES-")))
    }

    @Test
    fun `get returns quote by id`() {
        val quote = quoteRepo.save(Quote(organizationId = organizationId, quoteNumber = "PRES-001"))

        mockMvc.perform(get("/api/quotes/{id}", quote.id)
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(quote.id.toString()))
    }

    @Test
    fun `get returns 404 for non-existent id`() {
        mockMvc.perform(get("/api/quotes/{id}", UUID.randomUUID())
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `add line to draft recalculates totals`() {
        val quote = quoteRepo.save(Quote(organizationId = organizationId, quoteNumber = "PRES-001"))
        val body = """{"lineNumber":1,"description":"Perfil IPN 200","quantity":10,"unitPrice":25.50}"""

        mockMvc.perform(post("/api/quotes/{id}/lines", quote.id)
            .header("X-Organization-Id", organizationId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.description").value("Perfil IPN 200"))
            .andExpect(jsonPath("$.totalPrice").value(255.00))

        mockMvc.perform(get("/api/quotes/{id}", quote.id)
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.subtotal").value(255.00))
    }

    @Test
    fun `issue transitions quote`() {
        val quote = quoteRepo.save(Quote(organizationId = organizationId, quoteNumber = "PRES-001"))

        mockMvc.perform(post("/api/quotes/{id}/issue", quote.id)
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("ISSUED"))
    }

    @Test
    fun `issue from issued returns 400`() {
        val quote = quoteRepo.save(Quote(organizationId = organizationId, quoteNumber = "PRES-001", status = com.blacksmith.metalstore.quote.domain.entity.QuoteStatus.ISSUED))

        mockMvc.perform(post("/api/quotes/{id}/issue", quote.id)
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `issue then accept`() {
        val quote = quoteRepo.save(Quote(organizationId = organizationId, quoteNumber = "PRES-001"))

        mockMvc.perform(post("/api/quotes/{id}/issue", quote.id)
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isOk)

        mockMvc.perform(post("/api/quotes/{id}/accept", quote.id)
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("ACCEPTED"))
    }

    @Test
    fun `issue then reject`() {
        val quote = quoteRepo.save(Quote(organizationId = organizationId, quoteNumber = "PRES-001"))

        mockMvc.perform(post("/api/quotes/{id}/issue", quote.id)
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isOk)

        mockMvc.perform(post("/api/quotes/{id}/reject", quote.id)
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("REJECTED"))
    }

    @Test
    fun `cancel from draft`() {
        val quote = quoteRepo.save(Quote(organizationId = organizationId, quoteNumber = "PRES-001"))

        mockMvc.perform(post("/api/quotes/{id}/cancel", quote.id)
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("CANCELLED"))
    }

    @Test
    fun `cancel from accepted returns 400`() {
        val quote = quoteRepo.save(Quote(organizationId = organizationId, quoteNumber = "PRES-001", status = com.blacksmith.metalstore.quote.domain.entity.QuoteStatus.ACCEPTED))

        mockMvc.perform(post("/api/quotes/{id}/cancel", quote.id)
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isBadRequest)
    }
}