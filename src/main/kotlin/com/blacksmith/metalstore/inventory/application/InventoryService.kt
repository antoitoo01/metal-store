package com.blacksmith.metalstore.inventory.application

import com.blacksmith.metalstore.auth.audit.AuditLogger
import com.blacksmith.metalstore.inventory.domain.entity.InventoryItem
import com.blacksmith.metalstore.inventory.domain.entity.assertValidSource
import com.blacksmith.metalstore.inventory.domain.repository.InventoryItemRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class InventoryService(
    private val repo: InventoryItemRepository,
    private val audit: AuditLogger
) {
    @Transactional(readOnly = true)
    fun findAll(organizationId: UUID, pageable: Pageable): Page<InventoryItem> =
        repo.findByOrganizationId(organizationId, pageable)

    @Transactional(readOnly = true)
    fun findById(organizationId: UUID, id: UUID): InventoryItem? =
        repo.findById(id).filter { it.organizationId == organizationId }.orElse(null)

    fun create(item: InventoryItem): InventoryItem {
        require(item.assertValidSource()) { "InventoryItem must reference either a profile or an item (exclusive)" }
        val saved = repo.save(item)
        audit.log(AuditLogger.AuditEvent(
            action = "INVENTORY_CREATED",
            entityType = "InventoryItem",
            entityId = saved.id.toString(),
            organizationId = saved.organizationId.toString(),
            details = mapOf("profileId" to saved.profileId?.toString(), "quantity" to saved.quantity.toString())
        ))
        return saved
    }

    fun update(organizationId: UUID, id: UUID, updated: InventoryItem): InventoryItem? {
        val existing = repo.findById(id).filter { it.organizationId == organizationId }.orElse(null) ?: return null
        val merged = existing.copy(
            quantity = updated.quantity,
            location = updated.location ?: existing.location,
            costPriceEur = updated.costPriceEur ?: existing.costPriceEur,
            supplier = updated.supplier ?: existing.supplier,
            notes = updated.notes ?: existing.notes
        )
        val saved = repo.save(merged)
        audit.log(AuditLogger.AuditEvent(
            action = "INVENTORY_UPDATED",
            entityType = "InventoryItem",
            entityId = saved.id.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("quantity" to saved.quantity.toString())
        ))
        return saved
    }

    fun delete(organizationId: UUID, id: UUID): Boolean {
        val item = repo.findById(id).filter { it.organizationId == organizationId }.orElse(null) ?: return false
        repo.delete(item)
        audit.log(AuditLogger.AuditEvent(
            action = "INVENTORY_DELETED",
            entityType = "InventoryItem",
            entityId = id.toString(),
            organizationId = organizationId.toString()
        ))
        return true
    }
}
