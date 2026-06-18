package com.blacksmith.metalstore.organization

import com.blacksmith.metalstore.organization.application.OrganizationService
import com.blacksmith.metalstore.organization.domain.dto.request.CreateOrganizationRequest
import com.blacksmith.metalstore.organization.domain.dto.request.UpdateOrganizationRequest
import com.blacksmith.metalstore.organization.domain.dto.request.UpdateRoleRequest
import com.blacksmith.metalstore.organization.domain.entity.*
import com.blacksmith.metalstore.organization.domain.repository.MembershipRepository
import com.blacksmith.metalstore.organization.domain.repository.OrganizationRepository
import com.blacksmith.metalstore.organization.exception.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
class OrganizationServiceTest {

    @Autowired
    private lateinit var orgRepository: OrganizationRepository

    @Autowired
    private lateinit var membershipRepository: MembershipRepository

    private lateinit var service: OrganizationService
    private val ownerId = UUID.randomUUID()
    private val adminId = UUID.randomUUID()
    private val workerId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        membershipRepository.deleteAll()
        orgRepository.deleteAll()
        service = OrganizationService(orgRepository, membershipRepository)
    }

    @Test
    fun `create organization creates org and owner membership`() {
        val response = service.createOrganization(ownerId, CreateOrganizationRequest("Mi Taller"))

        assert(response.name == "Mi Taller")
        assert(response.slug == "mi-taller")
        assert(response.memberCount == 1)

        val saved = orgRepository.findById(response.id)
        assert(saved.isPresent)

        val membership = membershipRepository.findByUserIdAndOrganizationIdAndStatus(ownerId, response.id, MembershipStatus.ACTIVE)
        assert(membership != null)
        assert(membership!!.role == OrganizationRole.OWNER)
    }

    @Test
    fun `create organization generates unique slug`() {
        service.createOrganization(ownerId, CreateOrganizationRequest("Test"))
        val second = service.createOrganization(ownerId, CreateOrganizationRequest("Test"))

        assert(second.slug == "test-1")
    }

    @Test
    fun `findOrganization returns organization`() {
        val org = service.createOrganization(ownerId, CreateOrganizationRequest("Mi Taller"))
        val found = service.findOrganization(org.id)

        assert(found.id == org.id)
        assert(found.name == "Mi Taller")
    }

    @Test
    fun `findOrganization throws when not found`() {
        try {
            service.findOrganization(UUID.randomUUID())
            assert(false) { "Expected OrganizationNotFoundException" }
        } catch (e: OrganizationNotFoundException) {
            // expected
        }
    }

    @Test
    fun `findOrganizationsByUserId returns orgs for user`() {
        val org1 = service.createOrganization(ownerId, CreateOrganizationRequest("Taller A"))
        val org2 = service.createOrganization(ownerId, CreateOrganizationRequest("Taller B"))

        val userOrgs = service.findOrganizationsByUserId(ownerId)

        assert(userOrgs.size == 2)
        assert(userOrgs.any { it.id == org1.id })
        assert(userOrgs.any { it.id == org2.id })
    }

    @Test
    fun `findOrganizationsByUserId returns empty for unknown user`() {
        val result = service.findOrganizationsByUserId(UUID.randomUUID())
        assert(result.isEmpty())
    }

    @Test
    fun `updateOrganization changes name`() {
        val org = service.createOrganization(ownerId, CreateOrganizationRequest("Original"))
        val updated = service.updateOrganization(org.id, UpdateOrganizationRequest("Renamed"), ownerId)

        assert(updated.name == "Renamed")
        val saved = orgRepository.findById(org.id)
        assert(saved.get().name == "Renamed")
    }

    @Test
    fun `updateOrganization throws when not member`() {
        val org = service.createOrganization(ownerId, CreateOrganizationRequest("Test"))
        try {
            service.updateOrganization(org.id, UpdateOrganizationRequest("Nope"), UUID.randomUUID())
            assert(false) { "Expected MembershipNotFoundException" }
        } catch (e: MembershipNotFoundException) {
            // expected
        }
    }

    @Test
    fun `getMembers returns all members`() {
        val org = service.createOrganization(ownerId, CreateOrganizationRequest("Test"))
        membershipRepository.save(Membership(userId = adminId, organizationId = org.id, role = OrganizationRole.EDITOR))

        val members = service.getMembers(org.id)

        assert(members.size == 2)
        assert(members.any { it.userId == ownerId && it.role == OrganizationRole.OWNER })
        assert(members.any { it.userId == adminId && it.role == OrganizationRole.EDITOR })
    }

    @Test
    fun `getMyMembership returns current user membership`() {
        val org = service.createOrganization(ownerId, CreateOrganizationRequest("Test"))

        val myMembership = service.getMyMembership(org.id, ownerId)
        assert(myMembership.userId == ownerId)
        assert(myMembership.role == OrganizationRole.OWNER)
    }

    @Test
    fun `getMyMembership throws when not member`() {
        val org = service.createOrganization(ownerId, CreateOrganizationRequest("Test"))
        try {
            service.getMyMembership(org.id, UUID.randomUUID())
            assert(false) { "Expected MembershipNotFoundException" }
        } catch (e: MembershipNotFoundException) {
            // expected
        }
    }

    @Test
    fun `updateMemberRole changes role`() {
        val org = service.createOrganization(ownerId, CreateOrganizationRequest("Test"))
        membershipRepository.save(Membership(userId = adminId, organizationId = org.id, role = OrganizationRole.EDITOR))

        service.updateMemberRole(org.id, adminId, OrganizationRole.VIEWER, ownerId)

        val updated = membershipRepository.findByUserIdAndOrganizationIdAndStatus(adminId, org.id, MembershipStatus.ACTIVE)
        assert(updated!!.role == OrganizationRole.VIEWER)
    }

    @Test
    fun `updateMemberRole throws when not admin or owner`() {
        val org = service.createOrganization(ownerId, CreateOrganizationRequest("Test"))
        membershipRepository.save(Membership(userId = workerId, organizationId = org.id, role = OrganizationRole.VIEWER))

        try {
            service.updateMemberRole(org.id, adminId, OrganizationRole.VIEWER, workerId)
            assert(false) { "Expected RoleRequiredException" }
        } catch (e: RoleRequiredException) {
            // expected
        }
    }

    @Test
    fun `removeMember removes membership`() {
        val org = service.createOrganization(ownerId, CreateOrganizationRequest("Test"))
        membershipRepository.save(Membership(userId = adminId, organizationId = org.id, role = OrganizationRole.EDITOR))

        service.removeMember(org.id, adminId, ownerId)

        val membership = membershipRepository.findByUserIdAndOrganizationIdAndStatus(adminId, org.id, MembershipStatus.ACTIVE)
        assert(membership == null)
    }

    @Test
    fun `removeMember throws when removing last owner`() {
        val org = service.createOrganization(ownerId, CreateOrganizationRequest("Test"))
        try {
            service.removeMember(org.id, ownerId, ownerId)
            assert(false) { "Expected CannotRemoveLastOwnerException" }
        } catch (e: CannotRemoveLastOwnerException) {
            // expected
        }
    }

    @Test
    fun `findMembership returns active membership`() {
        val org = service.createOrganization(ownerId, CreateOrganizationRequest("Test"))

        val membership = service.findMembership(ownerId, org.id)

        assert(membership != null)
        assert(membership!!.role == OrganizationRole.OWNER)
    }

    @Test
    fun `findMembership returns null when not a member`() {
        val org = service.createOrganization(ownerId, CreateOrganizationRequest("Test"))

        val membership = service.findMembership(UUID.randomUUID(), org.id)

        assert(membership == null)
    }
}