package com.blacksmith.metalstore.purchase

import com.blacksmith.metalstore.purchase.domain.entity.PurchaseOrder
import com.blacksmith.metalstore.purchase.domain.entity.PurchaseOrderLine
import com.blacksmith.metalstore.purchase.domain.entity.PurchaseOrderStatus
import com.blacksmith.metalstore.purchase.domain.entity.Supplier
import com.blacksmith.metalstore.purchase.domain.repository.PurchaseOrderLineRepository
import com.blacksmith.metalstore.purchase.domain.repository.PurchaseOrderRepository
import com.blacksmith.metalstore.purchase.domain.repository.SupplierRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PurchaseControllerHttpTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var supplierRepo: SupplierRepository

    @Autowired
    private lateinit var poRepo: PurchaseOrderRepository

    @Autowired
    private lateinit var poLineRepo: PurchaseOrderLineRepository

    private val organizationId = UUID.randomUUID()
    private val otherOrganizationId = UUID.randomUUID()
    private val profileId = UUID.randomUUID()
    private val headerName = "X-Organization-Id"

    @BeforeEach
    fun setUp() {
        supplierRepo.deleteAll()
        poLineRepo.deleteAll()
        poRepo.deleteAll()
    }

    // ── Supplier HTTP Tests ────────────────────────────────────────

    @Test
    fun `listSuppliers returns paginated suppliers for organization`() {
        supplierRepo.save(Supplier(organizationId = organizationId, name = "Proveedor A"))
        supplierRepo.save(Supplier(organizationId = organizationId, name = "Proveedor B"))
        supplierRepo.save(Supplier(organizationId = otherOrganizationId, name = "Otro Proveedor"))

        mockMvc.perform(get("/api/suppliers")
            .header(headerName, organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page.totalElements").value(2))
            .andExpect(jsonPath("$.content.length()").value(2))
    }

    @Test
    fun `listSuppliers filters by query param`() {
        supplierRepo.save(Supplier(organizationId = organizationId, name = "Taller Pérez"))
        supplierRepo.save(Supplier(organizationId = organizationId, name = "Herrería García"))

        mockMvc.perform(get("/api/suppliers")
            .param("q", "Pérez")
            .header(headerName, organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page.totalElements").value(1))
    }

    @Test
    fun `createSupplier stores supplier with organization`() {
        val body = """{"name":"Aceros del Norte","email":"info@aceros.com","phone":"965123456"}"""

        mockMvc.perform(post("/api/suppliers")
            .header(headerName, organizationId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Aceros del Norte"))
            .andExpect(jsonPath("$.email").value("info@aceros.com"))
            .andExpect(jsonPath("$.organizationId").value(organizationId.toString()))
    }

    @Test
    fun `getSupplier returns supplier by id`() {
        val supplier = supplierRepo.save(Supplier(organizationId = organizationId, name = "Test"))

        mockMvc.perform(get("/api/suppliers/{id}", supplier.id)
            .header(headerName, organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(supplier.id.toString()))
    }

    @Test
    fun `getSupplier returns 404 for non-existent id`() {
        mockMvc.perform(get("/api/suppliers/{id}", UUID.randomUUID())
            .header(headerName, organizationId.toString()))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `deleteSupplier removes supplier`() {
        val supplier = supplierRepo.save(Supplier(organizationId = organizationId, name = "Test"))

        mockMvc.perform(delete("/api/suppliers/{id}", supplier.id)
            .header(headerName, organizationId.toString()))
            .andExpect(status().isNoContent)

        assert(supplierRepo.findById(supplier.id).isEmpty)
    }

    @Test
    fun `updateSupplier modifies supplier`() {
        val supplier = supplierRepo.save(Supplier(organizationId = organizationId, name = "Original"))
        val body = """{"name":"Updated"}"""

        mockMvc.perform(put("/api/suppliers/{id}", supplier.id)
            .header(headerName, organizationId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Updated"))
    }

    @Test
    fun `activateSupplier toggles supplier status`() {
        val supplier = supplierRepo.save(Supplier(organizationId = organizationId, name = "Test"))

        mockMvc.perform(post("/api/suppliers/{id}/deactivate", supplier.id)
            .header(headerName, organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("INACTIVE"))

        mockMvc.perform(post("/api/suppliers/{id}/activate", supplier.id)
            .header(headerName, organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("ACTIVE"))
    }

    // ── Purchase Order HTTP Tests ──────────────────────────────────

    @Test
    fun `createDraft purchase order`() {
        val body = """{"supplierName":"Aceros del Sur","supplierVat":"B12345678"}"""

        mockMvc.perform(post("/api/purchase-orders")
            .header(headerName, organizationId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("DRAFT"))
            .andExpect(jsonPath("$.poNumber").isString)
            .andExpect(jsonPath("$.supplierName").value("Aceros del Sur"))
    }

    @Test
    fun `listPurchaseOrders returns paginated results`() {
        poRepo.save(PurchaseOrder(organizationId = organizationId, poNumber = "OC-2026-001"))
        poRepo.save(PurchaseOrder(organizationId = organizationId, poNumber = "OC-2026-002"))

        mockMvc.perform(get("/api/purchase-orders")
            .header(headerName, organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page.totalElements").value(2))
    }

    @Test
    fun `getPurchaseOrder by id`() {
        val po = poRepo.save(PurchaseOrder(organizationId = organizationId, poNumber = "OC-2026-001"))

        mockMvc.perform(get("/api/purchase-orders/{id}", po.id)
            .header(headerName, organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(po.id.toString()))
            .andExpect(jsonPath("$.poNumber").value("OC-2026-001"))
    }

    @Test
    fun `addLine to draft recalculates totals`() {
        val po = poRepo.save(PurchaseOrder(organizationId = organizationId, poNumber = "OC-2026-001"))
        val lineBody = """{"lineNumber":1,"description":"Viga HEB200","profileId":"$profileId","quantity":150.00,"unitPrice":2.50,"vatRate":21.00}"""

        mockMvc.perform(post("/api/purchase-orders/{id}/lines", po.id)
            .header(headerName, organizationId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(lineBody))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.lineNumber").value(1))
            .andExpect(jsonPath("$.totalPrice").value(375.00))
    }

    @Test
    fun `cannot add lines to issued purchase order`() {
        val po = poRepo.save(PurchaseOrder(
            organizationId = organizationId,
            poNumber = "OC-2026-001",
            status = PurchaseOrderStatus.ISSUED
        ))
        val lineBody = """{"lineNumber":1,"description":"Test","quantity":1,"unitPrice":10,"vatRate":21.00}"""

        mockMvc.perform(post("/api/purchase-orders/{id}/lines", po.id)
            .header(headerName, organizationId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(lineBody))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `issue purchase order changes status`() {
        val po = poRepo.save(PurchaseOrder(organizationId = organizationId, poNumber = "OC-2026-001"))

        mockMvc.perform(post("/api/purchase-orders/{id}/issue", po.id)
            .header(headerName, organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("ISSUED"))
    }

    @Test
    fun `receive transitions from issued`() {
        val po = poRepo.save(PurchaseOrder(
            organizationId = organizationId,
            poNumber = "OC-2026-001",
            status = PurchaseOrderStatus.ISSUED
        ))

        mockMvc.perform(post("/api/purchase-orders/{id}/receive", po.id)
            .header(headerName, organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("RECEIVED"))
    }

    @Test
    fun `cancel transitions from draft`() {
        val po = poRepo.save(PurchaseOrder(organizationId = organizationId, poNumber = "OC-2026-001"))

        mockMvc.perform(post("/api/purchase-orders/{id}/cancel", po.id)
            .header(headerName, organizationId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("CANCELLED"))
    }

    @Test
    fun `organization isolation prevents cross-tenant access`() {
        val po = poRepo.save(PurchaseOrder(organizationId = otherOrganizationId, poNumber = "OC-2026-001"))

        mockMvc.perform(get("/api/purchase-orders/{id}", po.id)
            .header(headerName, organizationId.toString()))
            .andExpect(status().isNotFound)
    }
}
