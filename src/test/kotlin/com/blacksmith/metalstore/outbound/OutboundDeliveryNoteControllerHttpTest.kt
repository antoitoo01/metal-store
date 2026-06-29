package com.blacksmith.metalstore.outbound

import com.blacksmith.metalstore.inventory.domain.entity.InventoryItem
import com.blacksmith.metalstore.inventory.domain.repository.InventoryItemRepository
import com.blacksmith.metalstore.outbound.domain.entity.OutboundDeliveryNote
import com.blacksmith.metalstore.outbound.domain.entity.OutboundDeliveryNoteLine
import com.blacksmith.metalstore.outbound.domain.entity.OutboundDeliveryNoteStatus
import com.blacksmith.metalstore.outbound.domain.repository.OutboundDeliveryNoteLineRepository
import com.blacksmith.metalstore.outbound.domain.repository.OutboundDeliveryNoteRepository
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
import java.time.LocalDate
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OutboundDeliveryNoteControllerHttpTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var repo: OutboundDeliveryNoteRepository

    @Autowired
    private lateinit var lineRepo: OutboundDeliveryNoteLineRepository

    @Autowired
    private lateinit var inventoryItemRepo: InventoryItemRepository

    private val organizationId = UUID.randomUUID()
    private val profileId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        repo.deleteAll()
        lineRepo.deleteAll()
        inventoryItemRepo.deleteAll()
    }

    @Test
    fun `create stores delivery note with organization and number`() {
        mockMvc.perform(post("/api/outbound-delivery-notes")
            .header("X-Organization-Id", organizationId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"customerName":"Cliente Test"}"""))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.number").value("ALS-${LocalDate.now().year}-${organizationId.toString().take(8).uppercase()}-1"))
            .andExpect(jsonPath("$.status").value("DRAFT"))
            .andExpect(jsonPath("$.customerName").value("Cliente Test"))
    }

    @Test
    fun `list returns paginated delivery notes`() {
        repo.save(OutboundDeliveryNote(organizationId = organizationId, number = "ALS-2026-001"))
        val otherOrg = UUID.randomUUID()
        repo.save(OutboundDeliveryNote(organizationId = otherOrg, number = "ALS-2026-002"))

        mockMvc.perform(get("/api/outbound-delivery-notes")
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page.totalElements").value(1))
    }

    @Test
    fun `get returns delivery note by id`() {
        val note = repo.save(OutboundDeliveryNote(organizationId = organizationId, number = "ALS-2026-001"))

        mockMvc.perform(get("/api/outbound-delivery-notes/{id}", note.id)
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(note.id.toString()))
            .andExpect(jsonPath("$.number").value("ALS-2026-001"))
    }

    @Test
    fun `get returns 404 for non-existent id`() {
        mockMvc.perform(get("/api/outbound-delivery-notes/{id}", UUID.randomUUID())
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `add and get lines`() {
        val note = repo.save(OutboundDeliveryNote(organizationId = organizationId, number = "ALS-2026-001"))

        mockMvc.perform(post("/api/outbound-delivery-notes/{id}/lines", note.id)
            .header("X-Organization-Id", organizationId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"lineNumber":1,"description":"IPN 200","quantity":10.00,"unitPrice":45.50}"""))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").exists())

        mockMvc.perform(get("/api/outbound-delivery-notes/{id}/lines", note.id)
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
    }

    @Test
    fun `remove line from draft`() {
        val note = repo.save(OutboundDeliveryNote(organizationId = organizationId, number = "ALS-2026-001"))
        val line = lineRepo.save(OutboundDeliveryNoteLine(
            deliveryNoteId = note.id, lineNumber = 1, profileId = profileId,
            description = "IPN 200", quantity = BigDecimal("10.00"), unitPrice = BigDecimal.ZERO
        ))

        mockMvc.perform(delete("/api/outbound-delivery-notes/{id}/lines/{lineId}", note.id, line.id)
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `confirm removes stock and returns confirmed status`() {
        val item = inventoryItemRepo.save(InventoryItem(organizationId = organizationId, profileId = profileId, quantity = BigDecimal("50.00")))
        val note = repo.save(OutboundDeliveryNote(organizationId = organizationId, number = "ALS-2026-001"))
        lineRepo.save(OutboundDeliveryNoteLine(
            deliveryNoteId = note.id, lineNumber = 1, profileId = profileId,
            description = "IPN 200", quantity = BigDecimal("30.00"), unitPrice = BigDecimal.ZERO
        ))

        mockMvc.perform(post("/api/outbound-delivery-notes/{id}/confirm", note.id)
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("CONFIRMED"))

        val updated = inventoryItemRepo.findById(item.id).get()
        assert(updated.quantity.compareTo(BigDecimal("20.00")) == 0) { "Expected 20.00 but was ${updated.quantity}" }
    }

    @Test
    fun `cancel transitions from draft`() {
        val note = repo.save(OutboundDeliveryNote(organizationId = organizationId, number = "ALS-2026-001"))

        mockMvc.perform(post("/api/outbound-delivery-notes/{id}/cancel", note.id)
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("CANCELLED"))
    }

    @Test
    fun `update updates header fields of draft outbound`() {
        val note = repo.save(OutboundDeliveryNote(
            organizationId = organizationId,
            number = "ALS-2026-001",
            customerName = "Original"
        ))

        val body = """{"customerName":"Cliente Modificado","customerVat":"A12345678","notes":"Nota actualizada"}"""

        mockMvc.perform(put("/api/outbound-delivery-notes/{id}", note.id)
            .header("X-Organization-Id", organizationId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.customerName").value("Cliente Modificado"))
            .andExpect(jsonPath("$.customerVat").value("A12345678"))
            .andExpect(jsonPath("$.notes").value("Nota actualizada"))
            .andExpect(jsonPath("$.status").value("DRAFT"))

        val updated = repo.findById(note.id).get()
        assert(updated.customerName == "Cliente Modificado") { "Expected updated customerName" }
    }

    @Test
    fun `update rejects for confirmed outbound`() {
        val note = repo.save(OutboundDeliveryNote(
            organizationId = organizationId,
            number = "ALS-2026-001",
            status = OutboundDeliveryNoteStatus.CONFIRMED
        ))

        val body = """{"customerName":"Cambio"}"""

        mockMvc.perform(put("/api/outbound-delivery-notes/{id}", note.id)
            .header("X-Organization-Id", organizationId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `update returns 404 for non-existent outbound`() {
        val body = """{"customerName":"Test"}"""

        mockMvc.perform(put("/api/outbound-delivery-notes/{id}", UUID.randomUUID())
            .header("X-Organization-Id", organizationId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isNotFound)
    }
}
