package com.blacksmith.metalstore.organization

import com.blacksmith.metalstore.organization.domain.entity.*
import com.blacksmith.metalstore.organization.domain.repository.InvitationRepository
import com.blacksmith.metalstore.organization.domain.repository.MembershipRepository
import com.blacksmith.metalstore.organization.domain.repository.OrganizationRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
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

    @BeforeEach
    fun setUp() {
        invitationRepository.deleteAll()
        membershipRepository.deleteAll()
        orgRepository.deleteAll()
        val org = orgRepository.save(Organization(name = "Test", slug = "test"))
        orgId = org.id
        membershipRepository.save(Membership(userId = ownerId, organizationId = orgId, role = OrganizationRole.OWNER))
    }

    @Test
    fun `create invitation returns pending invitation`() {
        mockMvc.perform(post("/api/organizations/{orgId}/invitations", orgId)
            .with(jwt().jwt { it.subject(ownerId.toString()) })
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"email":"invitado@test.com","role":"WORKER"}"""))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.email").value("invitado@test.com"))
            .andExpect(jsonPath("$.status").value("PENDING"))
    }

    @Test
    fun `list invitations returns pending`() {
        invitationRepository.save(Invitation(token = UUID.randomUUID(), organizationId = orgId, role = OrganizationRole.WORKER, email = "a@test.com", createdBy = ownerId))
        invitationRepository.save(Invitation(token = UUID.randomUUID(), organizationId = orgId, role = OrganizationRole.ADMIN, email = "b@test.com", createdBy = ownerId))

        mockMvc.perform(get("/api/organizations/{orgId}/invitations", orgId)
            .with(jwt().jwt { it.subject(ownerId.toString()) }))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    fun `accept invitation creates membership`() {
        val token = UUID.randomUUID()
        invitationRepository.save(Invitation(token = token, organizationId = orgId, role = OrganizationRole.WORKER, email = "nuevo@test.com", createdBy = ownerId))

        mockMvc.perform(post("/api/invitations/{token}/accept", token)
            .with(jwt().jwt { it.subject(newUserId.toString()) }))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.userId").value(newUserId.toString()))
            .andExpect(jsonPath("$.role").value("WORKER"))
    }
}