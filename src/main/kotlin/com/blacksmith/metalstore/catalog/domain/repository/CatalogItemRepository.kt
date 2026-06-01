package com.blacksmith.metalstore.catalog.domain.repository

import com.blacksmith.metalstore.catalog.domain.entity.CatalogItem
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CatalogItemRepository : JpaRepository<CatalogItem, UUID> {
    fun findByItemType(itemType: String, pageable: Pageable): Page<CatalogItem>
    fun findByDesignationContainingIgnoreCase(designation: String, pageable: Pageable): Page<CatalogItem>

    @Query("""
        SELECT i FROM CatalogItem i
        WHERE (:q IS NULL OR LOWER(i.designation) LIKE LOWER(CONCAT('%', :q, '%')))
        AND (:itemType IS NULL OR i.itemType = :itemType)
    """)
    fun searchItems(
        @Param("q") q: String?,
        @Param("itemType") itemType: String?,
        pageable: Pageable
    ): Page<CatalogItem>
}
