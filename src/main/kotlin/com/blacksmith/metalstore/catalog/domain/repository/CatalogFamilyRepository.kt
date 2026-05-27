package com.blacksmith.metalstore.catalog.domain.repository

import com.blacksmith.metalstore.catalog.domain.entity.CatalogFamily
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CatalogFamilyRepository : JpaRepository<CatalogFamily, UUID> {
    fun findByStandardAndCode(standard: String, code: String): Optional<CatalogFamily>
    fun findByStandard(standard: String): List<CatalogFamily>
}
