package com.blacksmith.metalstore.catalog.application

import com.blacksmith.metalstore.catalog.domain.entity.CatalogItemType
import com.blacksmith.metalstore.catalog.domain.repository.CatalogItemTypeRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class CatalogItemTypeService(
    private val repo: CatalogItemTypeRepository
) {
    @Transactional(readOnly = true)
    fun list(organizationId: UUID, pageable: Pageable): Page<CatalogItemType> =
        repo.findByOrganizationId(organizationId, pageable)

    @Transactional(readOnly = true)
    fun findById(organizationId: UUID, id: UUID): CatalogItemType? =
        repo.findById(id).filter { it.organizationId == organizationId }.orElse(null)

    fun create(type: CatalogItemType): CatalogItemType = repo.save(type)

    fun update(organizationId: UUID, id: UUID, updated: CatalogItemType): CatalogItemType? {
        val existing = repo.findById(id).filter { it.organizationId == organizationId }.orElse(null) ?: return null
        val merged = existing.copy(
            name = updated.name,
            description = updated.description ?: existing.description,
            schemaDefinition = updated.schemaDefinition ?: existing.schemaDefinition
        )
        return repo.save(merged)
    }

    fun delete(organizationId: UUID, id: UUID): Boolean {
        val type = repo.findById(id).filter { it.organizationId == organizationId }.orElse(null) ?: return false
        repo.delete(type)
        return true
    }
}
