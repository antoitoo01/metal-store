package com.blacksmith.metalstore.inventory

import com.blacksmith.metalstore.auth.audit.AuditLogger
import com.blacksmith.metalstore.inventory.application.InventoryService
import com.blacksmith.metalstore.inventory.domain.entity.InventoryItem
import com.blacksmith.metalstore.inventory.domain.repository.InventoryItemRepository
import com.blacksmith.metalstore.shared.exception.ResourceNotFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
class InventoryServiceTest {

    @Autowired
    private lateinit var repo: InventoryItemRepository

    private lateinit var service: InventoryService
    private val organizationId = UUID.randomUUID()
    private val profileId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        repo.deleteAll()
        service = InventoryService(repo, mock(AuditLogger::class.java))
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
}