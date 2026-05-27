package com.blacksmith.metalstore.catalog.domain.repository

import com.blacksmith.metalstore.catalog.domain.entity.EuroProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface EuroProfileRepository : JpaRepository<EuroProfile, UUID> {
    fun findByDesignationContainingIgnoreCase(designation: String): List<EuroProfile>
}
