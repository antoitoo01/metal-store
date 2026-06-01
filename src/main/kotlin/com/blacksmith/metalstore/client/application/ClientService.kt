package com.blacksmith.metalstore.client.application

import com.blacksmith.metalstore.auth.audit.AuditLogger
import com.blacksmith.metalstore.client.domain.entity.Client
import com.blacksmith.metalstore.client.domain.entity.ClientStatus
import com.blacksmith.metalstore.client.domain.repository.ClientRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class ClientService(
    private val repo: ClientRepository,
    private val audit: AuditLogger
) {
    @Transactional(readOnly = true)
    fun findAll(tenantId: UUID, pageable: Pageable, nameFilter: String? = null): Page<Client> =
        if (nameFilter.isNullOrBlank()) repo.findByTenantId(tenantId, pageable)
        else repo.findByTenantIdAndNameContainingIgnoreCase(tenantId, nameFilter, pageable)

    @Transactional(readOnly = true)
    fun findById(tenantId: UUID, id: UUID): Client? =
        repo.findById(id).filter { it.tenantId == tenantId }.orElse(null)

    fun create(client: Client): Client {
        val saved = repo.save(client)
        audit.log(AuditLogger.AuditEvent(
            action = "CLIENT_CREATED",
            entityType = "Client",
            entityId = saved.id.toString(),
            tenantId = saved.tenantId.toString(),
            details = mapOf("name" to saved.name, "vatNumber" to saved.vatNumber)
        ))
        return saved
    }

    fun update(tenantId: UUID, id: UUID, updated: Client): Client? {
        val existing = repo.findById(id).filter { it.tenantId == tenantId }.orElse(null) ?: return null
        val merged = existing.copy(
            name = updated.name.ifBlank { existing.name },
            email = updated.email ?: existing.email,
            phone = updated.phone ?: existing.phone,
            address = updated.address ?: existing.address,
            vatNumber = updated.vatNumber ?: existing.vatNumber,
            notes = updated.notes ?: existing.notes,
            status = updated.status,
            lastModifiedDate = LocalDateTime.now()
        )
        val saved = repo.save(merged)
        audit.log(AuditLogger.AuditEvent(
            action = "CLIENT_UPDATED",
            entityType = "Client",
            entityId = saved.id.toString(),
            tenantId = tenantId.toString(),
            details = mapOf("name" to saved.name)
        ))
        return saved
    }

    fun delete(tenantId: UUID, id: UUID): Boolean {
        val client = repo.findById(id).filter { it.tenantId == tenantId }.orElse(null) ?: return false
        repo.delete(client)
        audit.log(AuditLogger.AuditEvent(
            action = "CLIENT_DELETED",
            entityType = "Client",
            entityId = id.toString(),
            tenantId = tenantId.toString()
        ))
        return true
    }

    fun activate(tenantId: UUID, id: UUID): Client? {
        val client = repo.findById(id).filter { it.tenantId == tenantId }.orElse(null) ?: return null
        val saved = repo.save(client.copy(status = ClientStatus.ACTIVE, lastModifiedDate = LocalDateTime.now()))
        audit.log(AuditLogger.AuditEvent(action = "CLIENT_ACTIVATED", entityType = "Client", entityId = id.toString(), tenantId = tenantId.toString()))
        return saved
    }

    fun deactivate(tenantId: UUID, id: UUID): Client? {
        val client = repo.findById(id).filter { it.tenantId == tenantId }.orElse(null) ?: return null
        val saved = repo.save(client.copy(status = ClientStatus.INACTIVE, lastModifiedDate = LocalDateTime.now()))
        audit.log(AuditLogger.AuditEvent(action = "CLIENT_DEACTIVATED", entityType = "Client", entityId = id.toString(), tenantId = tenantId.toString()))
        return saved
    }
}
