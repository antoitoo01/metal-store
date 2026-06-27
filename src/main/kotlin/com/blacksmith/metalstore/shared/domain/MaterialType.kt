package com.blacksmith.metalstore.shared.domain

import java.math.BigDecimal

enum class MaterialType(val densityKgM3: BigDecimal) {
    STEEL(BigDecimal("7850")),
    ALUMINIUM(BigDecimal("2700")),
    STAINLESS_STEEL(BigDecimal("7930")),
    GALVANIZED(BigDecimal("7850"))
}
