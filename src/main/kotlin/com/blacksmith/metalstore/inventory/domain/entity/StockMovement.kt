package com.blacksmith.metalstore.inventory.domain.entity

import com.blacksmith.metalstore.shared.BaseEntity
import jakarta.persistence.*
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Objects
import java.util.UUID

enum class MovementType {
    INBOUND, OUTBOUND, ADJUSTMENT
}

enum class ReferenceType {
    PURCHASE_ORDER, DELIVERY_NOTE, MANUAL_ADJUSTMENT, SALE
}

@Entity
@Table(
    name = "stock_movements",
    indexes = [
        Index(name = "idx_smov_tenant", columnList = "organization_id"),
        Index(name = "idx_smov_inventory", columnList = "inventory_item_id"),
        Index(name = "idx_smov_type", columnList = "movement_type")
    ]
)
class StockMovement(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val organizationId: UUID,

    @Column(nullable = false)
    val inventoryItemId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val movementType: MovementType,

    @field:Positive
    @Column(nullable = false, precision = 12, scale = 4)
    val quantity: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    val referenceType: ReferenceType? = null,

    val referenceId: UUID? = null,

    @Column(nullable = false, precision = 12, scale = 4)
    val previousQuantity: BigDecimal,

    @Column(nullable = false, precision = 12, scale = 4)
    val newQuantity: BigDecimal,

    @Column(columnDefinition = "TEXT")
    val notes: String? = null,

    @Column(nullable = false, updatable = false)
    val performedAt: LocalDateTime = LocalDateTime.now()
) : BaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as StockMovement
        return id == that.id
    }

    override fun hashCode(): Int = Objects.hash(id)
}
