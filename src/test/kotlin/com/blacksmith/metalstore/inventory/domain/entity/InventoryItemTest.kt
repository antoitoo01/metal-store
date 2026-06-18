package com.blacksmith.metalstore.inventory.domain.entity

import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

class InventoryItemTest {

    @Test
    fun `valid when profileId set and itemId null`() {
        val item = InventoryItem(
            organizationId = UUID.randomUUID(),
            quantity = BigDecimal.TEN,
            profileId = UUID.randomUUID()
        )
        assert(item.assertValidSource())
    }

    @Test
    fun `valid when itemId set and profileId null`() {
        val item = InventoryItem(
            organizationId = UUID.randomUUID(),
            quantity = BigDecimal.TEN,
            itemId = UUID.randomUUID()
        )
        assert(item.assertValidSource())
    }

    @Test
    fun `invalid when both profileId and itemId are null`() {
        val item = InventoryItem(
            organizationId = UUID.randomUUID(),
            quantity = BigDecimal.TEN
        )
        assert(!item.assertValidSource())
    }

    @Test
    fun `invalid when both profileId and itemId are set`() {
        val item = InventoryItem(
            organizationId = UUID.randomUUID(),
            quantity = BigDecimal.TEN,
            profileId = UUID.randomUUID(),
            itemId = UUID.randomUUID()
        )
        assert(!item.assertValidSource())
    }
}
