package com.blacksmith.metalstore.catalog.controller

import com.blacksmith.metalstore.catalog.domain.entity.CatalogFamily
import com.blacksmith.metalstore.catalog.domain.repository.CatalogFamilyRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/catalog/families")
@Tag(name = "Catalog", description = "Familias de catálogo")
class CatalogFamilyController(
    private val repo: CatalogFamilyRepository
) {
    @GetMapping
    @Operation(summary = "Listar familias", description = "Retorna una lista de familias de catálogo.")
    @ApiResponse(responseCode = "200", description = "Operación exitosa")
    fun list(@RequestParam standard: String? = null): List<CatalogFamily> =
        if (standard != null) repo.findByStandard(standard) else repo.findAll()
}
