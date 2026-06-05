package com.blacksmith.metalstore.auth.client

import com.blacksmith.metalstore.auth.config.SupabaseProperties
import com.blacksmith.metalstore.auth.exception.UserAlreadyExistsException
import com.blacksmith.metalstore.auth.exception.UserNotFoundException
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@Service
class SupabaseAuthClient(
    private val props: SupabaseProperties
) {
    private val rest = RestTemplate()

    fun signUp(email: String, password: String, username: String?): Map<String, Any?> {
        val headers = HttpHeaders().apply {
            set("apikey", props.secretKey)
            set("Authorization", "Bearer ${props.secretKey}")
            set("Content-Type", "application/json")
        }
        val body = buildMap<String, Any?> {
            put("email", email)
            put("password", password)
            put("email_confirm", true)
            if (username != null) {
                put("user_metadata", mapOf("username" to username))
            }
        }
        return try {
            val response: ResponseEntity<Map<String, Any?>> = rest.exchange(
                "${props.url}/auth/v1/admin/users",
                HttpMethod.POST,
                HttpEntity(body, headers),
                object : ParameterizedTypeReference<Map<String, Any?>>() {}
            )
            response.body ?: throw UserAlreadyExistsException("Empty response from Supabase")
        } catch (e: HttpClientErrorException) {
            throw when (e.statusCode.value()) {
                422 -> UserAlreadyExistsException("User already exists")
                429 -> UserAlreadyExistsException("Too many requests")
                else -> UserAlreadyExistsException(e.responseBodyAsString)
            }
        }
    }

    fun signIn(email: String, password: String): Map<String, Any?> {
        val headers = HttpHeaders().apply {
            set("apikey", props.publishableKey)
            set("Content-Type", "application/json")
        }
        val body = mapOf("email" to email, "password" to password)
        return try {
            val response: ResponseEntity<Map<String, Any?>> = rest.exchange(
                "${props.url}/auth/v1/token?grant_type=password",
                HttpMethod.POST,
                HttpEntity(body, headers),
                object : ParameterizedTypeReference<Map<String, Any?>>() {}
            )
            response.body ?: throw UserNotFoundException("Empty response from Supabase")
        } catch (e: HttpClientErrorException) {
            val supabaseMessage = try {
                @Suppress("UNCHECKED_CAST")
                (e.responseBodyAsString.let { body ->
                    com.fasterxml.jackson.databind.ObjectMapper().readTree(body).get("error_description")?.asText()
                })
            } catch (_: Exception) { null } ?: e.responseBodyAsString
            throw when (e.statusCode.value()) {
                400, 401 -> UserNotFoundException("Invalid login credentials: $supabaseMessage")
                422 -> UserNotFoundException("Invalid email format: $supabaseMessage")
                429 -> UserNotFoundException("Too many requests")
                else -> UserNotFoundException(e.responseBodyAsString)
            }
        }
    }

    fun getUser(accessToken: String): Map<String, Any?> {
        val headers = HttpHeaders().apply {
            set("apikey", props.publishableKey)
            set("Authorization", "Bearer $accessToken")
        }
        val response: ResponseEntity<Map<String, Any?>> = rest.exchange(
            "${props.url}/auth/v1/user",
            HttpMethod.GET,
            HttpEntity(emptyMap<String, Any>(), headers),
            object : ParameterizedTypeReference<Map<String, Any?>>() {}
        )
        return response.body ?: throw UserNotFoundException("User not found")
    }

    fun refreshToken(refreshToken: String): Map<String, Any?> {
        val headers = HttpHeaders().apply {
            set("apikey", props.publishableKey)
            set("Content-Type", "application/json")
        }
        val body = mapOf("refresh_token" to refreshToken)
        return try {
            val response: ResponseEntity<Map<String, Any?>> = rest.exchange(
                "${props.url}/auth/v1/token?grant_type=refresh_token",
                HttpMethod.POST,
                HttpEntity(body, headers),
                object : ParameterizedTypeReference<Map<String, Any?>>() {}
            )
            response.body ?: throw UserNotFoundException("Invalid refresh token")
        } catch (e: HttpClientErrorException) {
            throw when (e.statusCode.value()) {
                400 -> UserNotFoundException("Invalid refresh token")
                else -> UserNotFoundException(e.responseBodyAsString)
            }
        }
    }

    fun signOut(accessToken: String) {
        val headers = HttpHeaders().apply {
            set("apikey", props.publishableKey)
            set("Authorization", "Bearer $accessToken")
        }
        rest.exchange(
            "${props.url}/auth/v1/logout",
            HttpMethod.POST,
            HttpEntity(emptyMap<String, Any>(), headers),
            Map::class.java
        )
    }

    fun updateUserEmail(userId: java.util.UUID, newEmail: String) {
        val headers = HttpHeaders().apply {
            set("apikey", props.secretKey)
            set("Authorization", "Bearer ${props.secretKey}")
            set("Content-Type", "application/json")
        }
        val body = mapOf("email" to newEmail, "email_confirm" to true)
        try {
            rest.exchange(
                "${props.url}/auth/v1/admin/users/$userId",
                HttpMethod.PUT,
                HttpEntity(body, headers),
                Map::class.java
            )
        } catch (e: HttpClientErrorException) {
            throw when (e.statusCode.value()) {
                400, 422 -> UserAlreadyExistsException("Email already exists or invalid: ${e.responseBodyAsString}")
                else -> UserAlreadyExistsException("Error updating user in Supabase: ${e.responseBodyAsString}")
            }
        }
    }

    fun deleteUser(userId: java.util.UUID) {
        val headers = HttpHeaders().apply {
            set("apikey", props.secretKey)
            set("Authorization", "Bearer ${props.secretKey}")
        }
        try {
            rest.exchange(
                "${props.url}/auth/v1/admin/users/$userId",
                HttpMethod.DELETE,
                HttpEntity(emptyMap<String, Any>(), headers),
                Map::class.java
            )
        } catch (e: HttpClientErrorException) {
            throw when (e.statusCode.value()) {
                404 -> UserNotFoundException("User not found in Supabase")
                else -> UserNotFoundException("Error deleting user in Supabase: ${e.responseBodyAsString}")
            }
        }
    }
}
