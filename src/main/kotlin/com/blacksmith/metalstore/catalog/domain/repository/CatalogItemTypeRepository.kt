package com.blacksmith.metalstore.catalog.domain.repository

import com.blacksmith.metalstore.catalog.domain.entity.CatalogItemType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CatalogItemTypeRepository : JpaRepository<CatalogItemType, UUID> {
    fun findByTenantId(tenantId: UUID, pageable: Pageable): Page<CatalogItemType>
    fun findByTenantIdAndNameContainingIgnoreCase(tenantId: UUID, name: String, pageable: Pageable): Page<CatalogItemType>
}
