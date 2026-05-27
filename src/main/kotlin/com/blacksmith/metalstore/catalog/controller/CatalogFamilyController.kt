package com.blacksmith.metalstore.catalog.controller

import com.blacksmith.metalstore.catalog.domain.entity.CatalogFamily
import com.blacksmith.metalstore.catalog.domain.repository.CatalogFamilyRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/catalog/families")
class CatalogFamilyController(
    private val repo: CatalogFamilyRepository
) {
    @GetMapping
    fun list(@RequestParam standard: String? = null): List<CatalogFamily> =
        if (standard != null) repo.findByStandard(standard) else repo.findAll()
}
