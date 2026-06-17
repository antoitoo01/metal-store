package com.blacksmith.metalstore.inventory.domain.entity

import com.blacksmith.metalstore.shared.BaseEntity
import jakarta.persistence.*
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "inventory_items",
    indexes = [
        Index(name = "idx_inventory_tenant", columnList = "organization_id"),
        Index(name = "idx_inventory_profile", columnList = "profile_id"),
        Index(name = "idx_inventory_item", columnList = "item_id")
    ]
)
data class InventoryItem(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val organizationId: UUID,

    val profileId: UUID? = null,

    val itemId: UUID? = null,

    @field:Positive
    @Column(nullable = false, precision = 12, scale = 4)
    val quantity: BigDecimal,

    @Column(length = 255)
    val location: String? = null,

    @field:PositiveOrZero
    @Column(precision = 12, scale = 4)
    val costPriceEur: BigDecimal? = null,

    @Column(length = 255)
    val supplier: String? = null,

    @Column(nullable = false, updatable = false)
    val receivedAt: LocalDateTime = LocalDateTime.now(),

    @Column(columnDefinition = "TEXT")
    val notes: String? = null
) : BaseEntity()

fun InventoryItem.assertValidSource(): Boolean =
    (profileId != null) xor (itemId != null)
