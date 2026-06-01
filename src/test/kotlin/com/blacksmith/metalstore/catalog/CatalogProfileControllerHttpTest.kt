package com.blacksmith.metalstore.catalog

import com.blacksmith.metalstore.catalog.domain.entity.CatalogProfile
import com.blacksmith.metalstore.catalog.domain.repository.CatalogProfileRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogProfileControllerHttpTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var repo: CatalogProfileRepository

    @Test
    fun `list returns paginated profiles`() {
        val total = repo.count()
        mockMvc.perform(get("/api/catalog/profiles"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page.totalElements").value(total.toInt()))
    }

    @Test
    fun `search by q returns matching profiles`() {
        val count = repo.findByDesignationContainingIgnoreCase("W", PageRequest.of(0, 100)).totalElements

        mockMvc.perform(get("/api/catalog/profiles?q=W"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page.totalElements").value(count.toInt()))
    }

    @Test
    fun `filter by standard returns only matching`() {
        mockMvc.perform(get("/api/catalog/profiles?standard=AISC"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page.totalElements").value(2091))
    }

    @Test
    fun `filter by shapeType`() {
        val expected = repo.findByFamilyShapeType("H").size
        mockMvc.perform(get("/api/catalog/profiles?shapeType=H"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page.totalElements").value(expected))
    }

    @Test
    fun `filter by familyCode`() {
        val expected = repo.findByFamilyCode("W").size
        mockMvc.perform(get("/api/catalog/profiles?familyCode=W"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page.totalElements").value(expected))
    }

    @Test
    fun `combined search with q and standard`() {
        mockMvc.perform(get("/api/catalog/profiles?q=IPE&standard=EURO"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page.totalElements").value(18))
    }

    @Test
    fun `get by id returns profile`() {
        val profile = repo.findAll(PageRequest.of(0, 1)).content.first()

        mockMvc.perform(get("/api/catalog/profiles/{id}", profile.id))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(profile.id.toString()))
            .andExpect(jsonPath("$.designation").isString)
    }

    @Test
    fun `get by id returns 404 for non-existent`() {
        mockMvc.perform(get("/api/catalog/profiles/{id}", UUID.randomUUID()))
            .andExpect(status().isNotFound)
    }
}
