package com.blacksmith.metalstore.inventory.domain.repository

import com.blacksmith.metalstore.inventory.domain.entity.StockMovement
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface StockMovementRepository : JpaRepository<StockMovement, UUID> {
    fun findByOrganizationIdAndInventoryItemIdOrderByPerformedAtDesc(
        organizationId: UUID,
        inventoryItemId: UUID,
        pageable: Pageable
    ): Page<StockMovement>
}
