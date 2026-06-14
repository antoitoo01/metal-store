package com.blacksmith.metalstore.catalog.controller

import com.blacksmith.metalstore.catalog.domain.dto.response.CatalogItemResponse
import com.blacksmith.metalstore.catalog.domain.repository.CatalogItemRepository
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
@RequestMapping("/api/catalog/items")
@Tag(name = "Catalog", description = "Ítems de catálogo")
class CatalogItemController(
    private val repo: CatalogItemRepository
) {
    @GetMapping
    @Operation(summary = "Listar ítems", description = "Retorna una lista paginada de ítems de catálogo.")
    @ApiResponse(responseCode = "200", description = "Operación exitosa")
    fun list(
        @RequestParam q: String? = null,
        @RequestParam itemType: String? = null,
        @PageableDefault(size = 20) pageable: Pageable
    ): Page<CatalogItemResponse> {
        val page = if (q != null || itemType != null) repo.searchItems(q, itemType, pageable)
        else repo.findAll(pageable)
        return page.map { CatalogItemResponse.from(it) }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener ítem por ID", description = "Retorna un ítem de catálogo por su UUID.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun getById(@PathVariable id: UUID): ResponseEntity<CatalogItemResponse> {
        val item = repo.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(CatalogItemResponse.from(item))
    }
}
