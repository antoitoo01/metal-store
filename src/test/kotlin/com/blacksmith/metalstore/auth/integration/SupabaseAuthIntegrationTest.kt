package com.blacksmith.metalstore.auth.integration

import com.blacksmith.metalstore.auth.client.SupabaseAuthClient
import com.blacksmith.metalstore.auth.config.SupabaseProperties
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestTemplate
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.MethodName::class)
class SupabaseAuthIntegrationTest {

    @Autowired
    private lateinit var props: SupabaseProperties

    @Autowired
    private lateinit var client: SupabaseAuthClient

    private val rest = RestTemplate()
    private val testEmail = "inttest-${UUID.randomUUID().toString().take(8)}@metalstore-test.com"
    private val password = "Test1234!"
    private var supabaseUserId: String? = null
    private var accessToken: String? = null
    private var refreshToken: String? = null

    @BeforeAll
    fun checkEnv() {
        assumeTrue(props.url.isNotBlank()) { "SUPABASE_URL not set â€” skipping real auth integration tests" }
    }

    @AfterAll
    fun cleanup() {
        supabaseUserId?.let { uid ->
            try {
                val headers = HttpHeaders().apply {
                    set("apikey", props.secretKey)
                    set("Authorization", "Bearer ${props.secretKey}")
                }
                rest.exchange(
                    "${props.url}/auth/v1/admin/users/$uid",
                    HttpMethod.DELETE,
                    HttpEntity<Any>(headers),
                    Map::class.java
                )
            } catch (_: Exception) { }
        }
    }

    @Test
    fun `01 register creates user in Supabase and returns id`() {
        val response = client.signUp(testEmail, password, "inttest-user")
        supabaseUserId = response["id"] as? String
        assertNotNull(supabaseUserId, "Should have a user id")
        assertEquals(testEmail, response["email"])

        assertDoesNotThrow {
            rest.exchange(
                "${props.url}/auth/v1/admin/users/${supabaseUserId}",
                HttpMethod.GET,
                HttpEntity<Any>(HttpHeaders().apply {
                    set("apikey", props.secretKey)
                    set("Authorization", "Bearer ${props.secretKey}")
                }),
                Map::class.java
            )
        }
    }

    @Test
    fun `02 login returns access token for registered user`() {
        val response = client.signIn(testEmail, password)
        accessToken = response["access_token"] as? String
        refreshToken = response["refresh_token"] as? String

        assertNotNull(accessToken, "Should have access token")
        assertNotNull(refreshToken, "Should have refresh token")
        assertTrue((response["expires_in"] as? Int ?: 0) > 0, "expires_in should be positive")
    }

    @Test
    fun `03 get user returns details with valid token`() {
        assertNotNull(accessToken) { "Run login test first or ensure accessToken is set" }

        val user = client.getUser(accessToken!!)
        val userId = user["id"] as? String
        assertEquals(supabaseUserId, userId, "User id should match registered user")
        assertEquals(testEmail, user["email"], "Email should match")
    }

    @Test
    fun `04 refresh token returns new tokens`() {
        assertNotNull(refreshToken) { "Run login test first or ensure refreshToken is set" }

        val response = client.refreshToken(refreshToken!!)
        val newAccess = response["access_token"] as? String
        val newRefresh = response["refresh_token"] as? String

        assertNotNull(newAccess, "Should have new access token")
        assertNotNull(newRefresh, "Should have new refresh token")
        assertNotEquals(accessToken, newAccess, "Tokens should be rotated")
        accessToken = newAccess
        refreshToken = newRefresh
    }

    @Test
    fun `05 logout revokes session`() {
        assertNotNull(accessToken) { "Run login test first" }

        assertDoesNotThrow { client.signOut(accessToken!!) }

        val ex = assertThrows<Exception> {
            client.getUser(accessToken!!)
        }
        val msg = ex.message?.lowercase() ?: ""
        assertTrue(msg.contains("not found") || msg.contains("invalid"))
    }
}