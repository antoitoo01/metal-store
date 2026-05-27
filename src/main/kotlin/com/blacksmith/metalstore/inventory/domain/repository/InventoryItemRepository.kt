package com.blacksmith.metalstore.inventory.domain.repository

import com.blacksmith.metalstore.inventory.domain.entity.InventoryItem
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface InventoryItemRepository : JpaRepository<InventoryItem, UUID> {
    fun findByTenantId(tenantId: UUID, pageable: Pageable): Page<InventoryItem>
    fun findByTenantIdAndProfileId(tenantId: UUID, profileId: UUID): List<InventoryItem>
    fun findByTenantIdAndItemId(tenantId: UUID, itemId: UUID): List<InventoryItem>
    fun findByTenantIdAndLocationContainingIgnoreCase(tenantId: UUID, location: String): List<InventoryItem>
}
