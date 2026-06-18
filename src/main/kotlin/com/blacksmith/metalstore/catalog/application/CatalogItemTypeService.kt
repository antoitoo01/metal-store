package com.blacksmith.metalstore.catalog.application

import com.blacksmith.metalstore.catalog.domain.entity.CatalogItemType
import com.blacksmith.metalstore.catalog.domain.repository.CatalogItemTypeRepository
import com.blacksmith.metalstore.shared.exception.ResourceNotFoundException
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
    fun findById(organizationId: UUID, id: UUID): CatalogItemType =
        repo.findById(id).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("CatalogItemType", id) }

    fun create(type: CatalogItemType): CatalogItemType = repo.save(type)

    fun update(organizationId: UUID, id: UUID, updated: CatalogItemType): CatalogItemType {
        val existing = repo.findById(id).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("CatalogItemType", id) }
        val merged = CatalogItemType(
            id = existing.id,
            organizationId = existing.organizationId,
            name = updated.name,
            description = updated.description ?: existing.description,
            schemaDefinition = updated.schemaDefinition ?: existing.schemaDefinition
        )
        return repo.save(merged)
    }

    fun delete(organizationId: UUID, id: UUID) {
        val type = repo.findById(id).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("CatalogItemType", id) }
        repo.delete(type)
    }
}
