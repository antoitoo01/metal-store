package com.blacksmith.metalstore.catalog

import com.blacksmith.metalstore.auth.domain.entity.Role
import com.blacksmith.metalstore.auth.domain.entity.User
import com.blacksmith.metalstore.auth.domain.entity.UserState
import com.blacksmith.metalstore.auth.repository.UserRepository
import com.blacksmith.metalstore.catalog.domain.entity.CatalogItemType
import com.blacksmith.metalstore.catalog.domain.repository.CatalogItemTypeRepository
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
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogItemTypeControllerHttpTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var repo: CatalogItemTypeRepository

    @Autowired
    private lateinit var userRepo: UserRepository

    private val tenantId = UUID.randomUUID()
    private val otherTenantId = UUID.randomUUID()
    private val userId = UUID.randomUUID()
    private val header = "X-Tenant-Id"

    @BeforeEach
    fun setUp() {
        repo.deleteAll()
        userRepo.deleteAll()
    }

    @Test
    fun `create item type`() {
        val body = """{"tenantId":"$tenantId","name":"Custom Bracket","description":"Soportes personalizados","schemaDefinition":"{\"fields\":[{\"name\":\"width\",\"type\":\"decimal\"}]}"}"""

        mockMvc.perform(post("/api/catalog/item-types")
            .header(header, tenantId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Custom Bracket"))
            .andExpect(jsonPath("$.tenantId").value(tenantId.toString()))
    }

    @Test
    fun `list returns paginated types for tenant`() {
        repo.save(CatalogItemType(tenantId = tenantId, name = "Bracket"))
        repo.save(CatalogItemType(tenantId = tenantId, name = "Panel"))
        repo.save(CatalogItemType(tenantId = otherTenantId, name = "Other"))

        mockMvc.perform(get("/api/catalog/item-types")
            .header(header, tenantId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page.totalElements").value(2))
    }

    @Test
    fun `get by id returns type`() {
        val t = repo.save(CatalogItemType(tenantId = tenantId, name = "Glass Panel"))

        mockMvc.perform(get("/api/catalog/item-types/{id}", t.id)
            .header(header, tenantId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Glass Panel"))
    }

    @Test
    fun `get by id returns 404 for cross-tenant`() {
        val t = repo.save(CatalogItemType(tenantId = otherTenantId, name = "Hidden"))

        mockMvc.perform(get("/api/catalog/item-types/{id}", t.id)
            .header(header, tenantId.toString()))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `update item type`() {
        val t = repo.save(CatalogItemType(tenantId = tenantId, name = "Old Name"))
        val body = """{"tenantId":"$tenantId","name":"New Name"}"""

        mockMvc.perform(put("/api/catalog/item-types/{id}", t.id)
            .header(header, tenantId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("New Name"))
    }

    @Test
    fun `delete removes type`() {
        val t = repo.save(CatalogItemType(tenantId = tenantId, name = "Temporal"))

        mockMvc.perform(delete("/api/catalog/item-types/{id}", t.id)
            .header(header, tenantId.toString()))
            .andExpect(status().isNoContent)

        assert(repo.findById(t.id).isEmpty)
    }

    @Test
    fun `delete returns 404 for wrong tenant`() {
        val t = repo.save(CatalogItemType(tenantId = otherTenantId, name = "Not Mine"))

        mockMvc.perform(delete("/api/catalog/item-types/{id}", t.id)
            .header(header, tenantId.toString()))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `tenant resolved from JWT when auth present`() {
        userRepo.save(User(
            id = userId, tenantId = tenantId, username = "jwtuser",
            email = "jwt@test.com", role = Role.USER, status = UserState.ACTIVE,
            createdDate = LocalDateTime.now(), lastModifiedDate = LocalDateTime.now()
        ))
        repo.save(CatalogItemType(tenantId = tenantId, name = "JWT Type"))

        mockMvc.perform(get("/api/catalog/item-types")
            .with(jwt().jwt { it.subject(userId.toString()) }))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page.totalElements").value(1))
    }
}
