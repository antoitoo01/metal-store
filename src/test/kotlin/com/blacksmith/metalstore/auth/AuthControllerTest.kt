package com.blacksmith.metalstore.auth

import com.blacksmith.metalstore.auth.client.SupabaseAuthClient
import com.blacksmith.metalstore.auth.domain.dto.request.RegisterRequest
import com.blacksmith.metalstore.auth.domain.entity.Role
import com.blacksmith.metalstore.auth.domain.entity.UserState
import com.blacksmith.metalstore.auth.repository.UserRepository
import com.blacksmith.metalstore.auth.service.AuthService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var supabaseAuthClient: SupabaseAuthClient
    private lateinit var authService: AuthService

    private val userUuid = UUID.randomUUID()
    private val email = "test@example.com"
    private val password = "Test1234!"
    private val accessToken = "test-access-token"
    private val refreshToken = "test-refresh-token"

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
        supabaseAuthClient = mock(SupabaseAuthClient::class.java)
        authService = AuthService(supabaseAuthClient, userRepository)
    }

    @Test
    fun `register creates user and returns tokens`() {
        `when`(supabaseAuthClient.signUp(anyString(), anyString(), anyString())).thenReturn(
            mapOf("id" to userUuid.toString(), "email" to email)
        )
        `when`(supabaseAuthClient.signIn(anyString(), anyString())).thenReturn(
            mapOf(
                "access_token" to accessToken,
                "refresh_token" to refreshToken,
                "expires_in" to 3600,
                "user" to mapOf("id" to userUuid.toString(), "email" to email)
            )
        )

        val response = authService.register(
            RegisterRequest(username = "testuser", email = email, password = password)
        )

        assert(response.accessToken == accessToken)
        assert(response.refreshToken == refreshToken)
        assert(response.email == email)
        assert(response.role == Role.USER)

        val savedUser = userRepository.findById(userUuid)
        assert(savedUser.isPresent)
        assert(savedUser.get().email == email)
    }

    @Test
    fun `login returns tokens for existing user`() {
        `when`(supabaseAuthClient.signUp(anyString(), anyString(), anyString())).thenReturn(
            mapOf("id" to userUuid.toString(), "email" to email)
        )
        `when`(supabaseAuthClient.signIn(anyString(), anyString())).thenReturn(
            mapOf(
                "access_token" to accessToken,
                "refresh_token" to refreshToken,
                "expires_in" to 3600,
                "user" to mapOf("id" to userUuid.toString(), "email" to email)
            )
        )

        authService.register(
            RegisterRequest(username = "testuser", email = email, password = password)
        )

        val response = authService.login(email, password)

        assert(response.accessToken == accessToken)
        assert(response.email == email)
    }

    @Test
    fun `me returns user info for valid id`() {
        `when`(supabaseAuthClient.signUp(anyString(), anyString(), anyString())).thenReturn(
            mapOf("id" to userUuid.toString(), "email" to email)
        )
        `when`(supabaseAuthClient.signIn(anyString(), anyString())).thenReturn(
            mapOf(
                "access_token" to accessToken,
                "refresh_token" to refreshToken,
                "expires_in" to 3600,
                "user" to mapOf("id" to userUuid.toString(), "email" to email)
            )
        )

        authService.register(
            RegisterRequest(username = "testuser", email = email, password = password)
        )

        val response = authService.me(userUuid)

        assert(response.email == email)
        assert(response.username == "testuser")
    }

    @Test
    fun `refresh returns new tokens`() {
        `when`(supabaseAuthClient.refreshToken(anyString())).thenReturn(
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
}
