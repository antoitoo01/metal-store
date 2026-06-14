package com.blacksmith.metalstore.organization.controller

import com.blacksmith.metalstore.organization.application.OrganizationService
import com.blacksmith.metalstore.organization.config.CurrentOrganizationId
import com.blacksmith.metalstore.organization.domain.dto.request.CreateOrganizationRequest
import com.blacksmith.metalstore.organization.domain.dto.request.UpdateOrganizationRequest
import com.blacksmith.metalstore.organization.domain.dto.response.OrganizationResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import java.util.UUID

@RestController
@RequestMapping("/api/organizations")
@Tag(name = "Organizations", description = "Gestión de organizaciones multi-tenant")
class OrganizationController(
    private val service: OrganizationService,
) {
    @GetMapping
    @Operation(summary = "Listar organizaciones del usuario")
    fun list(@AuthenticationPrincipal jwt: Jwt?): List<OrganizationResponse> {
        val userId = jwt?.subject?.let { UUID.fromString(it) } ?: return emptyList()
        return service.findOrganizationsByUserId(userId)
    }

    @PostMapping
    @Operation(summary = "Crear organización")
    fun create(
        @AuthenticationPrincipal jwt: Jwt?,
        @Valid @RequestBody request: CreateOrganizationRequest,
    ): ResponseEntity<OrganizationResponse> {
        val userId = jwt?.subject?.let { UUID.fromString(it) }
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val org = service.createOrganization(userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(org)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener organización por ID")
    fun getById(@PathVariable id: UUID): OrganizationResponse =
        service.findOrganization(id)

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar organización")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateOrganizationRequest,
        @AuthenticationPrincipal jwt: Jwt?,
    ): ResponseEntity<OrganizationResponse> {
        val userId = jwt?.subject?.let { UUID.fromString(it) }
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        return ResponseEntity.ok(service.updateOrganization(id, request, userId))
    }
}
