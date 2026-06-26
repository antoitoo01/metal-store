package com.blacksmith.metalstore.inbound

import com.blacksmith.metalstore.auth.audit.AuditLogger
import com.blacksmith.metalstore.inbound.application.InboundDeliveryNoteService
import com.blacksmith.metalstore.inbound.domain.entity.InboundDeliveryNote
import com.blacksmith.metalstore.inbound.domain.entity.InboundDeliveryNoteLine
import com.blacksmith.metalstore.inbound.domain.entity.InboundDeliveryNoteStatus
import com.blacksmith.metalstore.inbound.domain.repository.InboundDeliveryNoteLineRepository
import com.blacksmith.metalstore.inbound.domain.repository.InboundDeliveryNoteRepository
import com.blacksmith.metalstore.inventory.application.InventoryService
import com.blacksmith.metalstore.inventory.domain.entity.InventoryItem
import com.blacksmith.metalstore.inventory.domain.entity.MovementType
import com.blacksmith.metalstore.inventory.domain.entity.ReferenceType
import com.blacksmith.metalstore.inventory.domain.repository.InventoryItemRepository
import com.blacksmith.metalstore.inventory.domain.repository.StockMovementRepository
import com.blacksmith.metalstore.shared.NumberSequenceRepository
import com.blacksmith.metalstore.shared.exception.ResourceNotFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InboundDeliveryNoteServiceTest {

    @Autowired
    private lateinit var repo: InboundDeliveryNoteRepository

    @Autowired
    private lateinit var lineRepo: InboundDeliveryNoteLineRepository

    @Autowired
    private lateinit var inventoryItemRepo: InventoryItemRepository

    @Autowired
    private lateinit var numberSequenceRepo: NumberSequenceRepository

    @Autowired
    private lateinit var inventoryService: InventoryService

    @Autowired
    private lateinit var movementRepo: StockMovementRepository

    private lateinit var service: InboundDeliveryNoteService
    private val organizationId = UUID.randomUUID()
    private val profileId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        repo.deleteAll()
        lineRepo.deleteAll()
        inventoryItemRepo.deleteAll()
        service = InboundDeliveryNoteService(repo, lineRepo, numberSequenceRepo, inventoryItemRepo, inventoryService, mock(AuditLogger::class.java))
    }

    @Test
    fun `create and find delivery note`() {
        val note = service.create(organizationId, InboundDeliveryNote(organizationId = organizationId, number = ""))
        assert(note.id != null)
        assert(note.number.startsWith("AL-${LocalDate.now().year}-"))
        assert(note.status == InboundDeliveryNoteStatus.DRAFT)
        assert(note.totalAmount == BigDecimal.ZERO)

        val found = service.findById(organizationId, note.id)
        assert(found.id == note.id)
    }

    @Test
    fun `findById throws when wrong organization`() {
        val note = service.create(organizationId, InboundDeliveryNote(organizationId = organizationId, number = ""))
        try {
            service.findById(UUID.randomUUID(), note.id)
            assert(false) { "Expected ResourceNotFoundException" }
        } catch (e: ResourceNotFoundException) {
            // expected
        }
    }

    @Test
    fun `findById throws when not found`() {
        try {
            service.findById(organizationId, UUID.randomUUID())
            assert(false) { "Expected ResourceNotFoundException" }
        } catch (e: ResourceNotFoundException) {
            // expected
        }
    }

    @Test
    fun `add and remove line`() {
        val note = service.create(organizationId, InboundDeliveryNote(organizationId = organizationId, number = ""))

        val line = service.addLine(organizationId, note.id, InboundDeliveryNoteLine(
            deliveryNoteId = note.id,
            lineNumber = 1,
            profileId = profileId,
            description = "IPN 200",
            quantity = BigDecimal("10.00"),
            unitPrice = BigDecimal("45.50")
        ))
        assert(line.id != null)
        assert(line.lineNumber == 1)

        val lines = service.getLines(note.id)
        assert(lines.size == 1)

        val refreshed = service.findById(organizationId, note.id)
        assert(refreshed.totalAmount.compareTo(BigDecimal("455.00")) == 0)

        service.removeLine(organizationId, note.id, line.id)
        assert(service.getLines(note.id).isEmpty())

        val refreshed2 = service.findById(organizationId, note.id)
        assert(refreshed2.totalAmount.compareTo(BigDecimal.ZERO) == 0)
    }

    @Test
    fun `cannot add line to confirmed note`() {
        val note = service.create(organizationId, InboundDeliveryNote(organizationId = organizationId, number = ""))
        val item = inventoryItemRepo.save(InventoryItem(organizationId = organizationId, profileId = profileId, quantity = BigDecimal("1.00")))
        service.addLine(organizationId, note.id, InboundDeliveryNoteLine(
            deliveryNoteId = note.id, lineNumber = 1, profileId = profileId,
            description = "Test", quantity = BigDecimal("10.00"), unitPrice = BigDecimal.ZERO
        ))
        service.confirm(organizationId, note.id)

        try {
            service.addLine(organizationId, note.id, InboundDeliveryNoteLine(
                deliveryNoteId = note.id, lineNumber = 2, profileId = profileId,
                description = "Another", quantity = BigDecimal("5.00"), unitPrice = BigDecimal.ZERO
            ))
            assert(false) { "Expected IllegalArgumentException" }
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `confirm updates inventory and registers movement`() {
        val item = inventoryItemRepo.save(InventoryItem(organizationId = organizationId, profileId = profileId, quantity = BigDecimal("50.00")))
        val note = service.create(organizationId, InboundDeliveryNote(organizationId = organizationId, number = "", supplierName = "Test"))

        service.addLine(organizationId, note.id, InboundDeliveryNoteLine(
            deliveryNoteId = note.id, lineNumber = 1, profileId = profileId,
            description = "IPN 200", quantity = BigDecimal("30.00"), unitPrice = BigDecimal("45.50")
        ))

        val confirmed = service.confirm(organizationId, note.id)
        assert(confirmed.status == InboundDeliveryNoteStatus.CONFIRMED)

        val updatedItem = inventoryItemRepo.findById(item.id).get()
        assert(updatedItem.quantity == BigDecimal("80.00"))

        val movements = movementRepo.findByOrganizationIdAndInventoryItemIdOrderByPerformedAtDesc(organizationId, item.id, org.springframework.data.domain.PageRequest.of(0, 100))
        assert(movements.totalElements >= 1L)
        val mov = movements.content[0]
        assert(mov.movementType == MovementType.INBOUND)
        assert(mov.referenceType == ReferenceType.DELIVERY_NOTE)
        assert(mov.referenceId == note.id)
        assert(mov.quantity == BigDecimal("30.00"))
    }

    @Test
    fun `confirm creates inventory item when none exists`() {
        val newProfileId = UUID.randomUUID()
        val note = service.create(organizationId, InboundDeliveryNote(organizationId = organizationId, number = "", supplierName = "Test"))

        service.addLine(organizationId, note.id, InboundDeliveryNoteLine(
            deliveryNoteId = note.id, lineNumber = 1, profileId = newProfileId,
            description = "New material", quantity = BigDecimal("25.00"), unitPrice = BigDecimal.ZERO
        ))

        service.confirm(organizationId, note.id)

        val items = inventoryItemRepo.findByOrganizationIdAndProfileId(organizationId, newProfileId)
        assert(items.size == 1)
        assert(items[0].quantity == BigDecimal("25.00"))
    }

    @Test
    fun `cancel transitions from draft`() {
        val note = service.create(organizationId, InboundDeliveryNote(organizationId = organizationId, number = ""))
        val cancelled = service.cancel(organizationId, note.id)
        assert(cancelled.status == InboundDeliveryNoteStatus.CANCELLED)
    }

    @Test
    fun `cancel throws from confirmed`() {
        val note = service.create(organizationId, InboundDeliveryNote(organizationId = organizationId, number = ""))
        val item = inventoryItemRepo.save(InventoryItem(organizationId = organizationId, profileId = profileId, quantity = BigDecimal("1.00")))
        service.addLine(organizationId, note.id, InboundDeliveryNoteLine(
            deliveryNoteId = note.id, lineNumber = 1, profileId = profileId,
            description = "Test", quantity = BigDecimal("10.00"), unitPrice = BigDecimal.ZERO
        ))
        service.confirm(organizationId, note.id)

        try {
            service.cancel(organizationId, note.id)
            assert(false) { "Expected IllegalArgumentException" }
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `confirm throws from confirmed`() {
        val note = service.create(organizationId, InboundDeliveryNote(organizationId = organizationId, number = ""))
        val item = inventoryItemRepo.save(InventoryItem(organizationId = organizationId, profileId = profileId, quantity = BigDecimal("1.00")))
        service.addLine(organizationId, note.id, InboundDeliveryNoteLine(
            deliveryNoteId = note.id, lineNumber = 1, profileId = profileId,
            description = "Test", quantity = BigDecimal("10.00"), unitPrice = BigDecimal.ZERO
        ))
        service.confirm(organizationId, note.id)

        try {
            service.confirm(organizationId, note.id)
            assert(false) { "Expected IllegalArgumentException" }
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }
}
