package com.blacksmith.metalstore.auth.controller

import com.blacksmith.metalstore.auth.domain.dto.request.UpdateUserRequest
import com.blacksmith.metalstore.auth.domain.dto.response.UserResponse
import com.blacksmith.metalstore.auth.service.UserService
import com.blacksmith.metalstore.organization.config.CurrentOrganizationId
import com.blacksmith.metalstore.organization.config.RequiresRole
import com.blacksmith.metalstore.organization.domain.entity.OrganizationRole
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.*

@RestController
@RequestMapping("/api/users")
@Tag(name = "Auth", description = "Gestión de usuarios")
class UserController(private val userService: UserService) {

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Retorna una lista paginada de usuarios de la organización.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa")
    ])
    fun list(
        @CurrentOrganizationId organizationId: UUID,
        @PageableDefault(size = 20) pageable: Pageable,
        @RequestParam(name = "q", required = false) q: String?
    ): Page<UserResponse> =
        userService.findAll(organizationId, pageable, q).map { UserResponse.from(it) }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID", description = "Retorna los datos de un usuario por su UUID.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun getById(@PathVariable id: UUID): ResponseEntity<UserResponse> {
        val user = userService.findById(id)
        return ResponseEntity.ok(UserResponse.from(user))
    }

    @PutMapping
    @Operation(summary = "Actualizar usuario", description = "Actualiza los datos del usuario autenticado.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        ApiResponse(responseCode = "401", description = "No autorizado")
    ])
    fun update(
        @Valid @RequestBody request: UpdateUserRequest,
        @AuthenticationPrincipal jwt: Jwt?
    ): ResponseEntity<UserResponse> {
        val authenticatedId = jwt?.subject?.let { UUID.fromString(it) }
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val user = userService.update(request, authenticatedId)
        return ResponseEntity.ok(UserResponse.from(user))
    }

    @DeleteMapping("/{id}")
    @RequiresRole(OrganizationRole.ADMIN)
    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario por su UUID.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Sin contenido"),
        ApiResponse(responseCode = "401", description = "No autorizado"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun deleteById(
        @PathVariable id: UUID,
        @AuthenticationPrincipal jwt: Jwt?
    ): ResponseEntity<Void> {
        val authenticatedId = jwt?.subject?.let { UUID.fromString(it) }
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        userService.deleteById(id, authenticatedId)
        return ResponseEntity.noContent().build()
    }
}
