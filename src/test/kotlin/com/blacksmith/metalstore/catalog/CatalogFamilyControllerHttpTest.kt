package com.blacksmith.metalstore.catalog

import com.blacksmith.metalstore.catalog.domain.entity.CatalogFamily
import com.blacksmith.metalstore.catalog.domain.repository.CatalogFamilyRepository
import com.blacksmith.metalstore.catalog.domain.repository.CatalogItemRepository
import com.blacksmith.metalstore.catalog.domain.repository.CatalogProfileRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CatalogFamilyControllerHttpTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var familyRepo: CatalogFamilyRepository

    @Autowired
    private lateinit var profileRepo: CatalogProfileRepository

    @Autowired
    private lateinit var itemRepo: CatalogItemRepository

    @BeforeEach
    fun setUp() {
        itemRepo.deleteAll()
        profileRepo.deleteAll()
        familyRepo.deleteAll()
    }

    @Test
    fun `list returns all families`() {
        familyRepo.save(CatalogFamily(standard = "ISO", code = "HE", shapeType = "HEA", description = "HEA beam"))
        familyRepo.save(CatalogFamily(standard = "ISO", code = "IP", shapeType = "IPE", description = "IPE beam"))

        mockMvc.perform(get("/api/catalog/families"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.size()").value(2))
    }

    @Test
    fun `filter by standard returns matching families`() {
        familyRepo.save(CatalogFamily(standard = "ISO", code = "HE", shapeType = "HEA"))
        familyRepo.save(CatalogFamily(standard = "DIN", code = "TR", shapeType = "TUBE"))

        mockMvc.perform(get("/api/catalog/families?standard=ISO"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.size()").value(1))
            .andExpect(jsonPath("$[0].standard").value("ISO"))
    }

    @Test
    fun `filter by non-existent standard returns empty`() {
        familyRepo.save(CatalogFamily(standard = "ISO", code = "HE", shapeType = "HEA"))

        mockMvc.perform(get("/api/catalog/families?standard=UNKNOWN"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.size()").value(0))
    }
}
