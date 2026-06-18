package com.blacksmith.metalstore.organization

import com.blacksmith.metalstore.organization.domain.entity.*
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
class MembershipControllerHttpTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var orgRepository: OrganizationRepository

    @Autowired
    private lateinit var membershipRepository: MembershipRepository

    private val ownerId = UUID.randomUUID()
    private val memberId = UUID.randomUUID()
    private var orgId: UUID = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        membershipRepository.deleteAll()
        orgRepository.deleteAll()
        val org = orgRepository.save(Organization(name = "Test", slug = "test"))
        orgId = org.id
        membershipRepository.save(Membership(userId = ownerId, organizationId = orgId, role = OrganizationRole.OWNER))
        membershipRepository.save(Membership(userId = memberId, organizationId = orgId, role = OrganizationRole.VIEWER))
    }

    @Test
    fun `list returns all members`() {
        mockMvc.perform(get("/api/organizations/{orgId}/members", orgId)
            .with(jwt().jwt { it.subject(ownerId.toString()) }))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    fun `me returns current user membership`() {
        mockMvc.perform(get("/api/organizations/{orgId}/members/me", orgId)
            .with(jwt().jwt { it.subject(ownerId.toString()) }))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.userId").value(ownerId.toString()))
            .andExpect(jsonPath("$.role").value("OWNER"))
    }

    @Test
    fun `updateRole changes member role`() {
        mockMvc.perform(put("/api/organizations/{orgId}/members/{userId}/role", orgId, memberId)
            .with(jwt().jwt { it.subject(ownerId.toString()) })
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"role":"EDITOR"}"""))
            .andExpect(status().isOk)

        val membership = membershipRepository.findByUserIdAndOrganizationIdAndStatus(memberId, orgId, MembershipStatus.ACTIVE)
        assert(membership!!.role == OrganizationRole.EDITOR)
    }

    @Test
    fun `remove deletes member`() {
        mockMvc.perform(delete("/api/organizations/{orgId}/members/{userId}", orgId, memberId)
            .with(jwt().jwt { it.subject(ownerId.toString()) }))
            .andExpect(status().isNoContent)

        val membership = membershipRepository.findByUserIdAndOrganizationIdAndStatus(memberId, orgId, MembershipStatus.ACTIVE)
        assert(membership == null)
    }
}