package com.blacksmith.metalstore.organization.domain.repository

import com.blacksmith.metalstore.organization.domain.entity.Membership
import com.blacksmith.metalstore.organization.domain.entity.MembershipStatus
import com.blacksmith.metalstore.organization.domain.entity.OrganizationRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface MembershipRepository : JpaRepository<Membership, UUID> {
    fun findByUserIdAndOrganizationIdAndStatus(
        userId: UUID, organizationId: UUID, status: MembershipStatus
    ): Membership?

    fun findByOrganizationId(organizationId: UUID): List<Membership>

    fun findByOrganizationIdAndStatus(
        organizationId: UUID, status: MembershipStatus
    ): List<Membership>

    fun existsByUserIdAndOrganizationIdAndRole(
        userId: UUID, organizationId: UUID, role: OrganizationRole
    ): Boolean

    fun findByUserId(userId: UUID): List<Membership>
}
