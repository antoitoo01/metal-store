package com.blacksmith.metalstore.auth

import com.blacksmith.metalstore.auth.client.SupabaseAuthClient
import com.blacksmith.metalstore.auth.domain.entity.Role
import com.blacksmith.metalstore.auth.domain.entity.User
import com.blacksmith.metalstore.auth.domain.entity.UserState
import com.blacksmith.metalstore.auth.repository.UserRepository
import com.blacksmith.metalstore.organization.domain.entity.Organization
import com.blacksmith.metalstore.organization.domain.repository.OrganizationRepository
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
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
class AuthHttpIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var organizationRepository: OrganizationRepository

    @MockitoBean
    private lateinit var supabaseAuthClient: SupabaseAuthClient

    private val organizationId = UUID.randomUUID()
    private val userId = UUID.randomUUID()
    private val email = "http-test@example.com"

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
        organizationRepository.deleteAll()

        organizationRepository.save(Organization(id = organizationId, name = "Test HTTP", slug = "test-http"))
    }

    @Test
    fun `register with invalid request returns bad request`() {
        val invalidJson = """
            {
              "username": "test",
              "email": "not-an-email",
              "password": "short"
            }
        """.trimIndent()

        mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.detail").value("Validation failed"))
            .andExpect(jsonPath("$.errors.email").exists())
            .andExpect(jsonPath("$.errors.password").exists())
    }

    @Test
    fun `me returns unauthorized when JWT is missing`() {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `me returns user details when JWT is valid`() {
        val user = User(
            id = userId,
            tenantId = organizationId,
            username = "httptest",
            email = email,
            role = Role.USER,
            status = UserState.ACTIVE
        )
        userRepository.save(user)

        mockMvc.perform(get("/api/auth/me")
            .with(jwt().jwt { it.subject(userId.toString()) }))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(userId.toString()))
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.username").value("httptest"))
            .andExpect(jsonPath("$.organizationId").value(organizationId.toString()))
            .andExpect(jsonPath("$.organizationName").value("Test HTTP"))
    }

    @Test
    fun `get user by non-existent id returns 404`() {
        val nonExistentId = UUID.randomUUID()
        mockMvc.perform(get("/api/users/$nonExistentId"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.title").value("Not Found"))
            .andExpect(jsonPath("$.detail").value(containsString("Usuario con id $nonExistentId no encontrado")))
    }

    @Test
    fun `update user with invalid email returns bad request`() {
        val invalidUpdateJson = """
            {
              "id": "$userId",
              "username": "validusername",
              "email": "invalid-email"
            }
        """.trimIndent()

        mockMvc.perform(put("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidUpdateJson))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.detail").value("Validation failed"))
            .andExpect(jsonPath("$.errors.email").exists())
    }
}
