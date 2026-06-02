package com.blacksmith.metalstore.catalog.domain.entity

import com.blacksmith.metalstore.shared.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.UUID

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "standard", discriminatorType = DiscriminatorType.STRING, length = 10)
@Table(name = "catalog_profiles")
abstract class CatalogProfile(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    val family: CatalogFamily,

    @field:Lob
    @field:Access(AccessType.FIELD)
    @field:Column(nullable = false, columnDefinition = "TEXT")
    val designation: String,

    @Column(precision = 10, scale = 4)
    val weightKgM: BigDecimal? = null,

    @Column(precision = 10, scale = 4)
    val areaCm2: BigDecimal? = null,

    var imagePath: String? = null
) : BaseEntity()
