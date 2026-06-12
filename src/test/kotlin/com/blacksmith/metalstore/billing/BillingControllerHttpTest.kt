package com.blacksmith.metalstore.billing

import com.blacksmith.metalstore.billing.domain.entity.*
import com.blacksmith.metalstore.billing.domain.repository.InvoiceLineRepository
import com.blacksmith.metalstore.billing.domain.repository.InvoiceRepository
import com.blacksmith.metalstore.billing.domain.repository.PriceListRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BillingControllerHttpTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var priceListRepo: PriceListRepository
    @Autowired
    private lateinit var invoiceRepo: InvoiceRepository
    @Autowired
    private lateinit var invoiceLineRepo: InvoiceLineRepository

    private val organizationId = UUID.randomUUID()
    private val otherOrganizationId = UUID.randomUUID()
    private val profileId = UUID.randomUUID()
    private val headerName = "X-Organization-Id"

    @BeforeEach
    fun setUp() {
        priceListRepo.deleteAll()
        invoiceLineRepo.deleteAll()
        invoiceRepo.deleteAll()
    }

    // â”€â”€ Price List â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Test
    fun `create and list price list items`() {
        val body = """{"organizationId":"$organizationId","profileId":"$profileId","unitPrice":150.00}"""

        mockMvc.perform(post("/api/billing/prices")
            .header(headerName, organizationId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.unitPrice").value(150.00))

        mockMvc.perform(get("/api/billing/prices")
            .header(headerName, organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page.totalElements").value(1))
    }

    @Test
    fun `delete price list item`() {
        val price = priceListRepo.save(
            PriceListItem(organizationId = organizationId, profileId = profileId, unitPrice = BigDecimal("100.00"))
        )

        mockMvc.perform(delete("/api/billing/prices/{id}", price.id)
            .header(headerName, organizationId.toString()))
            .andExpect(status().isNoContent)

        assert(priceListRepo.findById(price.id).isEmpty)
    }

    // â”€â”€ Invoices â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Test
    fun `create draft invoice`() {
        mockMvc.perform(post("/api/billing/invoices")
            .header(headerName, organizationId.toString())
            .param("customerName", "Cliente Test")
            .param("customerVat", "B12345678"))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("DRAFT"))
            .andExpect(jsonPath("$.invoiceNumber").isString)
            .andExpect(jsonPath("$.customerName").value("Cliente Test"))
    }

    @Test
    fun `list invoices returns paginated results`() {
        invoiceRepo.save(Invoice(organizationId = organizationId, invoiceNumber = "FAC-2026-001"))
        invoiceRepo.save(Invoice(organizationId = organizationId, invoiceNumber = "FAC-2026-002"))

        mockMvc.perform(get("/api/billing/invoices")
            .header(headerName, organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page.totalElements").value(2))
    }

    @Test
    fun `get invoice by id`() {
        val inv = invoiceRepo.save(Invoice(organizationId = organizationId, invoiceNumber = "FAC-2026-001"))

        mockMvc.perform(get("/api/billing/invoices/{id}", inv.id)
            .header(headerName, organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(inv.id.toString()))
            .andExpect(jsonPath("$.invoiceNumber").value("FAC-2026-001"))
    }

    @Test
    fun `add line to draft recalculates totals`() {
        val inv = invoiceRepo.save(Invoice(organizationId = organizationId, invoiceNumber = "FAC-2026-001"))
        val lineBody = """{"invoiceId":"${inv.id}","lineNumber":1,"description":"Viga HEB200","profileId":"$profileId","quantity":150.00,"unitPrice":2.50,"totalPrice":999.99}"""

        mockMvc.perform(post("/api/billing/invoices/{id}/lines", inv.id)
            .header(headerName, organizationId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(lineBody))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.lineNumber").value(1))
            .andExpect(jsonPath("$.totalPrice").value(375.00))
    }

    @Test
    fun `cannot add lines to issued invoice`() {
        val inv = invoiceRepo.save(Invoice(organizationId = organizationId, invoiceNumber = "FAC-2026-001").copy(status = InvoiceStatus.ISSUED))
        val lineBody = """{"invoiceId":"${inv.id}","lineNumber":1,"description":"Test","quantity":1,"unitPrice":10,"totalPrice":10}"""

        mockMvc.perform(post("/api/billing/invoices/{id}/lines", inv.id)
            .header(headerName, organizationId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(lineBody))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `issue invoice changes status`() {
        val inv = invoiceRepo.save(Invoice(organizationId = organizationId, invoiceNumber = "FAC-2026-001"))

        mockMvc.perform(post("/api/billing/invoices/{id}/issue", inv.id)
            .header(headerName, organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("ISSUED"))
    }

    @Test
    fun `pay transitions from issued`() {
        val inv = invoiceRepo.save(Invoice(organizationId = organizationId, invoiceNumber = "FAC-2026-001", status = InvoiceStatus.ISSUED))

        mockMvc.perform(post("/api/billing/invoices/{id}/pay", inv.id)
            .header(headerName, organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("PAID"))
    }

    @Test
    fun `cancel transitions from draft`() {
        val inv = invoiceRepo.save(Invoice(organizationId = organizationId, invoiceNumber = "FAC-2026-001"))

        mockMvc.perform(post("/api/billing/invoices/{id}/cancel", inv.id)
            .header(headerName, organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("CANCELLED"))
    }

    @Test
    fun `organization isolation prevents cross-tenant access`() {
        val inv = invoiceRepo.save(Invoice(organizationId = otherOrganizationId, invoiceNumber = "FAC-2026-001"))

        mockMvc.perform(get("/api/billing/invoices/{id}", inv.id)
            .header(headerName, organizationId.toString()))
            .andExpect(status().isNotFound)
    }
}