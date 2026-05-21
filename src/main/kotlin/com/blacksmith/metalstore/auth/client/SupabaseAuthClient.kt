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
            set("apikey", props.serviceRoleKey)
            set("Authorization", "Bearer ${props.serviceRoleKey}")
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
            set("apikey", props.anonKey)
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
            throw when (e.statusCode.value()) {
                400, 401 -> UserNotFoundException("Invalid login credentials")
                422 -> UserNotFoundException("Invalid email format")
                429 -> UserNotFoundException("Too many requests")
                else -> UserNotFoundException(e.responseBodyAsString)
            }
        }
    }

    fun getUser(accessToken: String): Map<String, Any?> {
        val headers = HttpHeaders().apply {
            set("apikey", props.anonKey)
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
}
