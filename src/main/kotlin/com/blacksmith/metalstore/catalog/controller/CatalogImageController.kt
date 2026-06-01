package com.blacksmith.metalstore.catalog.controller

import com.blacksmith.metalstore.catalog.application.CatalogImageService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
class CatalogImageController(
    private val imageService: CatalogImageService
) {
    @PostMapping("/api/catalog/profiles/{id}/image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadProfileImage(@PathVariable id: UUID, @RequestParam file: MultipartFile): ResponseEntity<Map<String, String>> {
        val url = imageService.uploadProfileImage(id, file)
        return ResponseEntity.status(HttpStatus.CREATED).body(mapOf("imageUrl" to url))
    }

    @DeleteMapping("/api/catalog/profiles/{id}/image")
    fun deleteProfileImage(@PathVariable id: UUID): ResponseEntity<Unit> {
        imageService.deleteProfileImage(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/api/catalog/items/{id}/image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadItemImage(@PathVariable id: UUID, @RequestParam file: MultipartFile): ResponseEntity<Map<String, String>> {
        val url = imageService.uploadItemImage(id, file)
        return ResponseEntity.status(HttpStatus.CREATED).body(mapOf("imageUrl" to url))
    }

    @DeleteMapping("/api/catalog/items/{id}/image")
    fun deleteItemImage(@PathVariable id: UUID): ResponseEntity<Unit> {
        imageService.deleteItemImage(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/api/images/{namespace}/{filename}")
    fun serveImage(@PathVariable namespace: String, @PathVariable filename: String): ResponseEntity<ByteArray> {
        val url = "http://localhost:8080/api/images/$namespace/$filename"
        val bytes = imageService.loadImage(url) ?: return ResponseEntity.notFound().build()
        val contentType = guessContentType(filename)
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(bytes)
    }

    private fun guessContentType(filename: String): String = when {
        filename.endsWith(".jpg") || filename.endsWith(".jpeg") -> "image/jpeg"
        filename.endsWith(".png") -> "image/png"
        filename.endsWith(".webp") -> "image/webp"
        else -> "application/octet-stream"
    }
}
