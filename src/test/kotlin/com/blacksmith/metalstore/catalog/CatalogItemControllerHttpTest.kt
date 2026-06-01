package com.blacksmith.metalstore.catalog

import com.blacksmith.metalstore.catalog.domain.entity.CatalogItem
import com.blacksmith.metalstore.catalog.domain.repository.CatalogItemRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogItemControllerHttpTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var repo: CatalogItemRepository

    @BeforeEach
    fun setUp() {
        repo.deleteAll()
    }

    @Test
    fun `list returns paginated items`() {
        repo.save(CatalogItem(itemType = "BEAM", designation = "Steel Beam A"))
        repo.save(CatalogItem(itemType = "BEAM", designation = "Steel Beam B"))
        repo.save(CatalogItem(itemType = "PLATE", designation = "Steel Plate 1"))

        mockMvc.perform(get("/api/catalog/items"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page.totalElements").value(3))
    }

    @Test
    fun `search by q returns matching items`() {
        repo.save(CatalogItem(itemType = "FASTENER", designation = "Hex Bolt M12"))
        repo.save(CatalogItem(itemType = "FASTENER", designation = "Hex Nut M12"))
        repo.save(CatalogItem(itemType = "BEAM", designation = "Steel Beam"))

        mockMvc.perform(get("/api/catalog/items?q=Hex"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page.totalElements").value(2))
    }

    @Test
    fun `filter by itemType`() {
        repo.save(CatalogItem(itemType = "FASTENER", designation = "Screw"))
        repo.save(CatalogItem(itemType = "BEAM", designation = "Beam"))

        mockMvc.perform(get("/api/catalog/items?itemType=BEAM"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page.totalElements").value(1))
    }

    @Test
    fun `combined search by q and itemType`() {
        repo.save(CatalogItem(itemType = "FASTENER", designation = "Hex Bolt M12"))
        repo.save(CatalogItem(itemType = "FASTENER", designation = "Hex Nut M12"))
        repo.save(CatalogItem(itemType = "BEAM", designation = "Hex Beam"))

        mockMvc.perform(get("/api/catalog/items?q=Hex&itemType=FASTENER"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page.totalElements").value(2))
    }

    @Test
    fun `get by id returns item`() {
        val item = repo.save(CatalogItem(itemType = "PLATE", designation = "Steel Plate"))

        mockMvc.perform(get("/api/catalog/items/{id}", item.id))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.designation").value("Steel Plate"))
    }

    @Test
    fun `get by id returns 404 for non-existent`() {
        mockMvc.perform(get("/api/catalog/items/{id}", UUID.randomUUID()))
            .andExpect(status().isNotFound)
    }
}
