package com.blacksmith.metalstore.inventory

import com.blacksmith.metalstore.auth.audit.AuditLogger
import com.blacksmith.metalstore.inventory.application.InventoryService
import com.blacksmith.metalstore.inventory.domain.entity.InventoryItem
import com.blacksmith.metalstore.inventory.domain.entity.MovementType
import com.blacksmith.metalstore.inventory.domain.repository.InventoryItemRepository
import com.blacksmith.metalstore.inventory.domain.repository.StockMovementRepository
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
class InventoryServiceTest {

    @Autowired
    private lateinit var repo: InventoryItemRepository

    @Autowired
    private lateinit var movementRepo: StockMovementRepository

    private lateinit var service: InventoryService
    private val organizationId = UUID.randomUUID()
    private val profileId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        repo.deleteAll()
        movementRepo.deleteAll()
        service = InventoryService(repo, movementRepo, mock(AuditLogger::class.java))
    }

    @Test
    fun `create and find inventory item`() {
        val item = InventoryItem(
            organizationId = organizationId,
            profileId = profileId,
            quantity = BigDecimal("150.00"),
            location = "Estante A1",
            costPriceEur = BigDecimal("450.00"),
            supplier = "Aceros S.A."
        )
        val saved = service.create(item)
        assert(saved.id != null)
        assert(saved.quantity == BigDecimal("150.00"))

        val found = service.findById(organizationId, saved.id)
        assert(found.supplier == "Aceros S.A.")
    }

    @Test
    fun `findAll returns only items for the given organization`() {
        val otherTenant = UUID.randomUUID()
        repo.save(InventoryItem(organizationId = organizationId, profileId = profileId, quantity = BigDecimal.ONE))
        repo.save(InventoryItem(organizationId = otherTenant, profileId = profileId, quantity = BigDecimal.ONE))

        val items = service.findAll(organizationId, PageRequest.of(0, 100))
        assert(items.totalElements == 1L)
    }

    @Test
    fun `delete removes item for the correct organization`() {
        val item = service.create(
            InventoryItem(organizationId = organizationId, profileId = profileId, quantity = BigDecimal.ONE)
        )
        service.delete(organizationId, item.id)
        assert(repo.findById(item.id).isEmpty)
    }

    @Test
    fun `delete throws for wrong organization`() {
        val otherTenant = UUID.randomUUID()
        val item = service.create(
            InventoryItem(organizationId = organizationId, profileId = profileId, quantity = BigDecimal.ONE)
        )
        try {
            service.delete(otherTenant, item.id)
            assert(false) { "Expected ResourceNotFoundException" }
        } catch (e: ResourceNotFoundException) {
            // expected
        }
    }

    @Test
    fun `create registers inbound movement`() {
        val item = service.create(
            InventoryItem(organizationId = organizationId, profileId = profileId, quantity = BigDecimal("100.00"))
        )
        val movements = service.getMovements(organizationId, item.id, PageRequest.of(0, 100))
        assert(movements.totalElements == 1L)
        val mov = movements.content[0]
        assert(mov.movementType == MovementType.INBOUND)
        assert(mov.quantity == BigDecimal("100.00"))
        assert(mov.previousQuantity == BigDecimal.ZERO)
        assert(mov.newQuantity == BigDecimal("100.00"))
    }

    @Test
    fun `addStock registers inbound movement`() {
        val item = service.create(
            InventoryItem(organizationId = organizationId, profileId = profileId, quantity = BigDecimal("50.00"))
        )
        service.addStock(organizationId, item.id, BigDecimal("30.00"), "Entrada extra")

        val movements = service.getMovements(organizationId, item.id, PageRequest.of(0, 100))
        assert(movements.totalElements == 2L)

        val updated = service.findById(organizationId, item.id)
        assert(updated.quantity == BigDecimal("80.00"))
    }

    @Test
    fun `removeStock registers outbound movement`() {
        val item = service.create(
            InventoryItem(organizationId = organizationId, profileId = profileId, quantity = BigDecimal("50.00"))
        )
        service.removeStock(organizationId, item.id, BigDecimal("20.00"), "Salida por venta")

        val movements = service.getMovements(organizationId, item.id, PageRequest.of(0, 100))
        assert(movements.totalElements == 2L)
        val outbound = movements.content[0]
        assert(outbound.movementType == MovementType.OUTBOUND)
        assert(outbound.quantity == BigDecimal("20.00"))
        assert(outbound.previousQuantity == BigDecimal("50.00"))
        assert(outbound.newQuantity == BigDecimal("30.00"))

        val updated = service.findById(organizationId, item.id)
        assert(updated.quantity == BigDecimal("30.00"))
    }

    @Test
    fun `removeStock throws on insufficient stock`() {
        val item = service.create(
            InventoryItem(organizationId = organizationId, profileId = profileId, quantity = BigDecimal("10.00"))
        )
        try {
            service.removeStock(organizationId, item.id, BigDecimal("99.00"), "Demasiado")
            assert(false) { "Expected IllegalArgumentException" }
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }
}
