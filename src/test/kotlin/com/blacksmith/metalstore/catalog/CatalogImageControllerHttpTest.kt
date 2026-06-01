package com.blacksmith.metalstore.catalog

import com.blacksmith.metalstore.catalog.domain.entity.CatalogItem
import com.blacksmith.metalstore.catalog.domain.repository.CatalogItemRepository
import com.blacksmith.metalstore.catalog.domain.repository.CatalogProfileRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.data.domain.PageRequest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogImageControllerHttpTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var profileRepo: CatalogProfileRepository

    @Autowired
    private lateinit var itemRepo: CatalogItemRepository

    @Test
    fun `upload profile image returns 201 with imageUrl`() {
        val profile = profileRepo.findAll(PageRequest.of(0, 1)).content.first()

        val file = MockMultipartFile("file", "test.jpg", "image/jpeg", byteArrayOf(1, 2, 3, 4))

        mockMvc.perform(multipart("/api/catalog/profiles/{id}/image", profile.id).file(file))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.imageUrl").isString)
    }

    @Test
    fun `delete profile image returns 204 and clears path`() {
        val profile = profileRepo.findAll(PageRequest.of(0, 1)).content.first()
        val file = MockMultipartFile("file", "test.jpg", "image/jpeg", byteArrayOf(1, 2, 3, 4))
        mockMvc.perform(multipart("/api/catalog/profiles/{id}/image", profile.id).file(file))

        mockMvc.perform(delete("/api/catalog/profiles/{id}/image", profile.id))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `upload item image returns 201`() {
        val item = itemRepo.save(CatalogItem(itemType = "BEAM", designation = "Steel Beam"))
        val file = MockMultipartFile("file", "beam.png", "image/png", byteArrayOf(5, 6, 7))

        mockMvc.perform(multipart("/api/catalog/items/{id}/image", item.id).file(file))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.imageUrl").isString)
    }

    @Test
    fun `delete item image returns 204`() {
        val item = itemRepo.save(CatalogItem(itemType = "BEAM", designation = "Steel Beam"))
        val file = MockMultipartFile("file", "beam.png", "image/png", byteArrayOf(5, 6, 7))
        mockMvc.perform(multipart("/api/catalog/items/{id}/image", item.id).file(file))

        mockMvc.perform(delete("/api/catalog/items/{id}/image", item.id))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `upload returns 400 for unsupported content type`() {
        val profile = profileRepo.findAll(PageRequest.of(0, 1)).content.first()
        val file = MockMultipartFile("file", "test.pdf", "application/pdf", byteArrayOf(1))

        mockMvc.perform(multipart("/api/catalog/profiles/{id}/image", profile.id).file(file))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `upload returns 400 for non-existent profile`() {
        val file = MockMultipartFile("file", "test.jpg", "image/jpeg", byteArrayOf(1))

        mockMvc.perform(multipart("/api/catalog/profiles/{id}/image", UUID.randomUUID()).file(file))
            .andExpect(status().isBadRequest)
    }
}
