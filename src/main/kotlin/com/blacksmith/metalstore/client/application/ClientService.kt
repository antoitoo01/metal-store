package com.blacksmith.metalstore.client.application

import com.blacksmith.metalstore.auth.audit.AuditLogger
import com.blacksmith.metalstore.client.domain.entity.Client
import com.blacksmith.metalstore.client.domain.entity.ClientStatus
import com.blacksmith.metalstore.client.domain.repository.ClientRepository
import com.blacksmith.metalstore.shared.exception.ResourceNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class ClientService(
    private val repo: ClientRepository,
    private val audit: AuditLogger
) {
    @Transactional(readOnly = true)
    fun findAll(organizationId: UUID, pageable: Pageable, nameFilter: String? = null): Page<Client> =
        if (nameFilter.isNullOrBlank()) repo.findByOrganizationId(organizationId, pageable)
        else repo.findByOrganizationIdAndNameContainingIgnoreCase(organizationId, nameFilter, pageable)

    @Transactional(readOnly = true)
    fun findById(organizationId: UUID, id: UUID): Client =
        repo.findById(id).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("Client", id) }

    fun create(client: Client): Client {
        val saved = repo.save(client)
        audit.log(AuditLogger.AuditEvent(
            action = "CLIENT_CREATED",
            entityType = "Client",
            entityId = saved.id.toString(),
            organizationId = saved.organizationId.toString(),
            details = mapOf("name" to saved.name, "vatNumber" to saved.vatNumber)
        ))
        return saved
    }

    fun update(organizationId: UUID, id: UUID, updated: Client): Client {
        val existing = repo.findById(id).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("Client", id) }
        val merged = Client(
            id = existing.id,
            organizationId = existing.organizationId,
            name = updated.name.ifBlank { existing.name },
            email = updated.email ?: existing.email,
            phone = updated.phone ?: existing.phone,
            address = updated.address ?: existing.address,
            vatNumber = updated.vatNumber ?: existing.vatNumber,
            notes = updated.notes ?: existing.notes,
            status = updated.status
        )
        val saved = repo.save(merged)
        audit.log(AuditLogger.AuditEvent(
            action = "CLIENT_UPDATED",
            entityType = "Client",
            entityId = saved.id.toString(),
            organizationId = organizationId.toString(),
            details = mapOf("name" to saved.name)
        ))
        return saved
    }

    fun delete(organizationId: UUID, id: UUID) {
        val client = repo.findById(id).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("Client", id) }
        repo.delete(client)
        audit.log(AuditLogger.AuditEvent(
            action = "CLIENT_DELETED",
            entityType = "Client",
            entityId = id.toString(),
            organizationId = organizationId.toString()
        ))
    }

    fun activate(organizationId: UUID, id: UUID): Client {
        val client = repo.findById(id).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("Client", id) }
        val saved = repo.save(Client(
            id = client.id,
            organizationId = client.organizationId,
            name = client.name,
            email = client.email,
            phone = client.phone,
            address = client.address,
            vatNumber = client.vatNumber,
            notes = client.notes,
            status = ClientStatus.ACTIVE
        ))
        audit.log(AuditLogger.AuditEvent(action = "CLIENT_ACTIVATED", entityType = "Client", entityId = id.toString(), organizationId = organizationId.toString()))
        return saved
    }

    fun deactivate(organizationId: UUID, id: UUID): Client {
        val client = repo.findById(id).filter { it.organizationId == organizationId }
            .orElseThrow { ResourceNotFoundException("Client", id) }
        val saved = repo.save(Client(
            id = client.id,
            organizationId = client.organizationId,
            name = client.name,
            email = client.email,
            phone = client.phone,
            address = client.address,
            vatNumber = client.vatNumber,
            notes = client.notes,
            status = ClientStatus.INACTIVE
        ))
        audit.log(AuditLogger.AuditEvent(action = "CLIENT_DEACTIVATED", entityType = "Client", entityId = id.toString(), organizationId = organizationId.toString()))
        return saved
    }
}
