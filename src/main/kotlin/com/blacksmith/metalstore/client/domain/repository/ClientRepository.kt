package com.blacksmith.metalstore.client.domain.repository

import com.blacksmith.metalstore.client.domain.entity.Client
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ClientRepository : JpaRepository<Client, UUID> {
    fun findByOrganizationId(organizationId: UUID, pageable: Pageable): Page<Client>
    fun findByOrganizationIdAndNameContainingIgnoreCase(organizationId: UUID, name: String, pageable: Pageable): Page<Client>
    fun findByOrganizationIdAndVatNumber(organizationId: UUID, vatNumber: String): Client?
}
