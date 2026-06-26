package com.blacksmith.metalstore.purchase

import com.blacksmith.metalstore.auth.audit.AuditLogger
import com.blacksmith.metalstore.purchase.application.PurchaseService
import com.blacksmith.metalstore.purchase.domain.entity.PurchaseOrder
import com.blacksmith.metalstore.purchase.domain.entity.PurchaseOrderLine
import com.blacksmith.metalstore.purchase.domain.entity.PurchaseOrderStatus
import com.blacksmith.metalstore.purchase.domain.entity.Supplier
import com.blacksmith.metalstore.purchase.domain.entity.SupplierStatus
import com.blacksmith.metalstore.purchase.domain.repository.PurchaseOrderLineRepository
import com.blacksmith.metalstore.purchase.domain.repository.PurchaseOrderRepository
import com.blacksmith.metalstore.purchase.domain.repository.SupplierRepository
import com.blacksmith.metalstore.shared.NumberSequenceRepository
import com.blacksmith.metalstore.shared.exception.ResourceNotFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PurchaseServiceTest {

    @Autowired
    private lateinit var supplierRepo: SupplierRepository

    @Autowired
    private lateinit var poRepo: PurchaseOrderRepository

    @Autowired
    private lateinit var poLineRepo: PurchaseOrderLineRepository

    @Autowired
    private lateinit var numberSequenceRepo: NumberSequenceRepository

    private lateinit var service: PurchaseService
    private val organizationId = UUID.randomUUID()
    private val otherOrganizationId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        supplierRepo.deleteAll()
        poRepo.deleteAll()
        poLineRepo.deleteAll()
        service = PurchaseService(supplierRepo, poRepo, poLineRepo, numberSequenceRepo, mock(AuditLogger::class.java))
    }

    // ── Supplier Tests ─────────────────────────────────────────────

    @Test
    fun `create and find supplier`() {
        val saved = service.createSupplier(Supplier(organizationId = organizationId, name = "Aceros del Sur S.L."))
        assert(saved.id != null)
        assert(saved.name == "Aceros del Sur S.L.")

        val found = service.findSupplier(organizationId, saved.id)
        assert(found.name == "Aceros del Sur S.L.")
    }

    @Test
    fun `listSuppliers returns only suppliers for the given organization`() {
        supplierRepo.save(Supplier(organizationId = organizationId, name = "Proveedor A"))
        supplierRepo.save(Supplier(organizationId = otherOrganizationId, name = "Proveedor B"))

        val suppliers = service.listSuppliers(organizationId, PageRequest.of(0, 100))
        assert(suppliers.totalElements == 1L)
    }

    @Test
    fun `listSuppliers filters by name`() {
        supplierRepo.save(Supplier(organizationId = organizationId, name = "Taller Pérez"))
        supplierRepo.save(Supplier(organizationId = organizationId, name = "Herrería García"))
        supplierRepo.save(Supplier(organizationId = organizationId, name = "Taller López"))

        val result = service.listSuppliers(organizationId, PageRequest.of(0, 100), "Taller")
        assert(result.totalElements == 2L)
    }

    @Test
    fun `deleteSupplier removes supplier for the correct organization`() {
        val supplier = service.createSupplier(Supplier(organizationId = organizationId, name = "Test"))
        service.deleteSupplier(organizationId, supplier.id)
        assert(supplierRepo.findById(supplier.id).isEmpty)
    }

    @Test
    fun `deleteSupplier throws for wrong organization`() {
        val supplier = service.createSupplier(Supplier(organizationId = organizationId, name = "Test"))
        try {
            service.deleteSupplier(otherOrganizationId, supplier.id)
            assert(false) { "Expected ResourceNotFoundException" }
        } catch (e: ResourceNotFoundException) {
            // expected
        }
    }

    @Test
    fun `activateSupplier toggles status`() {
        val supplier = service.createSupplier(Supplier(organizationId = organizationId, name = "Test"))
        val deactivated = service.deactivateSupplier(organizationId, supplier.id)
        assert(deactivated.status == SupplierStatus.INACTIVE)

        val activated = service.activateSupplier(organizationId, supplier.id)
        assert(activated.status == SupplierStatus.ACTIVE)
    }

    @Test
    fun `updateSupplier merges fields`() {
        val supplier = service.createSupplier(Supplier(organizationId = organizationId, name = "Original"))
        val updated = Supplier(
            id = supplier.id,
            organizationId = organizationId,
            name = "Updated Name",
            email = "new@email.com",
            phone = null,
            address = null,
            vatNumber = null,
            notes = null,
            status = SupplierStatus.ACTIVE
        )
        val result = service.updateSupplier(organizationId, supplier.id, updated)
        assert(result.name == "Updated Name")
        assert(result.email == "new@email.com")
    }

    // ── Purchase Order Tests ───────────────────────────────────────

    @Test
    fun `create draft purchase order`() {
        val po = service.createDraft(organizationId, PurchaseOrder(organizationId = organizationId, poNumber = ""))
        assert(po.status == PurchaseOrderStatus.DRAFT)
        assert(po.poNumber.startsWith("OC-"))
    }

    @Test
    fun `add line to draft recalculates totals`() {
        val po = service.createDraft(organizationId, PurchaseOrder(organizationId = organizationId, poNumber = ""))
        val profileId = UUID.randomUUID()
        val line = PurchaseOrderLine(
            poId = po.id,
            lineNumber = 1,
            description = "Viga HEB200",
            profileId = profileId,
            quantity = BigDecimal("150.00"),
            unitPrice = BigDecimal("2.50"),
            totalPrice = BigDecimal("375.00")
        )
        service.addLine(organizationId, po.id, line)

        val updated = service.findPurchaseOrder(organizationId, po.id)
        assert(updated.subtotal.compareTo(BigDecimal("375.00")) == 0)
        assert(updated.total > BigDecimal.ZERO)
    }

    @Test
    fun `issue purchase order changes status`() {
        val po = service.createDraft(organizationId, PurchaseOrder(organizationId = organizationId, poNumber = ""))
        val issued = service.issue(organizationId, po.id)
        assert(issued.status == PurchaseOrderStatus.ISSUED)
    }

    @Test
    fun `cannot add lines to issued purchase order`() {
        val po = service.createDraft(organizationId, PurchaseOrder(organizationId = organizationId, poNumber = ""))
        service.issue(organizationId, po.id)
        val line = PurchaseOrderLine(
            poId = po.id, lineNumber = 1, description = "Test",
            quantity = BigDecimal.ONE, unitPrice = BigDecimal.TEN, totalPrice = BigDecimal.TEN
        )
        try {
            service.addLine(organizationId, po.id, line)
            assert(false) { "Expected IllegalArgumentException" }
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `receive transitions from issued`() {
        val po = service.createDraft(organizationId, PurchaseOrder(organizationId = organizationId, poNumber = ""))
        service.issue(organizationId, po.id)
        val received = service.receive(organizationId, po.id)
        assert(received.status == PurchaseOrderStatus.RECEIVED)
    }

    @Test
    fun `cancel transitions from draft`() {
        val po = service.createDraft(organizationId, PurchaseOrder(organizationId = organizationId, poNumber = ""))
        val cancelled = service.cancel(organizationId, po.id)
        assert(cancelled.status == PurchaseOrderStatus.CANCELLED)
    }

    @Test
    fun `removeLine recalculates totals`() {
        val po = service.createDraft(organizationId, PurchaseOrder(organizationId = organizationId, poNumber = ""))
        val line1 = service.addLine(organizationId, po.id, PurchaseOrderLine(
            poId = po.id, lineNumber = 1, description = "Item 1",
            quantity = BigDecimal("10"), unitPrice = BigDecimal("100"), totalPrice = BigDecimal.ZERO
        ))
        service.addLine(organizationId, po.id, PurchaseOrderLine(
            poId = po.id, lineNumber = 2, description = "Item 2",
            quantity = BigDecimal("5"), unitPrice = BigDecimal("50"), totalPrice = BigDecimal.ZERO
        ))

        service.removeLine(organizationId, po.id, line1.id)
        val updated = service.findPurchaseOrder(organizationId, po.id)
        assert(updated.subtotal.compareTo(BigDecimal("250.00")) == 0)
    }
}
