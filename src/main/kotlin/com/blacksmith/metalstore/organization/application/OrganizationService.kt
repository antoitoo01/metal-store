package com.blacksmith.metalstore.organization.application

import com.blacksmith.metalstore.organization.domain.dto.request.CreateOrganizationRequest
import com.blacksmith.metalstore.organization.domain.dto.request.UpdateOrganizationRequest
import com.blacksmith.metalstore.organization.domain.dto.response.MembershipResponse
import com.blacksmith.metalstore.organization.domain.dto.response.OrganizationResponse
import com.blacksmith.metalstore.organization.domain.entity.*
import com.blacksmith.metalstore.organization.domain.repository.MembershipRepository
import com.blacksmith.metalstore.organization.domain.repository.OrganizationRepository
import com.blacksmith.metalstore.organization.exception.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class OrganizationService(
    private val orgRepository: OrganizationRepository,
    private val membershipRepository: MembershipRepository,
) {
    fun findOrganizationsByUserId(userId: UUID): List<OrganizationResponse> {
        val memberships = membershipRepository.findByUserId(userId)
        return memberships.map { m ->
            val org = orgRepository.findById(m.organizationId).orElse(null) ?: return@map null
            val count = membershipRepository.findByOrganizationId(m.organizationId).size
            OrganizationResponse.from(org, count)
        }.filterNotNull()
    }

    fun findOrganization(orgId: UUID): OrganizationResponse {
        val org = orgRepository.findById(orgId)
            .orElseThrow { OrganizationNotFoundException() }
        val count = membershipRepository.findByOrganizationId(orgId).size
        return OrganizationResponse.from(org, count)
    }

    @Transactional
    fun createOrganization(userId: UUID, request: CreateOrganizationRequest): OrganizationResponse {
        val slug = generateUniqueSlug(request.name)
        val org = orgRepository.save(Organization(name = request.name, slug = slug))
        membershipRepository.save(Membership(
            userId = userId,
            organizationId = org.id,
            role = OrganizationRole.ORGANIZATION_OWNER,
            status = MembershipStatus.ACTIVE,
        ))
        return OrganizationResponse.from(org, 1)
    }

    @Transactional
    fun updateOrganization(orgId: UUID, request: UpdateOrganizationRequest, currentUserId: UUID): OrganizationResponse {
        requireAdminOrOwner(orgId, currentUserId)
        val org = orgRepository.findById(orgId).orElseThrow { OrganizationNotFoundException() }
        org.name = request.name
        val saved = orgRepository.save(org)
        return OrganizationResponse.from(saved)
    }

    fun getMembers(orgId: UUID): List<MembershipResponse> {
        return membershipRepository.findByOrganizationId(orgId).map { MembershipResponse.from(it) }
    }

    fun getMyMembership(orgId: UUID, userId: UUID): MembershipResponse {
        val membership = membershipRepository.findByUserIdAndOrganizationIdAndStatus(userId, orgId, MembershipStatus.ACTIVE)
            ?: throw MembershipNotFoundException()
        return MembershipResponse.from(membership)
    }

    @Transactional
    fun updateMemberRole(orgId: UUID, targetUserId: UUID, newRole: OrganizationRole, currentUserId: UUID) {
        requireAdminOrOwner(orgId, currentUserId)
        val membership = membershipRepository.findByUserIdAndOrganizationIdAndStatus(targetUserId, orgId, MembershipStatus.ACTIVE)
            ?: throw MembershipNotFoundException()
        membership.role = newRole
        membershipRepository.save(membership)
    }

    @Transactional
    fun removeMember(orgId: UUID, targetUserId: UUID, currentUserId: UUID) {
        requireAdminOrOwner(orgId, currentUserId)
        val membership = membershipRepository.findByUserIdAndOrganizationIdAndStatus(targetUserId, orgId, MembershipStatus.ACTIVE)
            ?: throw MembershipNotFoundException()
        if (membership.role == OrganizationRole.ORGANIZATION_OWNER) {
            val owners = membershipRepository.findByOrganizationId(orgId)
                .filter { it.role == OrganizationRole.ORGANIZATION_OWNER && it.status == MembershipStatus.ACTIVE }
            if (owners.size <= 1) throw CannotRemoveLastOwnerException()
        }
        membershipRepository.delete(membership)
    }

    private fun requireAdminOrOwner(orgId: UUID, userId: UUID) {
        val membership = membershipRepository.findByUserIdAndOrganizationIdAndStatus(userId, orgId, MembershipStatus.ACTIVE)
            ?: throw MembershipNotFoundException()
        if (membership.role != OrganizationRole.ORGANIZATION_OWNER && membership.role != OrganizationRole.ADMIN) {
            throw RoleRequiredException("OWNER or ADMIN")
        }
    }

    fun findMembership(userId: UUID, orgId: UUID): Membership? {
        return membershipRepository.findByUserIdAndOrganizationIdAndStatus(userId, orgId, MembershipStatus.ACTIVE)
    }

    private fun generateUniqueSlug(name: String): String {
        val base = name.lowercase()
            .replace(Regex("[^a-z0-9\\s-]"), "")
            .replace(Regex("\\s+"), "-")
            .trim('-')
            .take(50)
        if (base.isBlank()) return "org-${UUID.randomUUID().toString().take(8)}"
        var slug = base
        var counter = 1
        while (orgRepository.existsBySlug(slug)) {
            slug = "$base-$counter"
            counter++
        }
        return slug
    }
}
