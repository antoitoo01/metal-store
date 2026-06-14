package com.blacksmith.metalstore.organization

import com.blacksmith.metalstore.organization.domain.entity.*
import com.blacksmith.metalstore.organization.domain.repository.InvitationRepository
import com.blacksmith.metalstore.organization.domain.repository.MembershipRepository
import com.blacksmith.metalstore.organization.domain.repository.OrganizationRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InvitationControllerHttpTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var orgRepository: OrganizationRepository

    @Autowired
    private lateinit var membershipRepository: MembershipRepository

    @Autowired
    private lateinit var invitationRepository: InvitationRepository

    private val ownerId = UUID.randomUUID()
    private val newUserId = UUID.randomUUID()
    private var orgId: UUID = UUID.randomUUID()
    private var orgName: String = "Test"

    @BeforeEach
    fun setUp() {
        invitationRepository.deleteAll()
        membershipRepository.deleteAll()
        orgRepository.deleteAll()
        val org = orgRepository.save(Organization(name = orgName, slug = "test"))
        orgId = org.id
        membershipRepository.save(Membership(userId = ownerId, organizationId = orgId, role = OrganizationRole.OWNER))
    }

    @Test
    fun `create invitations batch returns created invitations`() {
        mockMvc.perform(post("/api/organizations/{orgId}/invitations", orgId)
            .with(jwt().jwt { it.subject(ownerId.toString()) })
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"emails":["a@test.com","b@test.com"]}"""))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].email").value("a@test.com"))
            .andExpect(jsonPath("$[0].status").value("PENDING"))
            .andExpect(jsonPath("$[0].organizationName").value(orgName))
            .andExpect(jsonPath("$[0].token").isString)
            .andExpect(jsonPath("$[0].link").isString)
            .andExpect(jsonPath("$[0].expiresAt").isString)
    }

    @Test
    fun `create invitations returns conflict on duplicate email`() {
        invitationRepository.save(Invitation(organizationId = orgId, email = "dup@test.com", createdBy = ownerId))

        mockMvc.perform(post("/api/organizations/{orgId}/invitations", orgId)
            .with(jwt().jwt { it.subject(ownerId.toString()) })
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"emails":["dup@test.com"]}"""))
            .andExpect(status().isConflict)
    }

    @Test
    fun `list invitations returns paginated`() {
        invitationRepository.save(Invitation(organizationId = orgId, email = "a@test.com", createdBy = ownerId))
        invitationRepository.save(Invitation(organizationId = orgId, email = "b@test.com", createdBy = ownerId))

        mockMvc.perform(get("/api/organizations/{orgId}/invitations", orgId)
            .with(jwt().jwt { it.subject(ownerId.toString()) })
            .param("page", "0")
            .param("size", "10"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page.totalElements").value(2))
            .andExpect(jsonPath("$.content[0].id").exists())
    }

    @Test
    fun `cancel invitation marks as cancelled`() {
        val inv = invitationRepository.save(Invitation(organizationId = orgId, email = "c@test.com", createdBy = ownerId))

        mockMvc.perform(delete("/api/organizations/{orgId}/invitations/{id}", orgId, inv.id)
            .with(jwt().jwt { it.subject(ownerId.toString()) }))
            .andExpect(status().isNoContent)

        val updated = invitationRepository.findById(inv.id).get()
        assert(updated.status == InvitationStatus.CANCELLED)
    }

    @Test
    fun `accept invitation creates membership`() {
        val token = UUID.randomUUID().toString()
        val email = "nuevo@test.com"
        invitationRepository.save(Invitation(token = token, organizationId = orgId, email = email, createdBy = ownerId))

        mockMvc.perform(post("/api/invitations/accept")
            .with(jwt().jwt {
                it.subject(newUserId.toString())
                it.claim("email", email)
            })
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"token":"$token"}"""))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.userId").value(newUserId.toString()))
            .andExpect(jsonPath("$.role").value("WORKER"))
    }

    @Test
    fun `decline invitation marks as declined`() {
        val token = UUID.randomUUID().toString()
        invitationRepository.save(Invitation(token = token, organizationId = orgId, email = "d@test.com", createdBy = ownerId))

        mockMvc.perform(post("/api/invitations/decline")
            .with(jwt().jwt { it.subject(newUserId.toString()) })
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"token":"$token"}"""))
            .andExpect(status().isOk)

        val updated = invitationRepository.findByToken(token)!!
        assert(updated.status == InvitationStatus.DECLINED)
    }
}
