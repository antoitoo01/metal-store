package com.blacksmith.metalstore.catalog.controller

import com.blacksmith.metalstore.catalog.domain.entity.CatalogItem
import com.blacksmith.metalstore.catalog.domain.repository.CatalogItemRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/catalog/items")
class CatalogItemController(
    private val repo: CatalogItemRepository
) {
    @GetMapping
    fun list(
        @RequestParam q: String? = null,
        @RequestParam itemType: String? = null,
        @PageableDefault(size = 20) pageable: Pageable
    ): Page<CatalogItem> {
        if (q != null || itemType != null) {
            return repo.searchItems(q, itemType, pageable)
        }
        return repo.findAll(pageable)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): ResponseEntity<CatalogItem> {
        val item = repo.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(item)
    }
}
