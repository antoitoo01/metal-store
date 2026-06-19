package com.blacksmith.metalstore.organization

import com.blacksmith.metalstore.organization.domain.entity.MembershipStatus
import com.blacksmith.metalstore.organization.domain.entity.OrganizationRole
import com.blacksmith.metalstore.organization.domain.entity.Membership
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
class OrganizationControllerHttpTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var orgRepository: OrganizationRepository

    @Autowired
    private lateinit var membershipRepository: MembershipRepository

    private val ownerId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        membershipRepository.deleteAll()
        orgRepository.deleteAll()
    }

    @Test
    fun `list returns organizations for user`() {
        val org = orgRepository.save(com.blacksmith.metalstore.organization.domain.entity.Organization(name = "Mi Taller", slug = "mi-taller"))
        membershipRepository.save(Membership(userId = ownerId, organizationId = org.id, role = OrganizationRole.ORGANIZATION_OWNER))

        mockMvc.perform(get("/api/organizations")
            .with(jwt().jwt { it.subject(ownerId.toString()) }))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Mi Taller"))
    }

    @Test
    fun `create stores organization`() {
        mockMvc.perform(post("/api/organizations")
            .with(jwt().jwt { it.subject(ownerId.toString()) })
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"Nueva Org"}"""))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Nueva Org"))
            .andExpect(jsonPath("$.slug").value("nueva-org"))
            .andExpect(jsonPath("$.memberCount").value(1))
    }

    @Test
    fun `get by id returns organization`() {
        val org = orgRepository.save(com.blacksmith.metalstore.organization.domain.entity.Organization(name = "Test", slug = "test"))

        mockMvc.perform(get("/api/organizations/{id}", org.id)
            .with(jwt().jwt { it.subject(ownerId.toString()) }))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(org.id.toString()))
            .andExpect(jsonPath("$.name").value("Test"))
    }

    @Test
    fun `get by id returns 404 for non-existent`() {
        mockMvc.perform(get("/api/organizations/{id}", UUID.randomUUID())
            .with(jwt().jwt { it.subject(ownerId.toString()) }))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `update modifies organization`() {
        val org = orgRepository.save(com.blacksmith.metalstore.organization.domain.entity.Organization(name = "Original", slug = "original"))
        membershipRepository.save(Membership(userId = ownerId, organizationId = org.id, role = OrganizationRole.ORGANIZATION_OWNER))

        mockMvc.perform(put("/api/organizations/{id}", org.id)
            .with(jwt().jwt { it.subject(ownerId.toString()) })
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"Updated"}"""))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Updated"))
    }
}