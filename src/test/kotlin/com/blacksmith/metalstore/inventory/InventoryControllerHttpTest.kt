package com.blacksmith.metalstore.inventory

import com.blacksmith.metalstore.inventory.domain.entity.InventoryItem
import com.blacksmith.metalstore.inventory.domain.repository.InventoryItemRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InventoryControllerHttpTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var repo: InventoryItemRepository

    private val organizationId = UUID.randomUUID()
    private val otherOrganizationId = UUID.randomUUID()
    private val profileId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        repo.deleteAll()
    }

    @Test
    fun `list returns paginated items for organization`() {
        repo.save(InventoryItem(organizationId = organizationId, profileId = profileId, quantity = BigDecimal.ONE))
        repo.save(InventoryItem(organizationId = organizationId, profileId = profileId, quantity = BigDecimal.TEN))
        repo.save(InventoryItem(organizationId = otherOrganizationId, profileId = profileId, quantity = BigDecimal.ONE))

        mockMvc.perform(get("/api/inventory")
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page.totalElements").value(2))
            .andExpect(jsonPath("$.content.length()").value(2))
    }

    @Test
    fun `create stores item with organization`() {
        val body = """{"organizationId":"$organizationId","profileId":"$profileId","quantity":150.00,"location":"Estante A1"}"""

        mockMvc.perform(post("/api/inventory")
            .header("X-Organization-Id", organizationId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.organizationId").value(organizationId.toString()))
            .andExpect(jsonPath("$.quantity").value(150.00))
    }

    @Test
    fun `get returns item by id`() {
        val item = repo.save(InventoryItem(organizationId = organizationId, profileId = profileId, quantity = BigDecimal.ONE))

        mockMvc.perform(get("/api/inventory/{id}", item.id)
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(item.id.toString()))
    }

    @Test
    fun `get returns 404 for non-existent id`() {
        mockMvc.perform(get("/api/inventory/{id}", UUID.randomUUID())
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `delete removes item`() {
        val item = repo.save(InventoryItem(organizationId = organizationId, profileId = profileId, quantity = BigDecimal.ONE))

        mockMvc.perform(delete("/api/inventory/{id}", item.id)
            .header("X-Organization-Id", organizationId.toString()))
            .andExpect(status().isNoContent)

        assert(repo.findById(item.id).isEmpty)
    }

    @Test
    fun `update modifies item`() {
        val item = repo.save(InventoryItem(organizationId = organizationId, profileId = profileId, quantity = BigDecimal.ONE))
        val body = """{"organizationId":"$organizationId","quantity":99.00}"""

        mockMvc.perform(put("/api/inventory/{id}", item.id)
            .header("X-Organization-Id", organizationId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.quantity").value(99.00))
    }
}