package com.blacksmith.metalstore.shared

import com.blacksmith.metalstore.auth.domain.entity.Role
import com.blacksmith.metalstore.auth.domain.entity.User
import com.blacksmith.metalstore.auth.domain.entity.UserState
import com.blacksmith.metalstore.catalog.domain.entity.CatalogFamily
import com.blacksmith.metalstore.catalog.domain.entity.CatalogItem
import com.blacksmith.metalstore.catalog.domain.entity.CatalogItemType
import com.blacksmith.metalstore.inventory.domain.entity.InventoryItem
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

class EntityEqualityTest {

    @Test
    fun `same ID should be equal for InventoryItem`() {
        val id = UUID.randomUUID()
        assert(dummyInventory(id) == dummyInventory(id))
    }

    @Test
    fun `different ID should not be equal for InventoryItem`() {
        assert(dummyInventory(UUID.randomUUID()) != dummyInventory(UUID.randomUUID()))
    }

    @Test
    fun `same reference should be equal`() {
        val item = dummyInventory(UUID.randomUUID())
        assert(item == item)
    }

    @Test
    fun `entity should not equal null`() {
        assert(!dummyInventory(UUID.randomUUID()).equals(null))
    }

    @Test
    fun `entity should not equal different type`() {
        assert(!dummyInventory(UUID.randomUUID()).equals("some string"))
    }

    @Test
    fun `same ID should produce same hashCode for InventoryItem`() {
        val id = UUID.randomUUID()
        assert(dummyInventory(id).hashCode() == dummyInventory(id).hashCode())
    }

    @Test
    fun `same ID should be equal for User`() {
        val id = UUID.randomUUID()
        val email = "test@example.com"
        assert(user(id, email) == user(id, email))
    }

    @Test
    fun `different ID should not be equal for User`() {
        assert(user(UUID.randomUUID(), "a@x.com") != user(UUID.randomUUID(), "b@x.com"))
    }

    @Test
    fun `same ID should be equal for CatalogItem`() {
        val id = UUID.randomUUID()
        assert(catalogItem(id) == catalogItem(id))
    }

    @Test
    fun `same ID should be equal for CatalogFamily`() {
        val id = UUID.randomUUID()
        assert(catalogFamily(id) == catalogFamily(id))
    }

    @Test
    fun `same ID should be equal for CatalogItemType`() {
        val id = UUID.randomUUID()
        assert(catalogItemType(id) == catalogItemType(id))
    }

    private fun dummyInventory(id: UUID) = InventoryItem(
        id = id, organizationId = UUID.randomUUID(), quantity = BigDecimal.TEN
    )

    private fun user(id: UUID, email: String) = User(
        id = id, organizationId = UUID.randomUUID(), username = "test", email = email, role = Role.CUSTOMER, status = UserState.ACTIVE
    )

    private fun catalogItem(id: UUID) = CatalogItem(
        id = id, itemType = "BEAM", designation = "Test"
    )

    private fun catalogFamily(id: UUID) = CatalogFamily(
        id = id, standard = "ISO", code = "HE", shapeType = "HEA"
    )

    private fun catalogItemType(id: UUID) = CatalogItemType(
        id = id, organizationId = UUID.randomUUID(), name = "Beam"
    )
}
