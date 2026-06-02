package com.blacksmith.metalstore.catalog.controller

import com.blacksmith.metalstore.catalog.domain.entity.CatalogProfile
import com.blacksmith.metalstore.catalog.domain.repository.CatalogProfileRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID

@RestController
@RequestMapping("/api/catalog/profiles")
@Tag(name = "Catalog", description = "Perfiles de catálogo")
class CatalogProfileController(
    private val repo: CatalogProfileRepository
) {
    @GetMapping
    @Operation(summary = "Listar perfiles", description = "Retorna una lista paginada de perfiles de catálogo.")
    @ApiResponse(responseCode = "200", description = "Operación exitosa")
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
    @Operation(summary = "Obtener perfil por ID", description = "Retorna un perfil de catálogo por su UUID.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun getById(@PathVariable id: UUID): ResponseEntity<CatalogProfile> {
        val profile = repo.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(profile)
    }
}
