package com.blacksmith.metalstore.auth.client

import com.blacksmith.metalstore.auth.config.SupabaseProperties
import com.blacksmith.metalstore.shared.exception.ApiException
import com.blacksmith.metalstore.shared.exception.ErrorCode
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate

class SupabaseAuthClientTest {

    private val props = SupabaseProperties().copy(
        url = "https://test.supabase.co",
        publishableKey = "test-publishable-key",
        secretKey = "test-secret-key"
    )
    private val rest = mock(RestTemplate::class.java)
    private val client = SupabaseAuthClient(props, rest)

    @Test
    fun `signIn returns body on success`() {
        val expectedBody = mapOf(
            "access_token" to "token-123",
            "user" to mapOf("id" to "uuid-1", "email" to "test@test.com")
        )
        `when`(
            rest.exchange(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any<HttpMethod>(),
                ArgumentMatchers.any<HttpEntity<*>>(),
                ArgumentMatchers.any<ParameterizedTypeReference<Map<String, Any?>>>()
            )
        ).thenReturn(ResponseEntity.ok(expectedBody))

        val result = client.signIn("test@test.com", "pass")
        assert(result["access_token"] == "token-123")
    }

    @Test
    fun `signIn throws INVALID_CREDENTIALS on 401`() {
        val ex = HttpClientErrorException.create(
            HttpStatus.UNAUTHORIZED,
            "Unauthorized",
            HttpHeaders.EMPTY,
            null,
            null
        )
        `when`(
            rest.exchange(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any<HttpMethod>(),
                ArgumentMatchers.any<HttpEntity<*>>(),
                ArgumentMatchers.any<ParameterizedTypeReference<Map<String, Any?>>>()
            )
        ).thenThrow(ex)

        try {
            client.signIn("test@test.com", "wrong")
            assert(false) { "Expected ApiException" }
        } catch (e: ApiException) {
            assert(e.errorCode == ErrorCode.INVALID_CREDENTIALS)
        }
    }

    @Test
    fun `signIn throws INVALID_CREDENTIALS on 400`() {
        val ex = HttpClientErrorException.create(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            HttpHeaders.EMPTY,
            null,
            null
        )
        `when`(
            rest.exchange(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any<HttpMethod>(),
                ArgumentMatchers.any<HttpEntity<*>>(),
                ArgumentMatchers.any<ParameterizedTypeReference<Map<String, Any?>>>()
            )
        ).thenThrow(ex)

        try {
            client.signIn("test@test.com", "wrong")
            assert(false) { "Expected ApiException" }
        } catch (e: ApiException) {
            assert(e.errorCode == ErrorCode.INVALID_CREDENTIALS)
        }
    }

    @Test
    fun `signIn throws SERVICE_UNAVAILABLE on ResourceAccessException`() {
        `when`(
            rest.exchange(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any<HttpMethod>(),
                ArgumentMatchers.any<HttpEntity<*>>(),
                ArgumentMatchers.any<ParameterizedTypeReference<Map<String, Any?>>>()
            )
        ).thenThrow(ResourceAccessException("Connection refused"))

        try {
            client.signIn("test@test.com", "pass")
            assert(false) { "Expected ApiException" }
        } catch (e: ApiException) {
            assert(e.errorCode == ErrorCode.SERVICE_UNAVAILABLE)
        }
    }

    @Test
    fun `signIn throws VALIDATION_ERROR on 422`() {
        val ex = HttpClientErrorException.create(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "Unprocessable Entity",
            HttpHeaders.EMPTY,
            null,
            null
        )
        `when`(
            rest.exchange(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any<HttpMethod>(),
                ArgumentMatchers.any<HttpEntity<*>>(),
                ArgumentMatchers.any<ParameterizedTypeReference<Map<String, Any?>>>()
            )
        ).thenThrow(ex)

        try {
            client.signIn("test@test.com", "pass")
            assert(false) { "Expected ApiException" }
        } catch (e: ApiException) {
            assert(e.errorCode == ErrorCode.VALIDATION_ERROR)
        }
    }

    @Test
    fun `signIn throws RATE_LIMIT_EXCEEDED on 429`() {
        val ex = HttpClientErrorException.create(
            HttpStatus.TOO_MANY_REQUESTS,
            "Too Many Requests",
            HttpHeaders.EMPTY,
            null,
            null
        )
        `when`(
            rest.exchange(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.any<HttpMethod>(),
                ArgumentMatchers.any<HttpEntity<*>>(),
                ArgumentMatchers.any<ParameterizedTypeReference<Map<String, Any?>>>()
            )
        ).thenThrow(ex)

        try {
            client.signIn("test@test.com", "pass")
            assert(false) { "Expected ApiException" }
        } catch (e: ApiException) {
            assert(e.errorCode == ErrorCode.RATE_LIMIT_EXCEEDED)
        }
    }
}