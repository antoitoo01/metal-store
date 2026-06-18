package com.blacksmith.metalstore.catalog.domain.entity

import com.blacksmith.metalstore.shared.BaseEntity
import jakarta.persistence.*
import java.util.Objects
import java.util.UUID

@Entity
@Table(
    name = "catalog_item_types",
    indexes = [Index(name = "idx_itemtype_tenant", columnList = "organization_id")]
)
class CatalogItemType(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val organizationId: UUID,

    @Column(nullable = false)
    val name: String,
    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(columnDefinition = "TEXT")
    val schemaDefinition: String? = null
) : BaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as CatalogItemType
        return id == that.id
    }

    override fun hashCode(): Int = Objects.hash(id)
}
