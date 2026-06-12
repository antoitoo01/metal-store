package com.blacksmith.metalstore.organization.domain.repository

import com.blacksmith.metalstore.organization.domain.entity.Organization
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OrganizationRepository : JpaRepository<Organization, UUID> {
    fun existsBySlug(slug: String): Boolean
}
