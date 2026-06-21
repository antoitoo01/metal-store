package com.blacksmith.metalstore.catalog.domain.entity

import com.blacksmith.metalstore.shared.BaseEntity
import jakarta.persistence.*
import java.util.Objects
import java.util.UUID

@Entity
@Table(name = "catalog_families", uniqueConstraints = [UniqueConstraint(columnNames = ["standard", "code"])])
class CatalogFamily(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, length = 10)
    val standard: String,

    @Column(nullable = false, length = 10)
    val code: String,

    @Column(nullable = false, length = 10)
    val shapeType: String,

    @Column(length = 100, columnDefinition = "TEXT")
    val description: String? = null
) : BaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as CatalogFamily
        return id == that.id
    }

    override fun hashCode(): Int = Objects.hash(id)
}
