package com.blacksmith.metalstore.shared

import org.junit.jupiter.api.Test
import java.util.UUID

class NumberSequenceIdTest {

    private val orgId = UUID.randomUUID()

    @Test
    fun `equal when all fields match`() {
        val a = NumberSequenceId(orgId, "PRES", 2025)
        val b = NumberSequenceId(orgId, "PRES", 2025)
        assert(a == b)
    }

    @Test
    fun `hashCode equal when all fields match`() {
        val a = NumberSequenceId(orgId, "PRES", 2025)
        val b = NumberSequenceId(orgId, "PRES", 2025)
        assert(a.hashCode() == b.hashCode())
    }

    @Test
    fun `not equal when organizationId differs`() {
        val a = NumberSequenceId(orgId, "PRES", 2025)
        val b = NumberSequenceId(UUID.randomUUID(), "PRES", 2025)
        assert(a != b)
    }

    @Test
    fun `not equal when prefix differs`() {
        val a = NumberSequenceId(orgId, "PRES", 2025)
        val b = NumberSequenceId(orgId, "FAC", 2025)
        assert(a != b)
    }

    @Test
    fun `not equal when year differs`() {
        val a = NumberSequenceId(orgId, "PRES", 2025)
        val b = NumberSequenceId(orgId, "PRES", 2026)
        assert(a != b)
    }

    @Test
    fun `reflexive equals`() {
        val id = NumberSequenceId(orgId, "PRES", 2025)
        assert(id == id)
    }

    @Test
    fun `not equal to null`() {
        val id = NumberSequenceId(orgId, "PRES", 2025)
        assert(id != null)
    }
}
