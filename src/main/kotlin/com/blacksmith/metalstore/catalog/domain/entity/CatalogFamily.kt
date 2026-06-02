package com.blacksmith.metalstore.catalog.domain.entity

import com.blacksmith.metalstore.shared.BaseEntity
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "catalog_families", uniqueConstraints = [UniqueConstraint(columnNames = ["standard", "code"])])
data class CatalogFamily(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, length = 10)
    val standard: String,

    @Column(nullable = false, length = 10)
    val code: String,

    @Column(nullable = false, length = 10)
    val shapeType: String,

    @Column(length = 100)
    val description: String? = null
) : BaseEntity()
