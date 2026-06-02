package com.blacksmith.metalstore.catalog.domain.entity

import com.blacksmith.metalstore.shared.BaseEntity
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(
    name = "catalog_item_types",
    indexes = [Index(name = "idx_itemtype_tenant", columnList = "tenantId")]
)
data class CatalogItemType(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val tenantId: UUID,

    @Column(nullable = false)
    val name: String,

    val description: String? = null,

    @Column(columnDefinition = "TEXT")
    val schemaDefinition: String? = null
) : BaseEntity()
