package com.blacksmith.metalstore.client

import com.blacksmith.metalstore.client.domain.entity.Client
import com.blacksmith.metalstore.client.domain.repository.ClientRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClientControllerHttpTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var repo: ClientRepository

    private val organizationId = UUID.randomUUID()
    private val otherOrganizationId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        repo.deleteAll()
    }

    @Test
    fun `list returns paginated clients for organization`() {
        repo.save(Client(organizationId = organizationId, name = "Cliente A"))
        repo.save(Client(organizationId = organizationId, name = "Cliente B"))
        repo.save(Client(organizationId = otherOrganizationId, name = "Otro Cliente"))

        mockMvc.perform(get("/api/clients")
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page.totalElements").value(2))
            .andExpect(jsonPath("$.content.length()").value(2))
    }

    @Test
    fun `list filters by query param`() {
        repo.save(Client(organizationId = organizationId, name = "Taller PÃ©rez"))
        repo.save(Client(organizationId = organizationId, name = "HerrerÃ­a GarcÃ­a"))

        mockMvc.perform(get("/api/clients")
            .param("q", "PÃ©rez")
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page.totalElements").value(1))
    }

    @Test
    fun `create stores client with organization`() {
        val body = """{"name":"Taller LÃ³pez","email":"lopez@test.com","phone":"123456789"}"""

        mockMvc.perform(post("/api/clients")
            .header("X-Organization-Id", organizationId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Taller LÃ³pez"))
            .andExpect(jsonPath("$.email").value("lopez@test.com"))
            .andExpect(jsonPath("$.organizationId").value(organizationId.toString()))
    }

    @Test
    fun `get returns client by id`() {
        val client = repo.save(Client(organizationId = organizationId, name = "Test"))

        mockMvc.perform(get("/api/clients/{id}", client.id)
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(client.id.toString()))
    }

    @Test
    fun `get returns 404 for non-existent id`() {
        mockMvc.perform(get("/api/clients/{id}", UUID.randomUUID())
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `delete removes client`() {
        val client = repo.save(Client(organizationId = organizationId, name = "Test"))

        mockMvc.perform(delete("/api/clients/{id}", client.id)
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isNoContent)

        assert(repo.findById(client.id).isEmpty)
    }

    @Test
    fun `update modifies client`() {
        val client = repo.save(Client(organizationId = organizationId, name = "Original"))
        val body = """{"name":"Updated"}"""

        mockMvc.perform(put("/api/clients/{id}", client.id)
            .header("X-Organization-Id", organizationId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Updated"))
    }

    @Test
    fun `activate toggles client status`() {
        val client = repo.save(Client(organizationId = organizationId, name = "Test"))

        mockMvc.perform(post("/api/clients/{id}/deactivate", client.id)
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("INACTIVE"))

        mockMvc.perform(post("/api/clients/{id}/activate", client.id)
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("ACTIVE"))
    }
}