package com.blacksmith.metalstore.catalog.domain.repository

import com.blacksmith.metalstore.catalog.domain.entity.CatalogItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CatalogItemRepository : JpaRepository<CatalogItem, UUID> {
    fun findByItemType(itemType: String): List<CatalogItem>
    fun findByDesignationContainingIgnoreCase(designation: String): List<CatalogItem>
}
