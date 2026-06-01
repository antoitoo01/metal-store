package com.blacksmith.metalstore.catalog.infrastructure.storage

import org.springframework.beans.factory.annotation.Value
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
    @Value("\${app.storage.upload-dir:uploads}") uploadDir: String,
    @Value("\${server.port:8080}") private val serverPort: Int
) : ImageStorageService {

    private val basePath: Path = Path.of(uploadDir).toAbsolutePath().normalize()

    override fun save(namespace: String, filename: String, bytes: ByteArray, contentType: String): String {
        val dir = basePath.resolve(namespace)
        Files.createDirectories(dir)
        Files.write(dir.resolve(filename), bytes)
        return "http://localhost:$serverPort/api/images/$namespace/${URLEncoder.encode(filename, StandardCharsets.UTF_8)}"
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
        val prefix = "http://localhost:$serverPort/api/images/"
        if (!imageUrl.startsWith(prefix)) return null
        val encoded = imageUrl.removePrefix(prefix)
        return URLDecoder.decode(encoded, StandardCharsets.UTF_8)
    }
}
