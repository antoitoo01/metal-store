package com.blacksmith.metalstore.catalog.application

import com.blacksmith.metalstore.auth.audit.AuditLogger
import com.blacksmith.metalstore.catalog.domain.entity.CatalogItem
import com.blacksmith.metalstore.catalog.domain.entity.CatalogProfile
import com.blacksmith.metalstore.catalog.domain.repository.CatalogItemRepository
import com.blacksmith.metalstore.catalog.domain.repository.CatalogProfileRepository
import com.blacksmith.metalstore.catalog.infrastructure.storage.ImageStorageService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
@Transactional
class CatalogImageService(
    private val profileRepo: CatalogProfileRepository,
    private val itemRepo: CatalogItemRepository,
    private val storage: ImageStorageService,
    private val audit: AuditLogger
) {
    companion object {
        private val ALLOWED_TYPES = setOf("image/jpeg", "image/png", "image/webp")
        private const val MAX_SIZE = 5L * 1024 * 1024
        private const val NAMESPACE_PROFILES = "profiles"
        private const val NAMESPACE_ITEMS = "items"
    }

    fun uploadProfileImage(profileId: UUID, file: MultipartFile): String {
        validate(file)
        val profile = profileRepo.findById(profileId).orElse(null)
            ?: throw IllegalArgumentException("Profile not found: $profileId")
        val filename = generateFilename(file)
        val url = storage.save(NAMESPACE_PROFILES, filename, file.bytes, file.contentType ?: "application/octet-stream")
        profile.imagePath = url
        profileRepo.save(profile)
        audit.log(AuditLogger.AuditEvent(
            action = "IMAGE_UPLOAD", entityType = "CatalogProfile",
            entityId = profileId.toString(), details = mapOf("imageUrl" to url)
        ))
        return url
    }

    fun deleteProfileImage(profileId: UUID) {
        val profile = profileRepo.findById(profileId).orElse(null)
            ?: throw IllegalArgumentException("Profile not found: $profileId")
        val url = profile.imagePath ?: return
        storage.delete(url)
        profile.imagePath = null
        profileRepo.save(profile)
        audit.log(AuditLogger.AuditEvent(
            action = "IMAGE_DELETE", entityType = "CatalogProfile", entityId = profileId.toString()
        ))
    }

    fun uploadItemImage(itemId: UUID, file: MultipartFile): String {
        validate(file)
        val item = itemRepo.findById(itemId).orElse(null)
            ?: throw IllegalArgumentException("Item not found: $itemId")
        val filename = generateFilename(file)
        val url = storage.save(NAMESPACE_ITEMS, filename, file.bytes, file.contentType ?: "application/octet-stream")
        item.imagePath = url
        itemRepo.save(item)
        audit.log(AuditLogger.AuditEvent(
            action = "IMAGE_UPLOAD", entityType = "CatalogItem",
            entityId = itemId.toString(), details = mapOf("imageUrl" to url)
        ))
        return url
    }

    fun deleteItemImage(itemId: UUID) {
        val item = itemRepo.findById(itemId).orElse(null)
            ?: throw IllegalArgumentException("Item not found: $itemId")
        val url = item.imagePath ?: return
        storage.delete(url)
        item.imagePath = null
        itemRepo.save(item)
        audit.log(AuditLogger.AuditEvent(
            action = "IMAGE_DELETE", entityType = "CatalogItem", entityId = itemId.toString()
        ))
    }

    fun loadImage(imageUrl: String): ByteArray? = storage.load(imageUrl)

    private fun validate(file: MultipartFile) {
        if (file.isEmpty) throw IllegalArgumentException("File is empty")
        val contentType = file.contentType ?: throw IllegalArgumentException("Unknown content type")
        if (contentType !in ALLOWED_TYPES) throw IllegalArgumentException("Unsupported content type: $contentType. Allowed: jpeg, png, webp")
        if (file.size > MAX_SIZE) throw IllegalArgumentException("File too large. Max: 5MB")
    }

    private fun generateFilename(file: MultipartFile): String {
        val original = file.originalFilename ?: "image"
        val ext = original.substringAfterLast('.', "jpg").takeIf { it.length <= 5 } ?: "jpg"
        val sanitized = original.substringBeforeLast('.').replace(Regex("[^a-zA-Z0-9_-]"), "_").take(50)
        return "${UUID.randomUUID()}_$sanitized.$ext"
    }
}
