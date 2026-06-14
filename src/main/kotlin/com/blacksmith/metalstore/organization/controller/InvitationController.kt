package com.blacksmith.metalstore.organization.controller

import com.blacksmith.metalstore.organization.application.OrganizationService
import com.blacksmith.metalstore.organization.domain.dto.request.CreateInvitationRequest
import com.blacksmith.metalstore.organization.domain.dto.response.InvitationResponse
import com.blacksmith.metalstore.organization.domain.dto.response.MembershipResponse
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
@RequestMapping
@Tag(name = "Invitations", description = "Gestión de invitaciones a organizaciones")
class InvitationController(
    private val service: OrganizationService,
) {
    @PostMapping("/api/organizations/{orgId}/invitations")
    @Operation(summary = "Crear invitación")
    fun create(
        @PathVariable orgId: UUID,
        @Valid @RequestBody request: CreateInvitationRequest,
        @AuthenticationPrincipal jwt: Jwt?,
    ): ResponseEntity<InvitationResponse> {
        val userId = jwt?.subject?.let { UUID.fromString(it) }
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val invitation = service.createInvitation(orgId, request.email, request.role, userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(invitation)
    }

    @GetMapping("/api/organizations/{orgId}/invitations")
    @Operation(summary = "Listar invitaciones pendientes")
    fun list(
        @PathVariable orgId: UUID,
        @AuthenticationPrincipal jwt: Jwt?,
    ): ResponseEntity<List<InvitationResponse>> {
        val userId = jwt?.subject?.let { UUID.fromString(it) }
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        return ResponseEntity.ok(service.getInvitations(orgId, userId))
    }

    @PostMapping("/api/invitations/{token}/accept")
    @Operation(summary = "Aceptar invitación")
    fun accept(
        @PathVariable token: UUID,
        @AuthenticationPrincipal jwt: Jwt?,
    ): ResponseEntity<MembershipResponse> {
        val userId = jwt?.subject?.let { UUID.fromString(it) }
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val membership = service.acceptInvitation(token, userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(membership)
    }
}
