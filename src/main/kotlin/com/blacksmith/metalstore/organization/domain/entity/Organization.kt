package com.blacksmith.metalstore.organization.domain.entity

import com.blacksmith.metalstore.shared.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "organizations")
data class Organization(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var slug: String,

    @Column(columnDefinition = "TEXT")
    var settings: String? = null,
) : BaseEntity()
