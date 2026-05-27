package com.blacksmith.metalstore.catalog.controller

import com.blacksmith.metalstore.catalog.domain.entity.CatalogProfile
import com.blacksmith.metalstore.catalog.domain.repository.CatalogProfileRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/catalog/profiles")
class CatalogProfileController(
    private val repo: CatalogProfileRepository
) {
    @GetMapping
    fun list(
        @RequestParam standard: String? = null,
        @PageableDefault(size = 20) pageable: Pageable
    ): Page<CatalogProfile> =
        if (standard != null) repo.findByFamilyStandard(standard, pageable)
        else repo.findAll(pageable)
}
