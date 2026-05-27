package com.blacksmith.metalstore.catalog.domain.repository

import com.blacksmith.metalstore.catalog.domain.entity.CatalogProfile
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CatalogProfileRepository : JpaRepository<CatalogProfile, UUID> {
    @EntityGraph(attributePaths = ["family"])
    override fun findAll(pageable: Pageable): Page<CatalogProfile>

    @EntityGraph(attributePaths = ["family"])
    fun findByFamilyStandard(standard: String, pageable: Pageable): Page<CatalogProfile>

    fun findByFamilyShapeType(shapeType: String): List<CatalogProfile>
    fun findByFamilyCode(familyCode: String): List<CatalogProfile>

    @EntityGraph(attributePaths = ["family"])
    fun findByDesignationContainingIgnoreCase(designation: String, pageable: Pageable): Page<CatalogProfile>
}
