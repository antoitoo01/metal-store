package com.blacksmith.metalstore.catalog

import com.blacksmith.metalstore.auth.audit.AuditLogger
import com.blacksmith.metalstore.catalog.application.CatalogImageService
import com.blacksmith.metalstore.catalog.domain.entity.CatalogFamily
import com.blacksmith.metalstore.catalog.domain.entity.CatalogItem
import com.blacksmith.metalstore.catalog.domain.entity.CatalogProfile
import com.blacksmith.metalstore.catalog.domain.repository.CatalogItemRepository
import com.blacksmith.metalstore.catalog.domain.repository.CatalogProfileRepository
import com.blacksmith.metalstore.catalog.infrastructure.storage.ImageStorageService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.springframework.mock.web.MockMultipartFile
import java.util.Optional
import java.util.UUID

class CatalogImageServiceTest {

    private val profileRepo = mock(CatalogProfileRepository::class.java)
    private val itemRepo = mock(CatalogItemRepository::class.java)
    private val storage = mock(ImageStorageService::class.java)
    private val audit = mock(AuditLogger::class.java)
    private lateinit var service: CatalogImageService

    @BeforeEach
    fun setUp() {
        service = CatalogImageService(profileRepo, itemRepo, storage, audit)
    }

    @Test
    fun `upload profile image saves file and updates entity`() {
        val profile = dummyProfile()
        val fileBytes = byteArrayOf(1, 2, 3, 4)
        val file = MockMultipartFile("file", "photo.jpg", "image/jpeg", fileBytes)
        `when`(storage.save(anyString(), anyString(), anyByteArray(), anyString())).thenReturn("http://url")

        val url = service.uploadProfileImage(profile.id, file)

        assert(url == "http://url")
        assert(profile.imagePath == "http://url")
        verify(profileRepo).save(profile)
        verify(audit).log(anyAuditEvent())
    }

    @Test
    fun `delete profile image clears path and removes file`() {
        val profile = dummyProfile()
        profile.imagePath = "http://url"

        service.deleteProfileImage(profile.id)

        assert(profile.imagePath == null)
        verify(storage).delete("http://url")
        verify(profileRepo).save(profile)
    }

    @Test
    fun `delete profile image does nothing when no image`() {
        val profile = dummyProfile()

        service.deleteProfileImage(profile.id)

        verify(storage, never()).delete(anyString())
    }

    @Test
    fun `upload item image saves file and updates entity`() {
        val item = CatalogItem(itemType = "BEAM", designation = "Test")
        `when`(itemRepo.findById(item.id)).thenReturn(Optional.of(item))
        val fileBytes = byteArrayOf(5, 6, 7)
        val file = MockMultipartFile("file", "img.png", "image/png", fileBytes)
        `when`(storage.save(anyString(), anyString(), anyByteArray(), anyString())).thenReturn("http://url")

        val url = service.uploadItemImage(item.id, file)

        assert(url == "http://url")
        assert(item.imagePath == "http://url")
        verify(itemRepo).save(item)
    }

    @Test
    fun `delete item image clears path and removes file`() {
        val item = CatalogItem(itemType = "BEAM", designation = "Test")
        item.imagePath = "http://url"
        `when`(itemRepo.findById(item.id)).thenReturn(Optional.of(item))

        service.deleteItemImage(item.id)

        assert(item.imagePath == null)
        verify(storage).delete("http://url")
        verify(itemRepo).save(item)
    }

    @Test
    fun `rejects unsupported content type`() {
        val profile = dummyProfile()
        val file = MockMultipartFile("file", "doc.pdf", "application/pdf", byteArrayOf(1))

        try {
            service.uploadProfileImage(profile.id, file)
            assert(false) { "Expected exception" }
        } catch (e: IllegalArgumentException) {
            assert(e.message!!.contains("Unsupported content type"))
        }
    }

    @Test
    fun `rejects empty file`() {
        val profile = dummyProfile()
        val file = MockMultipartFile("file", "empty.jpg", "image/jpeg", ByteArray(0))

        try {
            service.uploadProfileImage(profile.id, file)
            assert(false) { "Expected exception" }
        } catch (e: IllegalArgumentException) {
            assert(e.message!!.contains("empty"))
        }
    }

    private fun dummyProfile(): CatalogProfile {
        val id = UUID.randomUUID()
        val family = mock(CatalogFamily::class.java)
        val profile = object : CatalogProfile(id, family, "Dummy") {}
        `when`(profileRepo.findById(id)).thenReturn(Optional.of(profile))
        return profile
    }

    private fun anyByteArray(): ByteArray {
        ArgumentMatchers.any(ByteArray::class.java)
        return byteArrayOf()
    }

    private fun anyAuditEvent(): AuditLogger.AuditEvent {
        ArgumentMatchers.any(AuditLogger.AuditEvent::class.java)
        return AuditLogger.AuditEvent(action = "", entityType = "", entityId = "")
    }
}
