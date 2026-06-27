package com.blacksmith.metalstore.catalog

import com.blacksmith.metalstore.catalog.domain.entity.CatalogFamily
import com.blacksmith.metalstore.catalog.domain.entity.EuroProfile
import com.blacksmith.metalstore.shared.domain.MaterialType
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CatalogProfileWeightCalculationTest {

    private val family = CatalogFamily(standard = "EURO", code = "HEB", shapeType = "H", description = "HEB")

    @Test
    fun `steel profile weight is area times density divided by 10000`() {
        val profile = EuroProfile(family = family, designation = "HEB 200", areaCm2 = BigDecimal("78.1"))
        val expected = BigDecimal("78.1").multiply(BigDecimal("7850")).divide(BigDecimal("10000"), 4, RoundingMode.HALF_UP)
        assertEquals(expected, profile.getCalculatedWeightKgM())
    }

    @Test
    fun `aluminium profile uses 2700 density`() {
        val profile = EuroProfile(
            family = family, designation = "HEB 200",
            areaCm2 = BigDecimal("78.1"), weightKgM = null,
            materialType = MaterialType.ALUMINIUM
        )
        val expected = BigDecimal("78.1").multiply(BigDecimal("2700")).divide(BigDecimal("10000"), 4, RoundingMode.HALF_UP)
        assertEquals(expected, profile.getCalculatedWeightKgM())
    }

    @Test
    fun `stainless steel profile uses 7930 density`() {
        val profile = EuroProfile(
            family = family, designation = "HEB 200",
            areaCm2 = BigDecimal("78.1"), weightKgM = null,
            materialType = MaterialType.STAINLESS_STEEL
        )
        val expected = BigDecimal("78.1").multiply(BigDecimal("7930")).divide(BigDecimal("10000"), 4, RoundingMode.HALF_UP)
        assertEquals(expected, profile.getCalculatedWeightKgM())
    }

    @Test
    fun `galvanized profile uses 7850 density`() {
        val profile = EuroProfile(
            family = family, designation = "HEB 200",
            areaCm2 = BigDecimal("78.1"), weightKgM = null,
            materialType = MaterialType.GALVANIZED
        )
        val expected = BigDecimal("78.1").multiply(BigDecimal("7850")).divide(BigDecimal("10000"), 4, RoundingMode.HALF_UP)
        assertEquals(expected, profile.getCalculatedWeightKgM())
    }

    @Test
    fun `null areaCm2 returns null weight`() {
        val profile = EuroProfile(family = family, designation = "HEB 200", areaCm2 = null)
        assertNull(profile.getCalculatedWeightKgM())
    }

    @Test
    fun `null materialType defaults to steel`() {
        val profile = EuroProfile(
            family = family, designation = "HEB 200",
            areaCm2 = BigDecimal("78.1"), weightKgM = null
        )
        val steel = EuroProfile(
            family = family, designation = "HEB 200",
            areaCm2 = BigDecimal("78.1"), weightKgM = null,
            materialType = MaterialType.STEEL
        )
        assertEquals(steel.getCalculatedWeightKgM(), profile.getCalculatedWeightKgM())
    }
}
