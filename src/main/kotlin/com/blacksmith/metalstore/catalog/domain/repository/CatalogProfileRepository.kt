package com.blacksmith.metalstore.catalog.domain.repository

import com.blacksmith.metalstore.catalog.domain.entity.CatalogProfile
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface CatalogProfileRepository : JpaRepository<CatalogProfile, UUID> {
    @EntityGraph(attributePaths = ["family"])
    override fun findAll(pageable: Pageable): Page<CatalogProfile>

    @EntityGraph(attributePaths = ["family"])
    override fun findById(id: UUID): Optional<CatalogProfile>

    @EntityGraph(attributePaths = ["family"])
    fun findByFamilyStandard(standard: String, pageable: Pageable): Page<CatalogProfile>

    fun findByFamilyShapeType(shapeType: String): List<CatalogProfile>
    fun findByFamilyCode(familyCode: String): List<CatalogProfile>

    @EntityGraph(attributePaths = ["family"])
    fun findByDesignationContainingIgnoreCase(designation: String, pageable: Pageable): Page<CatalogProfile>

    @EntityGraph(attributePaths = ["family"])
    @Query("""
        SELECT p FROM CatalogProfile p JOIN p.family f
        WHERE (:q IS NULL OR LOWER(p.designation) LIKE LOWER(CONCAT('%', :q, '%')))
        AND (:standard IS NULL OR f.standard = :standard)
        AND (:shapeType IS NULL OR f.shapeType = :shapeType)
        AND (:familyCode IS NULL OR f.code = :familyCode)
    """)
    fun searchProfiles(
        @Param("q") q: String?,
        @Param("standard") standard: String?,
        @Param("shapeType") shapeType: String?,
        @Param("familyCode") familyCode: String?,
        pageable: Pageable
    ): Page<CatalogProfile>
}
