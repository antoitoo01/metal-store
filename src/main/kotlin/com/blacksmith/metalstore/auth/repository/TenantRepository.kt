package com.blacksmith.metalstore.auth.repository

import com.blacksmith.metalstore.auth.domain.entity.Tenant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TenantRepository : JpaRepository<Tenant, UUID> {
    fun findBySlug(slug: String): Optional<Tenant>
    fun existsBySlug(slug: String): Boolean
}
