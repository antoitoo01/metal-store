package com.blacksmith.metalstore.catalog.domain.repository

import com.blacksmith.metalstore.catalog.domain.entity.CatalogItemType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CatalogItemTypeRepository : JpaRepository<CatalogItemType, UUID> {
    fun findByOrganizationId(organizationId: UUID, pageable: Pageable): Page<CatalogItemType>
    fun findByOrganizationIdAndNameContainingIgnoreCase(organizationId: UUID, name: String, pageable: Pageable): Page<CatalogItemType>
}
