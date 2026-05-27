package com.blacksmith.metalstore.catalog.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.util.UUID

@Entity
@DiscriminatorValue("EURO")
@Table(name = "euro_profiles")
class EuroProfile(
    id: UUID = UUID.randomUUID(),
    family: CatalogFamily,
    designation: String,
    weightKgM: BigDecimal? = null,
    areaCm2: BigDecimal? = null,

    @Column(precision = 8, scale = 2)
    val heightCm: BigDecimal? = null,

    @Column(precision = 8, scale = 2)
    val widthCm: BigDecimal? = null,

    @Column(precision = 8, scale = 3)
    val webThicknessCm: BigDecimal? = null,

    @Column(precision = 8, scale = 3)
    val flangeThicknessCm: BigDecimal? = null,

    @Column(precision = 8, scale = 3)
    val rootRadiusCm: BigDecimal? = null,

    @Column(precision = 14, scale = 4)
    val iyCm4: BigDecimal? = null,

    @Column(precision = 12, scale = 4)
    val iyCm: BigDecimal? = null,

    @Column(precision = 10, scale = 4)
    val welYCm3: BigDecimal? = null,

    @Column(precision = 10, scale = 4)
    val wplYCm3: BigDecimal? = null,

    @Column(precision = 14, scale = 4)
    val izCm4: BigDecimal? = null,

    @Column(precision = 12, scale = 4)
    val izCm: BigDecimal? = null,

    @Column(precision = 10, scale = 4)
    val welZCm3: BigDecimal? = null,

    @Column(precision = 10, scale = 4)
    val wplZCm3: BigDecimal? = null,

    @Column(precision = 10, scale = 4)
    val itCm4: BigDecimal? = null,

    @Column(precision = 14, scale = 4)
    val iwCm6: BigDecimal? = null,

    @Column(precision = 10, scale = 4)
    val nplRdKn: BigDecimal? = null,

    @Column(precision = 10, scale = 4)
    val vplRdZKn: BigDecimal? = null,

    @Column(precision = 10, scale = 4)
    val vplRdYKn: BigDecimal? = null,

    @Column(precision = 10, scale = 4)
    val melRdYKnm: BigDecimal? = null,

    @Column(precision = 10, scale = 4)
    val mplRdYKnm: BigDecimal? = null,

    @Column(precision = 10, scale = 4)
    val melRdZKnm: BigDecimal? = null,

    @Column(precision = 10, scale = 4)
    val mplRdZKnm: BigDecimal? = null

) : CatalogProfile(id, family, designation, weightKgM, areaCm2)
