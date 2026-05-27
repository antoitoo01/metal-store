package com.blacksmith.metalstore.catalog.domain.repository

import com.blacksmith.metalstore.catalog.domain.entity.AiscProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AiscProfileRepository : JpaRepository<AiscProfile, UUID> {
    fun findByDesignationContainingIgnoreCase(designation: String): List<AiscProfile>
}
