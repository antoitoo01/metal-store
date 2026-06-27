package com.blacksmith.metalstore.catalog.domain.entity

import com.blacksmith.metalstore.shared.domain.MaterialType
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.UUID

@Entity
@DiscriminatorValue("AISC")
@Table(name = "aisc_profiles")
class AiscProfile(
    id: UUID = UUID.randomUUID(),
    family: CatalogFamily,
    designation: String,
    weightKgM: BigDecimal? = null,
    areaCm2: BigDecimal? = null,

    @Column(length = 10)
    val typeNorm: String? = null,

    @Column(length = 5)
    val tF: String? = null,

    @Column(precision = 8, scale = 2)
    val depthMm: BigDecimal? = null,

    @Column(precision = 8, scale = 2)
    val flangeWidthMm: BigDecimal? = null,

    @Column(precision = 8, scale = 3)
    val webThicknessMm: BigDecimal? = null,

    @Column(precision = 8, scale = 3)
    val flangeThicknessMm: BigDecimal? = null,

    @Column(precision = 12, scale = 4)
    val ixCm4: BigDecimal? = null,

    @Column(precision = 10, scale = 4)
    val zxCm3: BigDecimal? = null,

    @Column(precision = 10, scale = 4)
    val sxCm3: BigDecimal? = null,

    @Column(precision = 8, scale = 4)
    val rxCm: BigDecimal? = null,

    @Column(precision = 12, scale = 4)
    val iyCm4: BigDecimal? = null,

    @Column(precision = 10, scale = 4)
    val zyCm3: BigDecimal? = null,

    @Column(precision = 10, scale = 4)
    val syCm3: BigDecimal? = null,

    @Column(precision = 8, scale = 4)
    val ryCm: BigDecimal? = null,

    @Column(precision = 12, scale = 4)
    val jCm4: BigDecimal? = null,

    @Column(precision = 14, scale = 4)
    val cwCm6: BigDecimal? = null,

    materialType: MaterialType? = null

) : CatalogProfile(id, family, designation, weightKgM, areaCm2, materialType = materialType)
