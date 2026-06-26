package com.blacksmith.metalstore.outbound

import com.blacksmith.metalstore.auth.audit.AuditLogger
import com.blacksmith.metalstore.inventory.application.InventoryService
import com.blacksmith.metalstore.inventory.domain.entity.InventoryItem
import com.blacksmith.metalstore.inventory.domain.entity.MovementType
import com.blacksmith.metalstore.inventory.domain.entity.ReferenceType
import com.blacksmith.metalstore.inventory.domain.repository.InventoryItemRepository
import com.blacksmith.metalstore.inventory.domain.repository.StockMovementRepository
import com.blacksmith.metalstore.outbound.application.OutboundDeliveryNoteService
import com.blacksmith.metalstore.outbound.domain.entity.OutboundDeliveryNote
import com.blacksmith.metalstore.outbound.domain.entity.OutboundDeliveryNoteLine
import com.blacksmith.metalstore.outbound.domain.entity.OutboundDeliveryNoteStatus
import com.blacksmith.metalstore.outbound.domain.repository.OutboundDeliveryNoteLineRepository
import com.blacksmith.metalstore.outbound.domain.repository.OutboundDeliveryNoteRepository
import com.blacksmith.metalstore.shared.NumberSequenceRepository
import com.blacksmith.metalstore.shared.exception.ResourceNotFoundException
import com.blacksmith.metalstore.shared.exception.ResourceNotFoundExceptionByField
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
class OutboundDeliveryNoteServiceTest {

    @Autowired
    private lateinit var repo: OutboundDeliveryNoteRepository

    @Autowired
    private lateinit var lineRepo: OutboundDeliveryNoteLineRepository

    @Autowired
    private lateinit var inventoryItemRepo: InventoryItemRepository

    @Autowired
    private lateinit var numberSequenceRepo: NumberSequenceRepository

    @Autowired
    private lateinit var inventoryService: InventoryService

    @Autowired
    private lateinit var movementRepo: StockMovementRepository

    private lateinit var service: OutboundDeliveryNoteService
    private val organizationId = UUID.randomUUID()
    private val profileId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        repo.deleteAll()
        lineRepo.deleteAll()
        inventoryItemRepo.deleteAll()
        service = OutboundDeliveryNoteService(repo, lineRepo, numberSequenceRepo, inventoryItemRepo, inventoryService, mock(AuditLogger::class.java))
    }

    @Test
    fun `create and find delivery note`() {
        val note = service.create(organizationId, OutboundDeliveryNote(organizationId = organizationId, number = ""))
        assert(note.id != null)
        assert(note.number.startsWith("ALS-${LocalDate.now().year}-"))
        assert(note.status == OutboundDeliveryNoteStatus.DRAFT)
        assert(note.totalAmount == BigDecimal.ZERO)

        val found = service.findById(organizationId, note.id)
        assert(found.id == note.id)
    }

    @Test
    fun `findById throws when wrong organization`() {
        val note = service.create(organizationId, OutboundDeliveryNote(organizationId = organizationId, number = ""))
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
        val note = service.create(organizationId, OutboundDeliveryNote(organizationId = organizationId, number = ""))

        val line = service.addLine(organizationId, note.id, OutboundDeliveryNoteLine(
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
        val note = service.create(organizationId, OutboundDeliveryNote(organizationId = organizationId, number = ""))
        val item = inventoryItemRepo.save(InventoryItem(organizationId = organizationId, profileId = profileId, quantity = BigDecimal("100.00")))
        service.addLine(organizationId, note.id, OutboundDeliveryNoteLine(
            deliveryNoteId = note.id, lineNumber = 1, profileId = profileId,
            description = "Test", quantity = BigDecimal("10.00"), unitPrice = BigDecimal.ZERO
        ))
        service.confirm(organizationId, note.id)

        try {
            service.addLine(organizationId, note.id, OutboundDeliveryNoteLine(
                deliveryNoteId = note.id, lineNumber = 2, profileId = profileId,
                description = "Another", quantity = BigDecimal("5.00"), unitPrice = BigDecimal.ZERO
            ))
            assert(false) { "Expected IllegalArgumentException" }
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `confirm removes stock and registers movement`() {
        val item = inventoryItemRepo.save(InventoryItem(organizationId = organizationId, profileId = profileId, quantity = BigDecimal("50.00")))
        val note = service.create(organizationId, OutboundDeliveryNote(organizationId = organizationId, number = "", customerName = "Test"))

        service.addLine(organizationId, note.id, OutboundDeliveryNoteLine(
            deliveryNoteId = note.id, lineNumber = 1, profileId = profileId,
            description = "IPN 200", quantity = BigDecimal("30.00"), unitPrice = BigDecimal("45.50")
        ))

        val confirmed = service.confirm(organizationId, note.id)
        assert(confirmed.status == OutboundDeliveryNoteStatus.CONFIRMED)

        val updatedItem = inventoryItemRepo.findById(item.id).get()
        assert(updatedItem.quantity.compareTo(BigDecimal("20.00")) == 0)

        val movements = movementRepo.findByOrganizationIdAndInventoryItemIdOrderByPerformedAtDesc(organizationId, item.id, org.springframework.data.domain.PageRequest.of(0, 100))
        assert(movements.totalElements >= 1L)
        val mov = movements.content[0]
        assert(mov.movementType == MovementType.OUTBOUND)
        assert(mov.referenceType == ReferenceType.DELIVERY_NOTE)
        assert(mov.referenceId == note.id)
        assert(mov.quantity.compareTo(BigDecimal("30.00")) == 0)
    }

    @Test
    fun `confirm throws when insufficient stock`() {
        val item = inventoryItemRepo.save(InventoryItem(organizationId = organizationId, profileId = profileId, quantity = BigDecimal("5.00")))
        val note = service.create(organizationId, OutboundDeliveryNote(organizationId = organizationId, number = "", customerName = "Test"))

        service.addLine(organizationId, note.id, OutboundDeliveryNoteLine(
            deliveryNoteId = note.id, lineNumber = 1, profileId = profileId,
            description = "IPN 200", quantity = BigDecimal("30.00"), unitPrice = BigDecimal.ZERO
        ))

        try {
            service.confirm(organizationId, note.id)
            assert(false) { "Expected IllegalArgumentException" }
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `confirm throws when no inventory item exists`() {
        val note = service.create(organizationId, OutboundDeliveryNote(organizationId = organizationId, number = "", customerName = "Test"))

        service.addLine(organizationId, note.id, OutboundDeliveryNoteLine(
            deliveryNoteId = note.id, lineNumber = 1, profileId = UUID.randomUUID(),
            description = "New material", quantity = BigDecimal("10.00"), unitPrice = BigDecimal.ZERO
        ))

        try {
            service.confirm(organizationId, note.id)
            assert(false) { "Expected ResourceNotFoundException" }
        } catch (e: ResourceNotFoundExceptionByField) {
            // expected
        }
    }

    @Test
    fun `cancel transitions from draft`() {
        val note = service.create(organizationId, OutboundDeliveryNote(organizationId = organizationId, number = ""))
        val cancelled = service.cancel(organizationId, note.id)
        assert(cancelled.status == OutboundDeliveryNoteStatus.CANCELLED)
    }

    @Test
    fun `cancel throws from confirmed`() {
        val note = service.create(organizationId, OutboundDeliveryNote(organizationId = organizationId, number = ""))
        val item = inventoryItemRepo.save(InventoryItem(organizationId = organizationId, profileId = profileId, quantity = BigDecimal("100.00")))
        service.addLine(organizationId, note.id, OutboundDeliveryNoteLine(
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
        val note = service.create(organizationId, OutboundDeliveryNote(organizationId = organizationId, number = ""))
        val item = inventoryItemRepo.save(InventoryItem(organizationId = organizationId, profileId = profileId, quantity = BigDecimal("100.00")))
        service.addLine(organizationId, note.id, OutboundDeliveryNoteLine(
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
