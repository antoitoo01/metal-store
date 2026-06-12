package com.blacksmith.metalstore.auth

import com.blacksmith.metalstore.auth.audit.AuditLogger
import com.blacksmith.metalstore.auth.client.SupabaseAuthClient
import com.blacksmith.metalstore.auth.domain.dto.request.RegisterRequest
import com.blacksmith.metalstore.auth.domain.entity.Role
import com.blacksmith.metalstore.auth.domain.entity.UserState
import com.blacksmith.metalstore.auth.repository.UserRepository
import com.blacksmith.metalstore.auth.service.AuthService
import com.blacksmith.metalstore.organization.application.OrganizationService
import com.blacksmith.metalstore.organization.domain.dto.request.CreateOrganizationRequest
import com.blacksmith.metalstore.organization.domain.dto.response.OrganizationResponse
import com.blacksmith.metalstore.organization.domain.entity.Organization
import com.blacksmith.metalstore.organization.domain.repository.OrganizationRepository
import com.blacksmith.metalstore.shared.exception.ApiException
import com.blacksmith.metalstore.shared.exception.ErrorCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var organizationRepository: OrganizationRepository

    private lateinit var supabaseAuthClient: SupabaseAuthClient
    private lateinit var organizationService: OrganizationService
    private lateinit var authService: AuthService

    private val organizationId = UUID.randomUUID()
    private val userUuid = UUID.randomUUID()
    private val email = "test@example.com"
    private val password = "Test1234!"
    private val accessToken = "test-access-token"
    private val refreshToken = "test-refresh-token"
    private val organizationName = "Test Taller"

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
        organizationRepository.deleteAll()
        organizationRepository.save(Organization(id = userUuid, name = organizationName, slug = organizationName.lowercase().replace(" ", "-")))
        supabaseAuthClient = mock(SupabaseAuthClient::class.java)
        organizationService = mock(OrganizationService::class.java)
        `when`(organizationService.createOrganization(any(), any()))
            .thenReturn(OrganizationResponse(id = userUuid, name = organizationName, slug = organizationName.lowercase().replace(" ", "-")))
        authService = AuthService(supabaseAuthClient, userRepository, organizationRepository, organizationService, mock(AuditLogger::class.java))
    }

    @Test
    fun `register creates user and returns tokens`() {
        `when`(supabaseAuthClient.signUp(any<String>(), any<String>(), any<String>())).thenReturn(
            mapOf("id" to userUuid.toString(), "email" to email)
        )
        `when`(supabaseAuthClient.signIn(any<String>(), any<String>())).thenReturn(
            mapOf(
                "access_token" to accessToken,
                "refresh_token" to refreshToken,
                "expires_in" to 3600,
                "user" to mapOf("id" to userUuid.toString(), "email" to email)
            )
        )

        val response = authService.register(
            RegisterRequest(username = "testuser", organizationName = organizationName, email = email, password = password)
        )

        assert(response.accessToken == accessToken)
        assert(response.refreshToken == refreshToken)
        assert(response.email == email)
        assert(response.role == Role.ORGANIZATION_OWNER)
        assert(response.organizationName == organizationName)

        val savedUser = userRepository.findById(userUuid)
        assert(savedUser.isPresent)
        assert(savedUser.get().email == email)
        assert(savedUser.get().role == Role.ORGANIZATION_OWNER)
        assert(savedUser.get().organizationId == response.organizationId)
    }

    @Test
    fun `login returns tokens for existing user`() {
        `when`(supabaseAuthClient.signUp(any<String>(), any<String>(), any<String>())).thenReturn(
            mapOf("id" to userUuid.toString(), "email" to email)
        )
        `when`(supabaseAuthClient.signIn(any<String>(), any<String>())).thenReturn(
            mapOf(
                "access_token" to accessToken,
                "refresh_token" to refreshToken,
                "expires_in" to 3600,
                "user" to mapOf("id" to userUuid.toString(), "email" to email)
            )
        )

        authService.register(
            RegisterRequest(username = "testuser", organizationName = organizationName, email = email, password = password)
        )

        val response = authService.login(email, password)

        assert(response.accessToken == accessToken)
        assert(response.email == email)
    }

    @Test
    fun `me returns user info for valid id`() {
        `when`(supabaseAuthClient.signUp(any<String>(), any<String>(), any<String>())).thenReturn(
            mapOf("id" to userUuid.toString(), "email" to email)
        )
        `when`(supabaseAuthClient.signIn(any<String>(), any<String>())).thenReturn(
            mapOf(
                "access_token" to accessToken,
                "refresh_token" to refreshToken,
                "expires_in" to 3600,
                "user" to mapOf("id" to userUuid.toString(), "email" to email)
            )
        )

        authService.register(
            RegisterRequest(username = "testuser", organizationName = organizationName, email = email, password = password)
        )

        val response = authService.me(userUuid)

        assert(response.email == email)
        assert(response.username == "testuser")
        assert(response.organizationName == organizationName)
    }

    @Test
    fun `refresh returns new tokens`() {
        `when`(supabaseAuthClient.refreshToken(any<String>())).thenReturn(
            mapOf(
                "access_token" to "new-$accessToken",
                "refresh_token" to "new-$refreshToken",
                "expires_in" to 3600,
                "user" to mapOf("id" to userUuid.toString(), "email" to email)
            )
        )

        val response = authService.refresh(refreshToken)

        assert(response.accessToken == "new-$accessToken")
    }

    @Test
    fun `logout calls supabase signout`() {
        authService.logout(accessToken)

        verify(supabaseAuthClient).signOut(accessToken)
    }

    @Test
    fun `login throws INVALID_CREDENTIALS when credentials are wrong`() {
        `when`(supabaseAuthClient.signIn(any<String>(), any<String>()))
            .thenThrow(ApiException(ErrorCode.INVALID_CREDENTIALS, "Invalid login credentials"))

        try {
            authService.login("wrong@test.com", "wrong")
            assert(false) { "Expected ApiException" }
        } catch (e: ApiException) {
            assert(e.errorCode == ErrorCode.INVALID_CREDENTIALS)
        }
    }

    @Test
    fun `login throws SERVICE_UNAVAILABLE when Supabase is down`() {
        `when`(supabaseAuthClient.signIn(any<String>(), any<String>()))
            .thenThrow(ApiException(ErrorCode.SERVICE_UNAVAILABLE, "Authentication service is unavailable"))

        try {
            authService.login("test@test.com", "pass")
            assert(false) { "Expected ApiException" }
        } catch (e: ApiException) {
            assert(e.errorCode == ErrorCode.SERVICE_UNAVAILABLE)
        }
    }
}
