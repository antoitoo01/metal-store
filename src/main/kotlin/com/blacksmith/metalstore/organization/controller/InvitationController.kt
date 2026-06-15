package com.blacksmith.metalstore.organization.controller

import com.blacksmith.metalstore.auth.config.CurrentUser
import com.blacksmith.metalstore.auth.config.CurrentUserInfo
import com.blacksmith.metalstore.auth.config.CurrentUserId
import com.blacksmith.metalstore.organization.application.InvitationService
import com.blacksmith.metalstore.organization.domain.dto.request.AcceptRequest
import com.blacksmith.metalstore.organization.domain.dto.request.CreateInvitationRequest
import com.blacksmith.metalstore.organization.domain.dto.request.DeclineRequest
import com.blacksmith.metalstore.organization.domain.dto.response.InvitationResponse
import com.blacksmith.metalstore.organization.domain.dto.response.MembershipResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping
@Tag(name = "Invitations", description = "Gestión de invitaciones a organizaciones")
class InvitationController(
    private val service: InvitationService,
) {
    @PostMapping("/api/organizations/{orgId}/invitations")
    @Operation(summary = "Crear invitaciones en lote")
    fun create(
        @PathVariable orgId: UUID,
        @Valid @RequestBody request: CreateInvitationRequest,
        @CurrentUserId userId: UUID?,
    ): ResponseEntity<List<InvitationResponse>> {
        val uid = userId ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val invitations = service.createInvitations(orgId, request.emails, uid)
        return ResponseEntity.status(HttpStatus.CREATED).body(invitations)
    }

    @GetMapping("/api/organizations/{orgId}/invitations")
    @Operation(summary = "Listar invitaciones paginadas")
    fun list(
        @PathVariable orgId: UUID,
        pageable: Pageable,
        @CurrentUserId userId: UUID?,
    ): ResponseEntity<Page<InvitationResponse>> {
        val uid = userId ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        return ResponseEntity.ok(service.listInvitations(orgId, pageable, uid))
    }

    @DeleteMapping("/api/organizations/{orgId}/invitations/{id}")
    @Operation(summary = "Cancelar invitación")
    fun cancel(
        @PathVariable orgId: UUID,
        @PathVariable id: UUID,
        @CurrentUserId userId: UUID?,
    ): ResponseEntity<Unit> {
        val uid = userId ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        service.cancelInvitation(orgId, id, uid)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/api/invitations/accept")
    @Operation(summary = "Aceptar invitación")
    fun accept(
        @Valid @RequestBody request: AcceptRequest,
        @CurrentUser user: CurrentUserInfo?,
    ): ResponseEntity<MembershipResponse> {
        val u = user ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val membership = service.acceptInvitation(request.token, u.id, u.email)
        return ResponseEntity.ok(membership)
    }

    @PostMapping("/api/invitations/decline")
    @Operation(summary = "Rechazar invitación")
    fun decline(
        @Valid @RequestBody request: DeclineRequest,
        @CurrentUserId userId: UUID?,
    ): ResponseEntity<Unit> {
        val uid = userId ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        service.declineInvitation(request.token, uid)
        return ResponseEntity.ok().build()
    }
}
