package com.blacksmith.metalstore.organization

import com.blacksmith.metalstore.organization.application.InvitationService
import com.blacksmith.metalstore.organization.domain.entity.*
import com.blacksmith.metalstore.organization.domain.repository.InvitationRepository
import com.blacksmith.metalstore.organization.domain.repository.MembershipRepository
import com.blacksmith.metalstore.organization.domain.repository.OrganizationRepository
import com.blacksmith.metalstore.organization.exception.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.time.Instant
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
class InvitationServiceTest {

    @Autowired
    private lateinit var orgRepository: OrganizationRepository

    @Autowired
    private lateinit var membershipRepository: MembershipRepository

    @Autowired
    private lateinit var invitationRepository: InvitationRepository

    private lateinit var service: InvitationService
    private val ownerId = UUID.randomUUID()
    private val workerId = UUID.randomUUID()
    private var orgId: UUID = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        invitationRepository.deleteAll()
        membershipRepository.deleteAll()
        orgRepository.deleteAll()
        val org = orgRepository.save(Organization(name = "Test", slug = "test"))
        orgId = org.id
        membershipRepository.save(Membership(userId = ownerId, organizationId = orgId, role = OrganizationRole.ORGANIZATION_OWNER))
        service = InvitationService(orgRepository, membershipRepository, invitationRepository, "http://localhost:4200")
    }

    @Test
    fun `createInvitations creates multiple invitations`() {
        val result = service.createInvitations(orgId, listOf("a@test.com", "b@test.com"), ownerId)

        assert(result.size == 2)
        assert(result.all { it.status == InvitationStatus.PENDING })
        assert(result.all { it.organizationName == "Test" })
        assert(result.all { it.link.startsWith("http://localhost:4200") })
    }

    @Test
    fun `createInvitations throws on duplicate`() {
        invitationRepository.save(Invitation(organizationId = orgId, email = "dup@test.com", createdBy = ownerId))

        try {
            service.createInvitations(orgId, listOf("dup@test.com"), ownerId)
            assert(false) { "Expected DuplicateInvitationException" }
        } catch (e: DuplicateInvitationException) {
            // expected
        }
    }

    @Test
    fun `createInvitations throws when user is not admin`() {
        membershipRepository.save(Membership(userId = workerId, organizationId = orgId, role = OrganizationRole.USER))

        try {
            service.createInvitations(orgId, listOf("w@test.com"), workerId)
            assert(false) { "Expected RoleRequiredException" }
        } catch (e: RoleRequiredException) {
            // expected
        }
    }

    @Test
    fun `listInvitations returns paginated results`() {
        invitationRepository.save(Invitation(organizationId = orgId, email = "a@test.com", createdBy = ownerId))
        invitationRepository.save(Invitation(organizationId = orgId, email = "b@test.com", createdBy = ownerId))

        val page = service.listInvitations(orgId, PageRequest.of(0, 10), ownerId)

        assert(page.totalElements == 2L)
        assert(page.content.size == 2)
    }

    @Test
    fun `cancelInvitation marks as cancelled`() {
        val inv = invitationRepository.save(Invitation(organizationId = orgId, email = "c@test.com", createdBy = ownerId))

        service.cancelInvitation(orgId, inv.id, ownerId)

        val updated = invitationRepository.findById(inv.id).get()
        assert(updated.status == InvitationStatus.CANCELLED)
    }

    @Test
    fun `acceptInvitation creates membership as VIEWER`() {
        val token = UUID.randomUUID().toString()
        val email = "nuevo@test.com"
        invitationRepository.save(Invitation(token = token, organizationId = orgId, email = email, createdBy = ownerId))

        val membership = service.acceptInvitation(token, workerId, email)

        assert(membership.userId == workerId)
        assert(membership.role == OrganizationRole.USER)

        val updatedInv = invitationRepository.findByToken(token)!!
        assert(updatedInv.status == InvitationStatus.ACCEPTED)
    }

    @Test
    fun `acceptInvitation throws when token not found`() {
        try {
            service.acceptInvitation(UUID.randomUUID().toString(), workerId, "x@test.com")
            assert(false) { "Expected InvitationNotFoundException" }
        } catch (e: InvitationNotFoundException) {
            // expected
        }
    }

    @Test
    fun `acceptInvitation throws when expired`() {
        val token = UUID.randomUUID().toString()
        invitationRepository.save(Invitation(
            token = token,
            organizationId = orgId,
            email = "expired@test.com",
            createdBy = ownerId,
            expiresAt = Instant.now().minusSeconds(3600),
        ))

        try {
            service.acceptInvitation(token, workerId, "expired@test.com")
            assert(false) { "Expected InvitationExpiredException" }
        } catch (e: InvitationExpiredException) {
            // expected
        }
    }

    @Test
    fun `acceptInvitation throws when email mismatches`() {
        val token = UUID.randomUUID().toString()
        invitationRepository.save(Invitation(token = token, organizationId = orgId, email = "real@test.com", createdBy = ownerId))

        try {
            service.acceptInvitation(token, workerId, "other@test.com")
            assert(false) { "Expected InvitationEmailMismatchException" }
        } catch (e: InvitationEmailMismatchException) {
            // expected
        }
    }

    @Test
    fun `acceptInvitation throws when already accepted`() {
        val token = UUID.randomUUID().toString()
        val email = "aceptado@test.com"
        invitationRepository.save(Invitation(token = token, organizationId = orgId, email = email, createdBy = ownerId))
        service.acceptInvitation(token, workerId, email)

        try {
            service.acceptInvitation(token, UUID.randomUUID(), email)
            assert(false) { "Expected InvitationAlreadyAcceptedException" }
        } catch (e: InvitationAlreadyAcceptedException) {
            // expected
        }
    }

    @Test
    fun `declineInvitation marks as declined`() {
        val token = UUID.randomUUID().toString()
        invitationRepository.save(Invitation(token = token, organizationId = orgId, email = "d@test.com", createdBy = ownerId))

        service.declineInvitation(token, workerId)

        val updated = invitationRepository.findByToken(token)!!
        assert(updated.status == InvitationStatus.DECLINED)
    }
}
