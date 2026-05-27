package com.blacksmith.metalstore.catalog.controller

import com.blacksmith.metalstore.catalog.domain.entity.CatalogItem
import com.blacksmith.metalstore.catalog.domain.repository.CatalogItemRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/catalog/items")
class CatalogItemController(
    private val repo: CatalogItemRepository
) {
    @GetMapping
    fun list(@PageableDefault(size = 20) pageable: Pageable): Page<CatalogItem> =
        repo.findAll(pageable)
}
