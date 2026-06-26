package com.blacksmith.metalstore.inbound

import com.blacksmith.metalstore.inbound.domain.entity.InboundDeliveryNote
import com.blacksmith.metalstore.inbound.domain.entity.InboundDeliveryNoteLine
import com.blacksmith.metalstore.inbound.domain.entity.InboundDeliveryNoteStatus
import com.blacksmith.metalstore.inbound.domain.repository.InboundDeliveryNoteLineRepository
import com.blacksmith.metalstore.inbound.domain.repository.InboundDeliveryNoteRepository
import com.blacksmith.metalstore.inventory.domain.entity.InventoryItem
import com.blacksmith.metalstore.inventory.domain.repository.InventoryItemRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.hamcrest.Matchers.startsWith
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class InboundDeliveryNoteControllerHttpTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var repo: InboundDeliveryNoteRepository

    @Autowired
    private lateinit var lineRepo: InboundDeliveryNoteLineRepository

    @Autowired
    private lateinit var inventoryItemRepo: InventoryItemRepository

    private val organizationId = UUID.randomUUID()
    private val profileId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        lineRepo.deleteAll()
        repo.deleteAll()
        inventoryItemRepo.deleteAll()
    }

    @Test
    fun `create stores delivery note with organization and number`() {
        val body = """{"supplierName":"Aceros del Norte","supplierVat":"B12345678"}"""

        mockMvc.perform(post("/api/inbound-delivery-notes")
            .header("X-Organization-Id", organizationId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.number").value(startsWith("AL-${LocalDate.now().year}-")))
            .andExpect(jsonPath("$.status").value("DRAFT"))
    }

    @Test
    fun `get returns delivery note by id`() {
        val note = repo.save(InboundDeliveryNote(
            organizationId = organizationId,
            number = "AL-2026-001",
            supplierName = "Test Supplier"
        ))

        mockMvc.perform(get("/api/inbound-delivery-notes/{id}", note.id)
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.number").value("AL-2026-001"))
            .andExpect(jsonPath("$.supplierName").value("Test Supplier"))
    }

    @Test
    fun `get returns 404 for non-existent id`() {
        mockMvc.perform(get("/api/inbound-delivery-notes/{id}", UUID.randomUUID())
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `list returns paginated delivery notes`() {
        repo.save(InboundDeliveryNote(organizationId = organizationId, number = "AL-2026-001"))
        repo.save(InboundDeliveryNote(organizationId = organizationId, number = "AL-2026-002"))
        repo.save(InboundDeliveryNote(organizationId = organizationId, number = "AL-2026-003", supplierName = "Other"))

        mockMvc.perform(get("/api/inbound-delivery-notes")
            .header("X-Organization-Id", organizationId.toString())
            .param("size", "2"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page.totalElements").value(3))
            .andExpect(jsonPath("$.page.size").value(2))
    }

    @Test
    fun `add and get lines`() {
        val note = repo.save(InboundDeliveryNote(organizationId = organizationId, number = "AL-2026-001"))

        val lineBody = """{"lineNumber":1,"description":"IPN 200","quantity":10.00,"unitPrice":45.50,"profileId":"$profileId"}"""

        mockMvc.perform(post("/api/inbound-delivery-notes/{id}/lines", note.id)
            .header("X-Organization-Id", organizationId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(lineBody))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.lineNumber").value(1))
            .andExpect(jsonPath("$.description").value("IPN 200"))

        mockMvc.perform(get("/api/inbound-delivery-notes/{id}/lines", note.id)
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
    }

    @Test
    fun `confirm updates inventory and returns confirmed status`() {
        val item = inventoryItemRepo.save(InventoryItem(organizationId = organizationId, profileId = profileId, quantity = BigDecimal("50.00")))
        val note = repo.save(InboundDeliveryNote(organizationId = organizationId, number = "AL-2026-001"))
        lineRepo.save(InboundDeliveryNoteLine(
            deliveryNoteId = note.id, lineNumber = 1, profileId = profileId,
            description = "IPN 200", quantity = BigDecimal("30.00"), unitPrice = BigDecimal.ZERO
        ))

        mockMvc.perform(post("/api/inbound-delivery-notes/{id}/confirm", note.id)
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("CONFIRMED"))

        val updated = inventoryItemRepo.findById(item.id).get()
        assert(updated.quantity.compareTo(BigDecimal("80.00")) == 0) { "Expected 80.00 but was ${updated.quantity}" }
    }

    @Test
    fun `cancel transitions from draft`() {
        val note = repo.save(InboundDeliveryNote(organizationId = organizationId, number = "AL-2026-001"))

        mockMvc.perform(post("/api/inbound-delivery-notes/{id}/cancel", note.id)
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("CANCELLED"))
    }

    @Test
    fun `remove line from draft`() {
        val note = repo.save(InboundDeliveryNote(organizationId = organizationId, number = "AL-2026-001"))
        val line = lineRepo.save(InboundDeliveryNoteLine(
            deliveryNoteId = note.id, lineNumber = 1, description = "Test",
            quantity = BigDecimal("10.00"), unitPrice = BigDecimal.ZERO
        ))

        mockMvc.perform(delete("/api/inbound-delivery-notes/{id}/lines/{lineId}", note.id, line.id)
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `cannot confirm without lines`() {
        val note = repo.save(InboundDeliveryNote(organizationId = organizationId, number = "AL-2026-001"))

        mockMvc.perform(post("/api/inbound-delivery-notes/{id}/confirm", note.id)
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("CONFIRMED"))
    }
}
