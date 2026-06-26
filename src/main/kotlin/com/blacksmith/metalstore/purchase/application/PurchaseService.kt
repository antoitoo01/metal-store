package com.blacksmith.metalstore.purchase.application

import com.blacksmith.metalstore.auth.audit.AuditLogger
import com.blacksmith.metalstore.purchase.domain.entity.PurchaseOrder
import com.blacksmith.metalstore.purchase.domain.entity.PurchaseOrderLine
import com.blacksmith.metalstore.purchase.domain.entity.PurchaseOrderStatus
import com.blacksmith.metalstore.purchase.domain.entity.Supplier
import com.blacksmith.metalstore.purchase.domain.entity.SupplierStatus
import com.blacksmith.metalstore.purchase.domain.repository.PurchaseOrderLineRepository
import com.blacksmith.metalstore.purchase.domain.repository.PurchaseOrderRepository
import com.blacksmith.metalstore.purchase.domain.repository.SupplierRepository
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
class PurchaseService(
    private val supplierRepo: SupplierRepository,
    private val poRepo: PurchaseOrderRepository,
    private val poLineRepo: PurchaseOrderLineRepository,
    private val numberSequenceRepo: NumberSequenceRepository,
    private val audit: AuditLogger
) {
    // ── Suppliers ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    fun listSuppliers(organizationId: UUID, pageable: Pageable, nameFilter: String? = null, status: SupplierStatus? = null): Page<Supplier> =
        when {
            !nameFilter.isNullOrBlank() && status != null ->
                supplierRepo.findByOrganizationIdAndNameContainingIgnoreCaseAndStatus(organizationId, nameFilter, status, pageable)
            !nameFilter.isNullOrBlank() ->
                supplierRepo.findByOrganizationIdAndNameContainingIgnoreCase(organizationId, nameFilter, pageable)
            status != null ->
                supplierRepo.findByOrganizationIdAndStatus(organizationId, status, pageable)
            else -> supplierRepo.findByOrganizationId(organizationId, pageable)
        }

    @Transactional(readOnly = true)
    fun findSupplier(organizationId: UUID, id: UUID): Supplier =
        supplierRepo.findById(id).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("Supplier", id) }

    fun createSupplier(supplier: Supplier): Supplier {
        val saved = supplierRepo.save(supplier)
        audit.log(AuditLogger.AuditEvent(
            action = "SUPPLIER_CREATED",
            entityType = "Supplier",
            entityId = saved.id.toString(),
            organizationId = saved.organizationId.toString(),
            details = mapOf("name" to saved.name)
        ))
        return saved
    }

    fun updateSupplier(organizationId: UUID, id: UUID, updated: Supplier): Supplier {
        val existing = supplierRepo.findById(id).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("Supplier", id) }
        val merged = Supplier(
            id = existing.id,
            organizationId = existing.organizationId,
            name = updated.name.ifBlank { existing.name },
            email = updated.email ?: existing.email,
            phone = updated.phone ?: existing.phone,
            address = updated.address ?: existing.address,
            vatNumber = updated.vatNumber ?: existing.vatNumber,
            notes = updated.notes ?: existing.notes,
            status = updated.status
        )
        val saved = supplierRepo.save(merged)
        audit.log(AuditLogger.AuditEvent(
            action = "SUPPLIER_UPDATED",
            entityType = "Supplier",
            entityId = id.toString(),
            organizationId = organizationId.toString()
        ))
        return saved
    }

    fun deleteSupplier(organizationId: UUID, id: UUID) {
        val supplier = supplierRepo.findById(id).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("Supplier", id) }
        supplierRepo.delete(supplier)
        audit.log(AuditLogger.AuditEvent(
            action = "SUPPLIER_DELETED",
            entityType = "Supplier",
            entityId = id.toString(),
            organizationId = organizationId.toString()
        ))
    }

    fun activateSupplier(organizationId: UUID, id: UUID): Supplier {
        val existing = supplierRepo.findById(id).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("Supplier", id) }
        val merged = Supplier(
            id = existing.id,
            organizationId = existing.organizationId,
            name = existing.name,
            email = existing.email,
            phone = existing.phone,
            address = existing.address,
            vatNumber = existing.vatNumber,
            notes = existing.notes,
            status = SupplierStatus.ACTIVE
        )
        val saved = supplierRepo.save(merged)
        audit.log(AuditLogger.AuditEvent(
            action = "SUPPLIER_ACTIVATED",
            entityType = "Supplier",
            entityId = id.toString(),
            organizationId = organizationId.toString()
        ))
        return saved
    }

    fun deactivateSupplier(organizationId: UUID, id: UUID): Supplier {
        val existing = supplierRepo.findById(id).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("Supplier", id) }
        val merged = Supplier(
            id = existing.id,
            organizationId = existing.organizationId,
            name = existing.name,
            email = existing.email,
            phone = existing.phone,
            address = existing.address,
            vatNumber = existing.vatNumber,
            notes = existing.notes,
            status = SupplierStatus.INACTIVE
        )
        val saved = supplierRepo.save(merged)
        audit.log(AuditLogger.AuditEvent(
            action = "SUPPLIER_DEACTIVATED",
            entityType = "Supplier",
            entityId = id.toString(),
            organizationId = organizationId.toString()
        ))
        return saved
    }

    // ── Purchase Orders ────────────────────────────────────────────

    @Transactional(readOnly = true)
    fun listPurchaseOrders(organizationId: UUID, pageable: Pageable, q: String? = null, status: PurchaseOrderStatus? = null, supplierId: UUID? = null): Page<PurchaseOrder> =
        poRepo.findAllFiltered(organizationId, q?.lowercase() ?: "", status, supplierId, pageable)

    @Transactional(readOnly = true)
    fun findPurchaseOrder(organizationId: UUID, poId: UUID): PurchaseOrder =
        poRepo.findById(poId).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("PurchaseOrder", poId) }

    @Transactional(readOnly = true)
    fun getLines(poId: UUID): List<PurchaseOrderLine> =
        poLineRepo.findByPoIdOrderByLineNumber(poId)

    fun createDraft(organizationId: UUID, po: PurchaseOrder): PurchaseOrder {
        val number = nextPoNumber(organizationId)
        val draft = PurchaseOrder(
            organizationId = organizationId,
            poNumber = number,
            supplierId = po.supplierId,
            supplierName = po.supplierName,
            supplierVat = po.supplierVat,
            supplierAddress = po.supplierAddress,
            expectedDate = po.expectedDate,
            notes = po.notes,
            status = PurchaseOrderStatus.DRAFT
        )
        val saved = poRepo.save(draft)
        audit.log(AuditLogger.AuditEvent(
            action = "PO_CREATED",
            entityType = "PurchaseOrder",
            entityId = saved.id.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("number" to number, "supplierName" to po.supplierName)
        ))
        return saved
    }

    fun addLine(organizationId: UUID, poId: UUID, line: PurchaseOrderLine): PurchaseOrderLine {
        val po = poRepo.findById(poId).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("PurchaseOrder", poId) }
        require(po.status == PurchaseOrderStatus.DRAFT) { "PurchaseOrder $poId is not in DRAFT status" }
        val computedTotal = line.quantity * line.unitPrice
        val lineWithTotal = PurchaseOrderLine(
            id = line.id,
            poId = poId,
            lineNumber = line.lineNumber,
            profileId = line.profileId,
            itemId = line.itemId,
            description = line.description,
            quantity = line.quantity,
            unitPrice = line.unitPrice,
            vatRate = line.vatRate,
            totalPrice = computedTotal
        )
        val saved = poLineRepo.save(lineWithTotal)
        recalcTotals(poId)
        audit.log(AuditLogger.AuditEvent(
            action = "PO_LINE_ADDED",
            entityType = "PurchaseOrderLine",
            entityId = saved.id.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("poId" to poId.toString())
        ))
        return saved
    }

    fun removeLine(organizationId: UUID, poId: UUID, lineId: UUID) {
        val po = poRepo.findById(poId).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("PurchaseOrder", poId) }
        require(po.status == PurchaseOrderStatus.DRAFT) { "PurchaseOrder $poId is not in DRAFT status" }
        val line = poLineRepo.findById(lineId).filter { it.poId == poId }
            .orElseThrow { ResourceNotFoundException("PurchaseOrderLine", lineId) }
        poLineRepo.delete(line)
        recalcTotals(poId)
        audit.log(AuditLogger.AuditEvent(
            action = "PO_LINE_REMOVED",
            entityType = "PurchaseOrderLine",
            entityId = lineId.toString(),
            organizationId = organizationId.toString()
        ))
    }

    fun issue(organizationId: UUID, poId: UUID): PurchaseOrder {
        val po = poRepo.findById(poId).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("PurchaseOrder", poId) }
        require(po.status.canTransitionTo(PurchaseOrderStatus.ISSUED)) { "PurchaseOrder $poId cannot be issued from status ${po.status}" }
        po.status = PurchaseOrderStatus.ISSUED
        val saved = poRepo.save(po)
        audit.log(AuditLogger.AuditEvent(
            action = "PO_ISSUED",
            entityType = "PurchaseOrder",
            entityId = poId.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("number" to po.poNumber)
        ))
        return saved
    }

    fun receive(organizationId: UUID, poId: UUID): PurchaseOrder {
        val po = poRepo.findById(poId).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("PurchaseOrder", poId) }
        require(po.status.canTransitionTo(PurchaseOrderStatus.RECEIVED)) { "PurchaseOrder $poId cannot be received from status ${po.status}" }
        po.status = PurchaseOrderStatus.RECEIVED
        val saved = poRepo.save(po)
        audit.log(AuditLogger.AuditEvent(
            action = "PO_RECEIVED",
            entityType = "PurchaseOrder",
            entityId = poId.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("number" to po.poNumber)
        ))
        return saved
    }

    fun cancel(organizationId: UUID, poId: UUID): PurchaseOrder {
        val po = poRepo.findById(poId).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("PurchaseOrder", poId) }
        require(po.status.canTransitionTo(PurchaseOrderStatus.CANCELLED)) { "PurchaseOrder $poId cannot be cancelled from status ${po.status}" }
        po.status = PurchaseOrderStatus.CANCELLED
        val saved = poRepo.save(po)
        audit.log(AuditLogger.AuditEvent(
            action = "PO_CANCELLED",
            entityType = "PurchaseOrder",
            entityId = poId.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("number" to po.poNumber)
        ))
        return saved
    }

    fun update(organizationId: UUID, poId: UUID, supplierId: UUID?, supplierName: String?, supplierVat: String?, supplierAddress: String?, expectedDate: LocalDate?, notes: String?): PurchaseOrder {
        val po = poRepo.findById(poId).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("PurchaseOrder", poId) }
        require(po.status == PurchaseOrderStatus.DRAFT) { "PurchaseOrder $poId is not in DRAFT status" }
        val merged = PurchaseOrder(
            id = po.id,
            organizationId = po.organizationId,
            poNumber = po.poNumber,
            supplierId = supplierId ?: po.supplierId,
            supplierName = supplierName ?: po.supplierName,
            supplierVat = supplierVat ?: po.supplierVat,
            supplierAddress = supplierAddress ?: po.supplierAddress,
            issueDate = po.issueDate,
            expectedDate = expectedDate ?: po.expectedDate,
            status = po.status,
            subtotal = po.subtotal,
            vatTotal = po.vatTotal,
            total = po.total,
            notes = notes ?: po.notes
        )
        val saved = poRepo.save(merged)
        audit.log(AuditLogger.AuditEvent(
            action = "PO_UPDATED",
            entityType = "PurchaseOrder",
            entityId = poId.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("number" to po.poNumber)
        ))
        return saved
    }

    // ── Internal ───────────────────────────────────────────────────

    private fun recalcTotals(poId: UUID) {
        val lines = poLineRepo.findByPoId(poId)
        val subtotal = lines.sumOf { it.totalPrice }
        val vatTotal = lines.sumOf { it.totalPrice * it.vatRate / BigDecimal("100") }
        val total = subtotal + vatTotal
        poRepo.findById(poId).ifPresent { po ->
            poRepo.save(PurchaseOrder(
                id = po.id,
                organizationId = po.organizationId,
                poNumber = po.poNumber,
                supplierId = po.supplierId,
                supplierName = po.supplierName,
                supplierVat = po.supplierVat,
                supplierAddress = po.supplierAddress,
                issueDate = po.issueDate,
                expectedDate = po.expectedDate,
                status = po.status,
                subtotal = subtotal,
                vatTotal = vatTotal,
                total = total,
                notes = po.notes
            ))
        }
    }

    private fun nextPoNumber(organizationId: UUID): String {
        val year = LocalDate.now().year
        val seq = numberSequenceRepo.findWithLock(organizationId, "OC", year)
            .orElse(NumberSequence(NumberSequenceId(organizationId, "OC", year), 0))
        seq.counter += 1
        numberSequenceRepo.save(seq)
        return "OC-$year-${organizationId.toString().take(8).uppercase()}-${seq.counter}"
    }
}
