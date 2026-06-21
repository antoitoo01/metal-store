package com.blacksmith.metalstore.catalog.domain.dto.response

import com.blacksmith.metalstore.catalog.domain.entity.AiscProfile
import com.blacksmith.metalstore.catalog.domain.entity.CatalogProfile
import com.blacksmith.metalstore.catalog.domain.entity.EuroProfile
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class CatalogProfileResponse(
    @field:Schema(description = "Identificador único del perfil")
    val id: UUID,
    @field:Schema(description = "Designación del perfil")
    val designation: String,
    @field:Schema(description = "Peso por metro (kg/m)")
    val weightKgM: BigDecimal?,
    @field:Schema(description = "Área (cm²)")
    val areaCm2: BigDecimal?,
    @field:Schema(description = "Altura (mm)")
    val h: BigDecimal?,
    @field:Schema(description = "Anchura (mm)")
    val b: BigDecimal?,
    @field:Schema(description = "Espesor del alma (mm)")
    val tw: BigDecimal?,
    @field:Schema(description = "Espesor del ala (mm)")
    val tf: BigDecimal?,
    @field:Schema(description = "Radio de acuerdo (mm)")
    val r: BigDecimal?,
    @field:Schema(description = "Ruta de la imagen")
    val imagePath: String?,
    @field:Schema(description = "Identificador de la familia")
    val familyId: UUID,
    @field:Schema(description = "Norma de la familia")
    val familyStandard: String,
    @field:Schema(description = "Código de la familia")
    val familyCode: String,
    @field:Schema(description = "Tipo de forma de la familia")
    val familyShapeType: String,
    @field:Schema(description = "Descripción de la familia")
    val familyDescription: String?,
    @field:Schema(description = "Fecha de creación")
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(e: CatalogProfile) = CatalogProfileResponse(
            id = e.id, designation = e.designation,
            weightKgM = e.weightKgM, areaCm2 = e.areaCm2,
            h = when (e) {
                is EuroProfile -> e.heightCm?.multiply(BigDecimal.TEN)
                is AiscProfile -> e.depthMm
                else -> null
            },
            b = when (e) {
                is EuroProfile -> e.widthCm?.multiply(BigDecimal.TEN)
                is AiscProfile -> e.flangeWidthMm
                else -> null
            },
            tw = when (e) {
                is EuroProfile -> e.webThicknessCm?.multiply(BigDecimal.TEN)
                is AiscProfile -> e.webThicknessMm
                else -> null
            },
            tf = when (e) {
                is EuroProfile -> e.flangeThicknessCm?.multiply(BigDecimal.TEN)
                is AiscProfile -> e.flangeThicknessMm
                else -> null
            },
            r = when (e) {
                is EuroProfile -> e.rootRadiusCm?.multiply(BigDecimal.TEN)
                else -> null
            },
            imagePath = e.imagePath,
            familyId = e.family.id,
            familyStandard = e.family.standard,
            familyCode = e.family.code,
            familyShapeType = e.family.shapeType,
            familyDescription = e.family.description,
            createdAt = e.createdAt
        )
    }
}
