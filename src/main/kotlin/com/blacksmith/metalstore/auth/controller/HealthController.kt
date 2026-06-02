package com.blacksmith.metalstore.auth.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import java.time.Instant

@RestController
@Tag(name = "Auth", description = "Estado del servicio")
class HealthController {

    @GetMapping("/api/health")
    @Operation(summary = "Verificar estado del servicio", description = "Retorna el estado actual del servicio.")
    @ApiResponse(responseCode = "200", description = "Operación exitosa")
    fun health(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(
            mapOf(
                "status" to "UP",
                "timestamp" to Instant.now().toString()
            )
        )
    }
}
