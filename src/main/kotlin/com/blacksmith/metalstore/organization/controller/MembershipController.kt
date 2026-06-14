package com.blacksmith.metalstore.organization.controller

import com.blacksmith.metalstore.organization.application.OrganizationService
import com.blacksmith.metalstore.organization.domain.dto.request.UpdateRoleRequest
import com.blacksmith.metalstore.organization.domain.dto.response.MembershipResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import java.util.UUID

@RestController
@RequestMapping("/api/organizations/{orgId}/members")
@Tag(name = "Members", description = "Gestión de miembros de organización")
class MembershipController(
    private val service: OrganizationService,
) {
    @GetMapping
    @Operation(summary = "Listar miembros de la organización")
    fun list(@PathVariable orgId: UUID): List<MembershipResponse> =
        service.getMembers(orgId)

    @GetMapping("/me")
    @Operation(summary = "Obtener mi membresía")
    fun me(
        @PathVariable orgId: UUID,
        @AuthenticationPrincipal jwt: Jwt?,
    ): ResponseEntity<MembershipResponse> {
        val userId = jwt?.subject?.let { UUID.fromString(it) }
            ?: return ResponseEntity.status(401).build()
        return ResponseEntity.ok(service.getMyMembership(orgId, userId))
    }

    @PutMapping("/{userId}/role")
    @Operation(summary = "Cambiar rol de un miembro")
    fun updateRole(
        @PathVariable orgId: UUID,
        @PathVariable userId: UUID,
        @Valid @RequestBody request: UpdateRoleRequest,
        @AuthenticationPrincipal jwt: Jwt?,
    ): ResponseEntity<Unit> {
        val currentUserId = jwt?.subject?.let { UUID.fromString(it) }
            ?: return ResponseEntity.status(401).build()
        service.updateMemberRole(orgId, userId, request.role, currentUserId)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Eliminar miembro de la organización")
    fun remove(
        @PathVariable orgId: UUID,
        @PathVariable userId: UUID,
        @AuthenticationPrincipal jwt: Jwt?,
    ): ResponseEntity<Unit> {
        val currentUserId = jwt?.subject?.let { UUID.fromString(it) }
            ?: return ResponseEntity.status(401).build()
        service.removeMember(orgId, userId, currentUserId)
        return ResponseEntity.noContent().build()
    }
}
