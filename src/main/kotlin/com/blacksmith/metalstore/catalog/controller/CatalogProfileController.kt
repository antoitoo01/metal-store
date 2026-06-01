package com.blacksmith.metalstore.catalog.controller

import com.blacksmith.metalstore.catalog.domain.entity.CatalogProfile
import com.blacksmith.metalstore.catalog.domain.repository.CatalogProfileRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/catalog/profiles")
class CatalogProfileController(
    private val repo: CatalogProfileRepository
) {
    @GetMapping
    fun list(
        @RequestParam q: String? = null,
        @RequestParam standard: String? = null,
        @RequestParam shapeType: String? = null,
        @RequestParam familyCode: String? = null,
        @PageableDefault(size = 20) pageable: Pageable
    ): Page<CatalogProfile> {
        if (q != null || standard != null || shapeType != null || familyCode != null) {
            return repo.searchProfiles(q, standard, shapeType, familyCode, pageable)
        }
        return repo.findAll(pageable)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): ResponseEntity<CatalogProfile> {
        val profile = repo.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(profile)
    }
}
