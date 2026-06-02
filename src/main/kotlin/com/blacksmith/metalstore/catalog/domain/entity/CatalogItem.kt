package com.blacksmith.metalstore.catalog.domain.entity

import com.blacksmith.metalstore.shared.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "catalog_items")
data class CatalogItem(
    @Id
    val id: UUID = UUID.randomUUID(),

    val typeId: UUID? = null,

    @Column(nullable = false, length = 30)
    val itemType: String,

    val sku: String? = null,

    @field:Lob
    @field:Access(AccessType.FIELD)
    @field:Column(nullable = false, columnDefinition = "TEXT")
    val designation: String,

    @Column(columnDefinition = "TEXT")
    val dimensions: String? = null,

    @Column(precision = 10, scale = 4)
    val weightKgM: BigDecimal? = null,

    val material: String? = null,

    @Column(precision = 10, scale = 4)
    val estimatedPriceKg: BigDecimal = BigDecimal.ZERO,

    @Column(columnDefinition = "TEXT")
    val metadata: String? = null,

    var imagePath: String? = null
) : BaseEntity()
