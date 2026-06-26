package com.blacksmith.metalstore.outbound.application

import com.blacksmith.metalstore.auth.audit.AuditLogger
import com.blacksmith.metalstore.inventory.application.InventoryService
import com.blacksmith.metalstore.inventory.domain.entity.ReferenceType
import com.blacksmith.metalstore.inventory.domain.repository.InventoryItemRepository
import com.blacksmith.metalstore.outbound.domain.entity.OutboundDeliveryNote
import com.blacksmith.metalstore.outbound.domain.entity.OutboundDeliveryNoteLine
import com.blacksmith.metalstore.outbound.domain.entity.OutboundDeliveryNoteStatus
import com.blacksmith.metalstore.outbound.domain.repository.OutboundDeliveryNoteLineRepository
import com.blacksmith.metalstore.outbound.domain.repository.OutboundDeliveryNoteRepository
import com.blacksmith.metalstore.shared.NumberSequence
import com.blacksmith.metalstore.shared.NumberSequenceId
import com.blacksmith.metalstore.shared.NumberSequenceRepository
import com.blacksmith.metalstore.shared.exception.ResourceNotFoundException
import com.blacksmith.metalstore.shared.exception.ResourceNotFoundExceptionByField
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Service
@Transactional
class OutboundDeliveryNoteService(
    private val repo: OutboundDeliveryNoteRepository,
    private val lineRepo: OutboundDeliveryNoteLineRepository,
    private val numberSequenceRepo: NumberSequenceRepository,
    private val inventoryItemRepo: InventoryItemRepository,
    private val inventoryService: InventoryService,
    private val audit: AuditLogger
) {
    @Transactional(readOnly = true)
    fun findAll(organizationId: UUID, pageable: Pageable): Page<OutboundDeliveryNote> =
        repo.findByOrganizationId(organizationId, pageable)

    @Transactional(readOnly = true)
    fun findById(organizationId: UUID, id: UUID): OutboundDeliveryNote =
        repo.findById(id).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("OutboundDeliveryNote", id) }

    @Transactional(readOnly = true)
    fun getLines(deliveryNoteId: UUID): List<OutboundDeliveryNoteLine> =
        lineRepo.findByDeliveryNoteIdOrderByLineNumber(deliveryNoteId)

    fun create(organizationId: UUID, note: OutboundDeliveryNote): OutboundDeliveryNote {
        val number = nextNumber(organizationId)
        val draft = OutboundDeliveryNote(
            organizationId = organizationId,
            number = number,
            customerId = note.customerId,
            customerName = note.customerName,
            customerVat = note.customerVat,
            customerAddress = note.customerAddress,
            issueDate = note.issueDate,
            notes = note.notes,
            status = OutboundDeliveryNoteStatus.DRAFT
        )
        val saved = repo.save(draft)
        audit.log(AuditLogger.AuditEvent(
            action = "ODN_CREATED",
            entityType = "OutboundDeliveryNote",
            entityId = saved.id.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("number" to number)
        ))
        return saved
    }

    fun addLine(organizationId: UUID, noteId: UUID, line: OutboundDeliveryNoteLine): OutboundDeliveryNoteLine {
        val note = repo.findById(noteId).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("OutboundDeliveryNote", noteId) }
        require(note.status == OutboundDeliveryNoteStatus.DRAFT) { "OutboundDeliveryNote $noteId is not in DRAFT status" }
        val saved = lineRepo.save(line)
        recalcTotal(noteId)
        audit.log(AuditLogger.AuditEvent(
            action = "ODN_LINE_ADDED",
            entityType = "OutboundDeliveryNoteLine",
            entityId = saved.id.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("noteId" to noteId.toString())
        ))
        return saved
    }

    fun removeLine(organizationId: UUID, noteId: UUID, lineId: UUID) {
        val note = repo.findById(noteId).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("OutboundDeliveryNote", noteId) }
        require(note.status == OutboundDeliveryNoteStatus.DRAFT) { "OutboundDeliveryNote $noteId is not in DRAFT status" }
        val line = lineRepo.findById(lineId).filter { it.deliveryNoteId == noteId }
            .orElseThrow { ResourceNotFoundException("OutboundDeliveryNoteLine", lineId) }
        lineRepo.delete(line)
        recalcTotal(noteId)
        audit.log(AuditLogger.AuditEvent(
            action = "ODN_LINE_REMOVED",
            entityType = "OutboundDeliveryNoteLine",
            entityId = lineId.toString(),
            organizationId = organizationId.toString()
        ))
    }

    fun confirm(organizationId: UUID, noteId: UUID): OutboundDeliveryNote {
        val note = repo.findById(noteId).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("OutboundDeliveryNote", noteId) }
        require(note.status.canTransitionTo(OutboundDeliveryNoteStatus.CONFIRMED)) {
            "OutboundDeliveryNote $noteId cannot be confirmed from status ${note.status}"
        }
        val lines = lineRepo.findByDeliveryNoteIdOrderByLineNumber(noteId)

        for (line in lines) {
            val effectiveProfileId = line.profileId
            if (effectiveProfileId != null) {
                val item = inventoryItemRepo.findByOrganizationIdAndProfileId(organizationId, effectiveProfileId)
                    .firstOrNull() ?: throw ResourceNotFoundExceptionByField("InventoryItem", "profileId", effectiveProfileId.toString())
                inventoryService.removeStock(
                    organizationId, item.id, line.quantity,
                    "Albarán ${note.number}: ${line.description}",
                    ReferenceType.DELIVERY_NOTE, noteId
                )
            }
        }

        note.status = OutboundDeliveryNoteStatus.CONFIRMED
        val saved = repo.save(note)
        audit.log(AuditLogger.AuditEvent(
            action = "ODN_CONFIRMED",
            entityType = "OutboundDeliveryNote",
            entityId = noteId.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("number" to note.number)
        ))
        return saved
    }

    fun cancel(organizationId: UUID, noteId: UUID): OutboundDeliveryNote {
        val note = repo.findById(noteId).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("OutboundDeliveryNote", noteId) }
        require(note.status.canTransitionTo(OutboundDeliveryNoteStatus.CANCELLED)) {
            "OutboundDeliveryNote $noteId cannot be cancelled from status ${note.status}"
        }
        note.status = OutboundDeliveryNoteStatus.CANCELLED
        val saved = repo.save(note)
        audit.log(AuditLogger.AuditEvent(
            action = "ODN_CANCELLED",
            entityType = "OutboundDeliveryNote",
            entityId = noteId.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("number" to note.number)
        ))
        return saved
    }

    private fun recalcTotal(noteId: UUID) {
        val lines = lineRepo.findByDeliveryNoteIdOrderByLineNumber(noteId)
        val total = lines.sumOf { it.quantity * it.unitPrice }
        repo.findById(noteId).ifPresent { note ->
            repo.save(OutboundDeliveryNote(
                id = note.id,
                organizationId = note.organizationId,
                number = note.number,
                customerId = note.customerId,
                customerName = note.customerName,
                customerVat = note.customerVat,
                customerAddress = note.customerAddress,
                issueDate = note.issueDate,
                status = note.status,
                totalAmount = total,
                notes = note.notes
            ))
        }
    }

    private fun nextNumber(organizationId: UUID): String {
        val year = LocalDate.now().year
        val seq = numberSequenceRepo.findWithLock(organizationId, "ALS", year)
            .orElse(NumberSequence(NumberSequenceId(organizationId, "ALS", year), 0))
        seq.counter += 1
        numberSequenceRepo.save(seq)
        return "ALS-$year-${organizationId.toString().take(8).uppercase()}-${seq.counter}"
    }
}
