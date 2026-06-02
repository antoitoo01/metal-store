package com.blacksmith.metalstore.catalog.controller

import com.blacksmith.metalstore.catalog.application.CatalogImageService
import com.blacksmith.metalstore.catalog.config.StorageProperties
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID

@RestController
@Tag(name = "Catalog", description = "Imágenes de catálogo")
class CatalogImageController(
    private val imageService: CatalogImageService,
    private val storageProperties: StorageProperties
) {
    private val baseUrl: String = storageProperties.baseUrl.trimEnd('/')

    @PostMapping("/api/catalog/profiles/{id}/image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "Subir imagen de perfil", description = "Sube una imagen para un perfil de catálogo.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Recurso creado"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun uploadProfileImage(@PathVariable id: UUID, @RequestParam file: MultipartFile): ResponseEntity<Map<String, String>> {
        val url = imageService.uploadProfileImage(id, file)
        return ResponseEntity.status(HttpStatus.CREATED).body(mapOf("imageUrl" to url))
    }

    @DeleteMapping("/api/catalog/profiles/{id}/image")
    @Operation(summary = "Eliminar imagen de perfil", description = "Elimina la imagen de un perfil de catálogo.")
    @ApiResponse(responseCode = "204", description = "Sin contenido")
    fun deleteProfileImage(@PathVariable id: UUID): ResponseEntity<Unit> {
        imageService.deleteProfileImage(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/api/catalog/items/{id}/image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "Subir imagen de ítem", description = "Sube una imagen para un ítem de catálogo.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Recurso creado"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun uploadItemImage(@PathVariable id: UUID, @RequestParam file: MultipartFile): ResponseEntity<Map<String, String>> {
        val url = imageService.uploadItemImage(id, file)
        return ResponseEntity.status(HttpStatus.CREATED).body(mapOf("imageUrl" to url))
    }

    @DeleteMapping("/api/catalog/items/{id}/image")
    @Operation(summary = "Eliminar imagen de ítem", description = "Elimina la imagen de un ítem de catálogo.")
    @ApiResponse(responseCode = "204", description = "Sin contenido")
    fun deleteItemImage(@PathVariable id: UUID): ResponseEntity<Unit> {
        imageService.deleteItemImage(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/api/images/{namespace}/{filename}")
    @Operation(summary = "Servir imagen", description = "Retorna una imagen del catálogo por namespace y nombre de archivo.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun serveImage(@PathVariable namespace: String, @PathVariable filename: String): ResponseEntity<ByteArray> {
        val url = "$baseUrl/api/images/$namespace/$filename"
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
