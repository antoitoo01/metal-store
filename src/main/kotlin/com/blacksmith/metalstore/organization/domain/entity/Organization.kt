package com.blacksmith.metalstore.organization.domain.entity

import com.blacksmith.metalstore.shared.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.Objects
import java.util.UUID

@Entity
@Table(name = "organizations")
class Organization(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var slug: String,

    @Column(columnDefinition = "TEXT")
    var settings: String? = null,
) : BaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as Organization
        return id == that.id
    }

    override fun hashCode(): Int = Objects.hash(id)
}
