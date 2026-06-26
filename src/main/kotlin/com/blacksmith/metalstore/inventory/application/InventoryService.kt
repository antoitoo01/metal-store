package com.blacksmith.metalstore.inventory.application

import com.blacksmith.metalstore.auth.audit.AuditLogger
import com.blacksmith.metalstore.inventory.domain.entity.InventoryItem
import com.blacksmith.metalstore.inventory.domain.entity.MovementType
import com.blacksmith.metalstore.inventory.domain.entity.ReferenceType
import com.blacksmith.metalstore.inventory.domain.entity.StockMovement
import com.blacksmith.metalstore.inventory.domain.entity.assertValidSource
import com.blacksmith.metalstore.inventory.domain.repository.InventoryItemRepository
import com.blacksmith.metalstore.inventory.domain.repository.StockMovementRepository
import com.blacksmith.metalstore.shared.exception.ResourceNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

@Service
@Transactional
class InventoryService(
    private val repo: InventoryItemRepository,
    private val movementRepo: StockMovementRepository,
    private val audit: AuditLogger
) {
    @Transactional(readOnly = true)
    fun findAll(organizationId: UUID, pageable: Pageable): Page<InventoryItem> =
        repo.findByOrganizationId(organizationId, pageable)

    @Transactional(readOnly = true)
    fun findById(organizationId: UUID, id: UUID): InventoryItem =
        repo.findById(id).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("InventoryItem", id) }

    @Transactional(readOnly = true)
    fun getMovements(organizationId: UUID, inventoryItemId: UUID, pageable: Pageable): Page<StockMovement> {
        findById(organizationId, inventoryItemId) // verify existence + org
        return movementRepo.findByOrganizationIdAndInventoryItemIdOrderByPerformedAtDesc(organizationId, inventoryItemId, pageable)
    }

    fun create(item: InventoryItem): InventoryItem {
        require(item.assertValidSource()) { "InventoryItem must reference either a profile or an item (exclusive)" }
        val saved = repo.save(item)
        registerMovement(saved.organizationId, saved.id, MovementType.INBOUND, saved.quantity, ReferenceType.MANUAL_ADJUSTMENT, null, "Entrada inicial", BigDecimal.ZERO, saved.quantity)
        audit.log(AuditLogger.AuditEvent(
            action = "INVENTORY_CREATED",
            entityType = "InventoryItem",
            entityId = saved.id.toString(),
            organizationId = saved.organizationId.toString(),
            details = mapOf("profileId" to saved.profileId?.toString(), "quantity" to saved.quantity.toString())
        ))
        return saved
    }

    fun update(organizationId: UUID, id: UUID, updated: InventoryItem): InventoryItem {
        val existing = repo.findById(id).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("InventoryItem", id) }
        val quantityDelta = updated.quantity - existing.quantity
        val merged = InventoryItem(
            id = existing.id,
            organizationId = existing.organizationId,
            profileId = existing.profileId,
            itemId = existing.itemId,
            quantity = updated.quantity,
            location = updated.location ?: existing.location,
            costPriceEur = updated.costPriceEur ?: existing.costPriceEur,
            supplier = updated.supplier ?: existing.supplier,
            receivedAt = existing.receivedAt,
            notes = updated.notes ?: existing.notes
        )
        val saved = repo.save(merged)
        if (quantityDelta != BigDecimal.ZERO) {
            val movementType = if (quantityDelta > BigDecimal.ZERO) MovementType.INBOUND else MovementType.OUTBOUND
            registerMovement(organizationId, id, movementType, quantityDelta.abs(), ReferenceType.MANUAL_ADJUSTMENT, null, "Ajuste por actualización", existing.quantity, saved.quantity)
        }
        audit.log(AuditLogger.AuditEvent(
            action = "INVENTORY_UPDATED",
            entityType = "InventoryItem",
            entityId = saved.id.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("quantity" to saved.quantity.toString())
        ))
        return saved
    }

    fun delete(organizationId: UUID, id: UUID) {
        val item = repo.findById(id).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("InventoryItem", id) }
        repo.delete(item)
        audit.log(AuditLogger.AuditEvent(
            action = "INVENTORY_DELETED",
            entityType = "InventoryItem",
            entityId = id.toString(),
            organizationId = organizationId.toString()
        ))
    }

    fun addStock(organizationId: UUID, id: UUID, quantity: BigDecimal, notes: String?): InventoryItem {
        val item = repo.findById(id).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("InventoryItem", id) }
        val previousQty = item.quantity
        val newQty = previousQty + quantity
        val merged = InventoryItem(
            id = item.id,
            organizationId = item.organizationId,
            profileId = item.profileId,
            itemId = item.itemId,
            quantity = newQty,
            location = item.location,
            costPriceEur = item.costPriceEur,
            supplier = item.supplier,
            receivedAt = item.receivedAt,
            notes = item.notes
        )
        val saved = repo.save(merged)
        registerMovement(organizationId, id, MovementType.INBOUND, quantity, ReferenceType.MANUAL_ADJUSTMENT, null, notes ?: "Entrada manual", previousQty, newQty)
        audit.log(AuditLogger.AuditEvent(
            action = "INVENTORY_STOCK_ADDED",
            entityType = "InventoryItem",
            entityId = id.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("quantity" to quantity.toString(), "previousQuantity" to previousQty.toString(), "newQuantity" to newQty.toString())
        ))
        return saved
    }

    fun removeStock(organizationId: UUID, id: UUID, quantity: BigDecimal, notes: String?): InventoryItem {
        val item = repo.findById(id).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("InventoryItem", id) }
        require(item.quantity >= quantity) { "Insufficient stock: available ${item.quantity}, requested $quantity" }
        val previousQty = item.quantity
        val newQty = previousQty - quantity
        val merged = InventoryItem(
            id = item.id,
            organizationId = item.organizationId,
            profileId = item.profileId,
            itemId = item.itemId,
            quantity = newQty,
            location = item.location,
            costPriceEur = item.costPriceEur,
            supplier = item.supplier,
            receivedAt = item.receivedAt,
            notes = item.notes
        )
        val saved = repo.save(merged)
        registerMovement(organizationId, id, MovementType.OUTBOUND, quantity, ReferenceType.MANUAL_ADJUSTMENT, null, notes ?: "Salida manual", previousQty, newQty)
        audit.log(AuditLogger.AuditEvent(
            action = "INVENTORY_STOCK_REMOVED",
            entityType = "InventoryItem",
            entityId = id.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("quantity" to quantity.toString(), "previousQuantity" to previousQty.toString(), "newQuantity" to newQty.toString())
        ))
        return saved
    }

    private fun registerMovement(
        organizationId: UUID,
        inventoryItemId: UUID,
        movementType: MovementType,
        quantity: BigDecimal,
        referenceType: ReferenceType?,
        referenceId: UUID?,
        notes: String?,
        previousQuantity: BigDecimal,
        newQuantity: BigDecimal
    ) {
        val movement = StockMovement(
            organizationId = organizationId,
            inventoryItemId = inventoryItemId,
            movementType = movementType,
            quantity = quantity,
            referenceType = referenceType,
            referenceId = referenceId,
            notes = notes,
            previousQuantity = previousQuantity,
            newQuantity = newQuantity
        )
        movementRepo.save(movement)
    }
}
