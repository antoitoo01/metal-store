package com.blacksmith.metalstore.catalog.infrastructure.storage

import com.blacksmith.metalstore.auth.config.SupabaseProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
@Profile("prod")
class SupabaseFileStorageService(
    private val supabase: SupabaseProperties,
    @Value("\${app.storage.bucket-name:catalog-images}") private val bucketName: String
) : ImageStorageService {

    private val rest = RestTemplate()

    override fun save(namespace: String, filename: String, bytes: ByteArray, contentType: String): String {
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer ${supabase.secretKey}")
            set("Content-Type", contentType)
            set("x-upsert", "true")
        }
        val path = "$namespace/$filename"
        rest.exchange(
            "${supabase.url}/storage/v1/object/$bucketName/$path",
            HttpMethod.POST,
            HttpEntity(bytes, headers),
            Map::class.java
        )
        return "${supabase.url}/storage/v1/object/public/$bucketName/$path"
    }

    override fun delete(imageUrl: String) {
        val path = extractPath(imageUrl) ?: return
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer ${supabase.secretKey}")
        }
        rest.exchange(
            "${supabase.url}/storage/v1/object/$bucketName/$path",
            HttpMethod.DELETE,
            HttpEntity(emptyMap<String, Any>(), headers),
            Map::class.java
        )
    }

    override fun load(imageUrl: String): ByteArray? {
        return try {
            val headers = HttpHeaders().apply {
                set("Authorization", "Bearer ${supabase.secretKey}")
            }
            val response = rest.exchange(
                imageUrl,
                HttpMethod.GET,
                HttpEntity(null, headers),
                ByteArray::class.java
            )
            response.body
        } catch (_: Exception) {
            null
        }
    }

    private fun extractPath(imageUrl: String): String? {
        val prefix = "${supabase.url}/storage/v1/object/public/$bucketName/"
        if (!imageUrl.startsWith(prefix)) return null
        return imageUrl.removePrefix(prefix)
    }
}
