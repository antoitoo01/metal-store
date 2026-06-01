package com.blacksmith.metalstore.client.domain.repository

import com.blacksmith.metalstore.client.domain.entity.Client
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ClientRepository : JpaRepository<Client, UUID> {
    fun findByTenantId(tenantId: UUID, pageable: Pageable): Page<Client>
    fun findByTenantIdAndNameContainingIgnoreCase(tenantId: UUID, name: String, pageable: Pageable): Page<Client>
    fun findByTenantIdAndVatNumber(tenantId: UUID, vatNumber: String): Client?
}
