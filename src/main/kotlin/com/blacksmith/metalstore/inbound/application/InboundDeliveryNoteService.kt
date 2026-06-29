package com.blacksmith.metalstore.inbound.application

import com.blacksmith.metalstore.auth.audit.AuditLogger
import com.blacksmith.metalstore.inbound.domain.entity.InboundDeliveryNote
import com.blacksmith.metalstore.inbound.domain.dto.request.UpdateInboundDeliveryNoteRequest
import com.blacksmith.metalstore.inbound.domain.entity.InboundDeliveryNoteLine
import com.blacksmith.metalstore.inbound.domain.entity.InboundDeliveryNoteStatus
import com.blacksmith.metalstore.inbound.domain.repository.InboundDeliveryNoteLineRepository
import com.blacksmith.metalstore.inbound.domain.repository.InboundDeliveryNoteRepository
import com.blacksmith.metalstore.inventory.application.InventoryService
import com.blacksmith.metalstore.inventory.domain.entity.InventoryItem
import com.blacksmith.metalstore.inventory.domain.entity.MovementType
import com.blacksmith.metalstore.inventory.domain.entity.ReferenceType
import com.blacksmith.metalstore.inventory.domain.repository.InventoryItemRepository
import com.blacksmith.metalstore.shared.NumberSequence
import com.blacksmith.metalstore.shared.NumberSequenceId
import com.blacksmith.metalstore.shared.NumberSequenceRepository
import com.blacksmith.metalstore.shared.exception.ResourceNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Service
@Transactional
class InboundDeliveryNoteService(
    private val repo: InboundDeliveryNoteRepository,
    private val lineRepo: InboundDeliveryNoteLineRepository,
    private val numberSequenceRepo: NumberSequenceRepository,
    private val inventoryItemRepo: InventoryItemRepository,
    private val inventoryService: InventoryService,
    private val audit: AuditLogger
) {
    @Transactional(readOnly = true)
    fun findAll(organizationId: UUID, pageable: Pageable): Page<InboundDeliveryNote> =
        repo.findByOrganizationId(organizationId, pageable)

    @Transactional(readOnly = true)
    fun findById(organizationId: UUID, id: UUID): InboundDeliveryNote =
        repo.findById(id).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("InboundDeliveryNote", id) }

    @Transactional(readOnly = true)
    fun getLines(deliveryNoteId: UUID): List<InboundDeliveryNoteLine> =
        lineRepo.findByDeliveryNoteIdOrderByLineNumber(deliveryNoteId)

    fun create(organizationId: UUID, note: InboundDeliveryNote): InboundDeliveryNote {
        val number = nextNumber(organizationId)
        val draft = InboundDeliveryNote(
            organizationId = organizationId,
            number = number,
            supplierId = note.supplierId,
            supplierName = note.supplierName,
            supplierVat = note.supplierVat,
            supplierAddress = note.supplierAddress,
            poId = note.poId,
            poNumber = note.poNumber,
            issueDate = note.issueDate,
            notes = note.notes,
            status = InboundDeliveryNoteStatus.DRAFT
        )
        val saved = repo.save(draft)
        audit.log(AuditLogger.AuditEvent(
            action = "IDN_CREATED",
            entityType = "InboundDeliveryNote",
            entityId = saved.id.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("number" to number)
        ))
        return saved
    }

    fun update(organizationId: UUID, id: UUID, request: UpdateInboundDeliveryNoteRequest): InboundDeliveryNote {
        val note = repo.findById(id).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("InboundDeliveryNote", id) }
        require(note.status == InboundDeliveryNoteStatus.DRAFT) { "InboundDeliveryNote $id is not in DRAFT status" }
        note.supplierId = request.supplierId
        note.supplierName = request.supplierName
        note.supplierVat = request.supplierVat
        note.supplierAddress = request.supplierAddress
        note.poId = request.poId
        note.poNumber = request.poNumber
        note.issueDate = request.issueDate ?: note.issueDate
        note.notes = request.notes
        val saved = repo.save(note)
        audit.log(AuditLogger.AuditEvent(
            action = "IDN_UPDATED",
            entityType = "InboundDeliveryNote",
            entityId = saved.id.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("number" to saved.number)
        ))
        return saved
    }

    fun addLine(organizationId: UUID, noteId: UUID, line: InboundDeliveryNoteLine): InboundDeliveryNoteLine {
        val note = repo.findById(noteId).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("InboundDeliveryNote", noteId) }
        require(note.status == InboundDeliveryNoteStatus.DRAFT) { "InboundDeliveryNote $noteId is not in DRAFT status" }
        val saved = lineRepo.save(line)
        recalcTotal(noteId)
        audit.log(AuditLogger.AuditEvent(
            action = "IDN_LINE_ADDED",
            entityType = "InboundDeliveryNoteLine",
            entityId = saved.id.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("noteId" to noteId.toString())
        ))
        return saved
    }

    fun removeLine(organizationId: UUID, noteId: UUID, lineId: UUID) {
        val note = repo.findById(noteId).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("InboundDeliveryNote", noteId) }
        require(note.status == InboundDeliveryNoteStatus.DRAFT) { "InboundDeliveryNote $noteId is not in DRAFT status" }
        val line = lineRepo.findById(lineId).filter { it.deliveryNoteId == noteId }
            .orElseThrow { ResourceNotFoundException("InboundDeliveryNoteLine", lineId) }
        lineRepo.delete(line)
        recalcTotal(noteId)
        audit.log(AuditLogger.AuditEvent(
            action = "IDN_LINE_REMOVED",
            entityType = "InboundDeliveryNoteLine",
            entityId = lineId.toString(),
            organizationId = organizationId.toString()
        ))
    }

    fun confirm(organizationId: UUID, noteId: UUID): InboundDeliveryNote {
        val note = repo.findById(noteId).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("InboundDeliveryNote", noteId) }
        require(note.status.canTransitionTo(InboundDeliveryNoteStatus.CONFIRMED)) {
            "InboundDeliveryNote $noteId cannot be confirmed from status ${note.status}"
        }
        val lines = lineRepo.findByDeliveryNoteIdOrderByLineNumber(noteId)

        for (line in lines) {
            val effectiveProfileId = line.profileId
            if (effectiveProfileId != null) {
                var item = inventoryItemRepo.findByOrganizationIdAndProfileId(organizationId, effectiveProfileId)
                    .firstOrNull()
                if (item == null) {
                    item = inventoryItemRepo.save(InventoryItem(
                        organizationId = organizationId,
                        profileId = effectiveProfileId,
                        quantity = BigDecimal.ZERO,
                        notes = "Creado desde albarán ${note.number}"
                    ))
                }
                inventoryService.addStock(
                    organizationId, item.id, line.quantity,
                    "Albarán ${note.number}: ${line.description}",
                    ReferenceType.DELIVERY_NOTE, noteId
                )
            }
        }

        note.status = InboundDeliveryNoteStatus.CONFIRMED
        val saved = repo.save(note)
        audit.log(AuditLogger.AuditEvent(
            action = "IDN_CONFIRMED",
            entityType = "InboundDeliveryNote",
            entityId = noteId.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("number" to note.number)
        ))
        return saved
    }

    fun cancel(organizationId: UUID, noteId: UUID): InboundDeliveryNote {
        val note = repo.findById(noteId).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("InboundDeliveryNote", noteId) }
        require(note.status.canTransitionTo(InboundDeliveryNoteStatus.CANCELLED)) {
            "InboundDeliveryNote $noteId cannot be cancelled from status ${note.status}"
        }
        note.status = InboundDeliveryNoteStatus.CANCELLED
        val saved = repo.save(note)
        audit.log(AuditLogger.AuditEvent(
            action = "IDN_CANCELLED",
            entityType = "InboundDeliveryNote",
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
            note.totalAmount = total
            repo.save(note)
        }
    }

    private fun nextNumber(organizationId: UUID): String {
        val year = LocalDate.now().year
        val seq = numberSequenceRepo.findWithLock(organizationId, "AL", year)
            .orElse(NumberSequence(NumberSequenceId(organizationId, "AL", year), 0))
        seq.counter += 1
        numberSequenceRepo.save(seq)
        return "AL-$year-${organizationId.toString().take(8).uppercase()}-${seq.counter}"
    }
}
