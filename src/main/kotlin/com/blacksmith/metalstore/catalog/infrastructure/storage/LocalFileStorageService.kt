package com.blacksmith.metalstore.catalog.infrastructure.storage

import com.blacksmith.metalstore.catalog.config.StorageProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

@Service
@Profile("dev", "test")
class LocalFileStorageService(
    private val properties: StorageProperties
) : ImageStorageService {

    private val basePath: Path = Path.of(properties.uploadDir).toAbsolutePath().normalize()
    private val baseUrl: String = properties.baseUrl.trimEnd('/')

    override fun save(namespace: String, filename: String, bytes: ByteArray, contentType: String): String {
        val dir = basePath.resolve(namespace)
        Files.createDirectories(dir)
        Files.write(dir.resolve(filename), bytes)
        return "$baseUrl/api/images/$namespace/${URLEncoder.encode(filename, StandardCharsets.UTF_8)}"
    }

    override fun delete(imageUrl: String) {
        val path = extractPath(imageUrl) ?: return
        Files.deleteIfExists(basePath.resolve(path))
    }

    override fun load(imageUrl: String): ByteArray? {
        val path = extractPath(imageUrl) ?: return null
        val file = basePath.resolve(path)
        return if (Files.exists(file)) Files.readAllBytes(file) else null
    }

    private fun extractPath(imageUrl: String): String? {
        val prefix = "$baseUrl/api/images/"
        if (!imageUrl.startsWith(prefix)) return null
        val encoded = imageUrl.removePrefix(prefix)
        return URLDecoder.decode(encoded, StandardCharsets.UTF_8)
    }
}
