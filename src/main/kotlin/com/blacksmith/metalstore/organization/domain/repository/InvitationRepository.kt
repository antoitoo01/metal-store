package com.blacksmith.metalstore.organization.domain.repository

import com.blacksmith.metalstore.organization.domain.entity.Invitation
import com.blacksmith.metalstore.organization.domain.entity.InvitationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface InvitationRepository : JpaRepository<Invitation, UUID> {
    fun findByToken(token: UUID): Invitation?

    fun findByOrganizationIdAndStatus(
        organizationId: UUID, status: InvitationStatus
    ): List<Invitation>

    fun existsByEmailAndOrganizationIdAndStatus(
        email: String, organizationId: UUID, status: InvitationStatus
    ): Boolean
}
