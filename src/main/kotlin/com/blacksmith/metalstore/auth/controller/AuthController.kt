package com.blacksmith.metalstore.auth.controller

import com.blacksmith.metalstore.auth.domain.dto.request.LoginRequest
import com.blacksmith.metalstore.auth.domain.dto.request.RefreshTokenRequest
import com.blacksmith.metalstore.auth.domain.dto.request.RegisterRequest
import com.blacksmith.metalstore.auth.domain.dto.response.LoginResponse
import com.blacksmith.metalstore.auth.domain.dto.response.UserResponse
import com.blacksmith.metalstore.auth.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Autenticación y registro de usuarios")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar nuevo usuario", description = "Crea un nuevo usuario en Supabase Auth y en la base local.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun register(@Valid @RequestBody request: RegisterRequest): LoginResponse =
        authService.register(request)

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica al usuario mediante Supabase Auth y retorna JWT.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "401", description = "No autorizado")
    ])
    fun login(@Valid @RequestBody request: LoginRequest): LoginResponse =
        authService.login(request.email, request.password)

    @GetMapping("/me")
    @Operation(summary = "Obtener usuario actual", description = "Retorna el usuario autenticado mediante el JWT.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "401", description = "No autorizado")
    ])
    fun me(@AuthenticationPrincipal jwt: Jwt?): ResponseEntity<UserResponse> {
        val userId = jwt?.subject ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        return ResponseEntity.ok(authService.me(UUID.fromString(userId)))
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refrescar token", description = "Refresca el token de acceso usando un refresh token.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): LoginResponse =
        authService.refresh(request.refreshToken)

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cerrar sesión", description = "Revoca la sesión del usuario.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Sin contenido"),
        ApiResponse(responseCode = "401", description = "No autorizado")
    ])
    fun logout(@RequestHeader("Authorization") authHeader: String) {
        authService.logout(authHeader.removePrefix("Bearer "))
    }
}
