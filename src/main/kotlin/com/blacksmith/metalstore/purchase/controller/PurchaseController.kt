package com.blacksmith.metalstore.purchase.controller

import com.blacksmith.metalstore.organization.config.CurrentOrganizationId
import com.blacksmith.metalstore.organization.config.RequiresRole
import com.blacksmith.metalstore.organization.domain.entity.OrganizationRole
import com.blacksmith.metalstore.purchase.application.PurchaseService
import com.blacksmith.metalstore.purchase.domain.dto.request.CreatePurchaseOrderLineRequest
import com.blacksmith.metalstore.purchase.domain.dto.request.CreatePurchaseOrderRequest
import com.blacksmith.metalstore.purchase.domain.dto.request.CreateSupplierRequest
import com.blacksmith.metalstore.purchase.domain.dto.request.UpdatePurchaseOrderRequest
import com.blacksmith.metalstore.purchase.domain.dto.request.UpdateSupplierRequest
import com.blacksmith.metalstore.purchase.domain.dto.response.PurchaseOrderLineResponse
import com.blacksmith.metalstore.purchase.domain.dto.response.PurchaseOrderResponse
import com.blacksmith.metalstore.purchase.domain.dto.response.SupplierResponse
import com.blacksmith.metalstore.purchase.domain.entity.PurchaseOrderStatus
import com.blacksmith.metalstore.purchase.domain.entity.SupplierStatus
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID

@RestController
@RequestMapping("/api")
@Tag(name = "Purchases", description = "Gestión de compras y proveedores")
class PurchaseController(
    private val service: PurchaseService
) {
    // ── Suppliers ──────────────────────────────────────────────────

    @GetMapping("/suppliers")
    @Operation(summary = "Listar proveedores", description = "Retorna una lista paginada de proveedores.")
    @ApiResponse(responseCode = "200", description = "Operación exitosa")
    fun listSuppliers(
        @CurrentOrganizationId organizationId: UUID,
        @PageableDefault(size = 20) pageable: Pageable,
        @RequestParam(name = "q", required = false) nameFilter: String?,
        @RequestParam(required = false) status: SupplierStatus?
    ): Page<SupplierResponse> =
        service.listSuppliers(organizationId, pageable, nameFilter, status).map { SupplierResponse.from(it) }

    @GetMapping("/suppliers/{id}")
    @Operation(summary = "Obtener proveedor por ID", description = "Retorna los datos de un proveedor por su UUID.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun getSupplier(@CurrentOrganizationId organizationId: UUID, @PathVariable id: UUID): SupplierResponse =
        SupplierResponse.from(service.findSupplier(organizationId, id))

    @PostMapping("/suppliers")
    @ResponseStatus(HttpStatus.CREATED)
    @RequiresRole(OrganizationRole.STAFF)
    @Operation(summary = "Crear proveedor", description = "Crea un nuevo proveedor.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Recurso creado"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun createSupplier(@CurrentOrganizationId organizationId: UUID, @Valid @RequestBody request: CreateSupplierRequest): SupplierResponse =
        SupplierResponse.from(service.createSupplier(request.toEntity(organizationId)))

    @PutMapping("/suppliers/{id}")
    @RequiresRole(OrganizationRole.STAFF)
    @Operation(summary = "Actualizar proveedor", description = "Actualiza los datos de un proveedor existente.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun updateSupplier(
        @CurrentOrganizationId organizationId: UUID,
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateSupplierRequest
    ): SupplierResponse =
        SupplierResponse.from(service.updateSupplier(organizationId, id, request.toEntity(organizationId)))

    @DeleteMapping("/suppliers/{id}")
    @RequiresRole(OrganizationRole.ADMIN)
    @Operation(summary = "Eliminar proveedor", description = "Elimina un proveedor por su UUID.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Sin contenido"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun deleteSupplier(@CurrentOrganizationId organizationId: UUID, @PathVariable id: UUID): ResponseEntity<Unit> {
        service.deleteSupplier(organizationId, id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/suppliers/{id}/activate")
    @RequiresRole(OrganizationRole.STAFF)
    @Operation(summary = "Activar proveedor", description = "Cambia el estado del proveedor a activo.")
    fun activateSupplier(@CurrentOrganizationId organizationId: UUID, @PathVariable id: UUID): SupplierResponse =
        SupplierResponse.from(service.activateSupplier(organizationId, id))

    @PostMapping("/suppliers/{id}/deactivate")
    @RequiresRole(OrganizationRole.STAFF)
    @Operation(summary = "Desactivar proveedor", description = "Cambia el estado del proveedor a inactivo.")
    fun deactivateSupplier(@CurrentOrganizationId organizationId: UUID, @PathVariable id: UUID): SupplierResponse =
        SupplierResponse.from(service.deactivateSupplier(organizationId, id))

    // ── Purchase Orders ────────────────────────────────────────────

    @GetMapping("/purchase-orders")
    @Operation(summary = "Listar órdenes de compra", description = "Retorna una lista paginada de órdenes de compra.")
    @ApiResponse(responseCode = "200", description = "Operación exitosa")
    fun listPurchaseOrders(
        @CurrentOrganizationId organizationId: UUID,
        @PageableDefault(size = 20) pageable: Pageable,
        @RequestParam(name = "q", required = false) q: String?,
        @RequestParam(name = "status", required = false) status: PurchaseOrderStatus?,
        @RequestParam(name = "supplierId", required = false) supplierId: UUID?
    ): Page<PurchaseOrderResponse> =
        service.listPurchaseOrders(organizationId, pageable, q, status, supplierId).map { PurchaseOrderResponse.from(it) }

    @GetMapping("/purchase-orders/{id}")
    @Operation(summary = "Obtener orden de compra por ID", description = "Retorna los datos de una orden de compra por su UUID.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun getPurchaseOrder(@CurrentOrganizationId organizationId: UUID, @PathVariable id: UUID): PurchaseOrderResponse =
        PurchaseOrderResponse.from(service.findPurchaseOrder(organizationId, id))

    @PostMapping("/purchase-orders")
    @ResponseStatus(HttpStatus.CREATED)
    @RequiresRole(OrganizationRole.STAFF)
    @Operation(summary = "Crear borrador de orden de compra", description = "Crea un nuevo borrador de orden de compra.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Recurso creado"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun createDraft(@CurrentOrganizationId organizationId: UUID, @Valid @RequestBody request: CreatePurchaseOrderRequest): PurchaseOrderResponse =
        PurchaseOrderResponse.from(service.createDraft(organizationId, request.toEntity(organizationId, "")))

    @PutMapping("/purchase-orders/{id}")
    @RequiresRole(OrganizationRole.STAFF)
    @Operation(summary = "Actualizar orden de compra", description = "Actualiza los datos de cabecera de una orden en estado DRAFT.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida o no está en DRAFT"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun updatePurchaseOrder(
        @CurrentOrganizationId organizationId: UUID,
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdatePurchaseOrderRequest
    ): PurchaseOrderResponse =
        PurchaseOrderResponse.from(service.update(organizationId, id, request.supplierId, request.supplierName, request.supplierVat, request.supplierAddress, request.expectedDate, request.notes))

    @GetMapping("/purchase-orders/{id}/lines")
    @Operation(summary = "Obtener líneas de orden de compra", description = "Retorna las líneas de una orden de compra.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun getLines(@PathVariable id: UUID): List<PurchaseOrderLineResponse> =
        service.getLines(id).map { PurchaseOrderLineResponse.from(it) }

    @PostMapping("/purchase-orders/{id}/lines")
    @RequiresRole(OrganizationRole.STAFF)
    @Operation(summary = "Agregar línea a orden de compra", description = "Agrega una nueva línea a una orden de compra existente.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun addLine(
        @CurrentOrganizationId organizationId: UUID,
        @PathVariable id: UUID,
        @Valid @RequestBody request: CreatePurchaseOrderLineRequest
    ): PurchaseOrderLineResponse =
        PurchaseOrderLineResponse.from(service.addLine(organizationId, id, request.toEntity(id)))

    @DeleteMapping("/purchase-orders/{poId}/lines/{lineId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequiresRole(OrganizationRole.STAFF)
    @Operation(summary = "Eliminar línea de orden de compra", description = "Elimina una línea de una orden de compra.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Sin contenido"),
        ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    ])
    fun removeLine(@CurrentOrganizationId organizationId: UUID, @PathVariable poId: UUID, @PathVariable lineId: UUID) {
        service.removeLine(organizationId, poId, lineId)
    }

    @PostMapping("/purchase-orders/{id}/issue")
    @RequiresRole(OrganizationRole.STAFF)
    @Operation(summary = "Emitir orden de compra", description = "Cambia el estado de la orden a emitida.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun issue(@CurrentOrganizationId organizationId: UUID, @PathVariable id: UUID): PurchaseOrderResponse =
        PurchaseOrderResponse.from(service.issue(organizationId, id))

    @PostMapping("/purchase-orders/{id}/receive")
    @RequiresRole(OrganizationRole.STAFF)
    @Operation(summary = "Recibir orden de compra", description = "Cambia el estado de la orden a recibida.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun receive(@CurrentOrganizationId organizationId: UUID, @PathVariable id: UUID): PurchaseOrderResponse =
        PurchaseOrderResponse.from(service.receive(organizationId, id))

    @PostMapping("/purchase-orders/{id}/cancel")
    @RequiresRole(OrganizationRole.STAFF)
    @Operation(summary = "Cancelar orden de compra", description = "Cambia el estado de la orden a cancelada.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Operación exitosa"),
        ApiResponse(responseCode = "400", description = "Solicitud inválida")
    ])
    fun cancel(@CurrentOrganizationId organizationId: UUID, @PathVariable id: UUID): PurchaseOrderResponse =
        PurchaseOrderResponse.from(service.cancel(organizationId, id))
}
