package com.blacksmith.metalstore.organization.application

import com.blacksmith.metalstore.organization.domain.dto.response.InvitationResponse
import com.blacksmith.metalstore.organization.domain.dto.response.MembershipResponse
import com.blacksmith.metalstore.organization.domain.entity.Invitation
import com.blacksmith.metalstore.organization.domain.entity.InvitationStatus
import com.blacksmith.metalstore.organization.domain.entity.Membership
import com.blacksmith.metalstore.organization.domain.entity.MembershipStatus
import com.blacksmith.metalstore.organization.domain.entity.OrganizationRole
import com.blacksmith.metalstore.organization.domain.repository.InvitationRepository
import com.blacksmith.metalstore.organization.domain.repository.MembershipRepository
import com.blacksmith.metalstore.organization.domain.repository.OrganizationRepository
import com.blacksmith.metalstore.organization.exception.DuplicateInvitationException
import com.blacksmith.metalstore.organization.exception.InvitationAlreadyAcceptedException
import com.blacksmith.metalstore.organization.exception.InvitationEmailMismatchException
import com.blacksmith.metalstore.organization.exception.InvitationExpiredException
import com.blacksmith.metalstore.organization.exception.InvitationNotFoundException
import com.blacksmith.metalstore.organization.exception.MembershipNotFoundException
import com.blacksmith.metalstore.organization.exception.OrganizationNotFoundException
import com.blacksmith.metalstore.organization.exception.RoleRequiredException
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class InvitationService(
    private val orgRepository: OrganizationRepository,
    private val membershipRepository: MembershipRepository,
    private val invitationRepository: InvitationRepository,
    @Value("\${app.invitation.base-url}") private val baseUrl: String,
) {
    @Transactional
    fun createInvitations(orgId: UUID, emails: List<String>, currentUserId: UUID): List<InvitationResponse> {
        val org = orgRepository.findById(orgId).orElseThrow { OrganizationNotFoundException() }
        requireAdminOrOwner(orgId, currentUserId)

        emails.forEach { email ->
            val existing = invitationRepository.findByEmailAndOrganizationIdAndStatus(email, orgId, InvitationStatus.PENDING)
            if (existing != null) throw DuplicateInvitationException()
        }

        return emails.map { email ->
            val invitation = invitationRepository.save(Invitation(
                organizationId = orgId,
                email = email.trim().lowercase(),
                createdBy = currentUserId,
            ))
            InvitationResponse.from(invitation, org.name, baseUrl)
        }
    }

    fun listInvitations(orgId: UUID, pageable: Pageable, currentUserId: UUID): Page<InvitationResponse> {
        requireAdminOrOwner(orgId, currentUserId)
        val org = orgRepository.findById(orgId).orElseThrow { OrganizationNotFoundException() }
        return invitationRepository.findByOrganizationIdOrderByCreatedAtDesc(orgId, pageable)
            .map { InvitationResponse.from(it, org.name, baseUrl) }
    }

    @Transactional
    fun cancelInvitation(orgId: UUID, invitationId: UUID, currentUserId: UUID) {
        requireAdminOrOwner(orgId, currentUserId)
        val invitation = invitationRepository.findById(invitationId)
            .orElseThrow { InvitationNotFoundException() }
        invitation.status = InvitationStatus.CANCELLED
        invitationRepository.save(invitation)
    }

    @Transactional
    fun acceptInvitation(token: String, userId: UUID, userEmail: String): MembershipResponse {
        val invitation = invitationRepository.findByToken(token)
            ?: throw InvitationNotFoundException()
        if (invitation.expiresAt.isBefore(Instant.now())) {
            invitation.status = InvitationStatus.EXPIRED
            invitationRepository.save(invitation)
            throw InvitationExpiredException()
        }
        if (invitation.status != InvitationStatus.PENDING) {
            throw InvitationAlreadyAcceptedException()
        }
        if (!invitation.email.equals(userEmail, ignoreCase = true)) {
            throw InvitationEmailMismatchException()
        }
        invitation.status = InvitationStatus.ACCEPTED
        invitationRepository.save(invitation)
        val membership = membershipRepository.save(Membership(
            userId = userId,
            organizationId = invitation.organizationId,
            role = OrganizationRole.WORKER,
            status = MembershipStatus.ACTIVE,
            invitedBy = invitation.createdBy,
        ))
        return MembershipResponse.from(membership)
    }

    @Transactional
    fun declineInvitation(token: String, currentUserId: UUID) {
        val invitation = invitationRepository.findByToken(token)
            ?: throw InvitationNotFoundException()
        if (invitation.status != InvitationStatus.PENDING) {
            throw InvitationAlreadyAcceptedException()
        }
        invitation.status = InvitationStatus.DECLINED
        invitationRepository.save(invitation)
    }

    private fun requireAdminOrOwner(orgId: UUID, userId: UUID) {
        val membership = membershipRepository.findByUserIdAndOrganizationIdAndStatus(userId, orgId, MembershipStatus.ACTIVE)
            ?: throw MembershipNotFoundException()
        if (membership.role != OrganizationRole.OWNER && membership.role != OrganizationRole.SUPER_ADMIN && membership.role != OrganizationRole.ADMIN) {
            throw RoleRequiredException("OWNER, SUPER_ADMIN, or ADMIN")
        }
    }
}
