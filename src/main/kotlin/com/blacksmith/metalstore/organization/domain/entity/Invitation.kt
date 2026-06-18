package com.blacksmith.metalstore.organization.domain.entity

import com.blacksmith.metalstore.shared.BaseEntity
import jakarta.persistence.*
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Objects
import java.util.UUID

@Entity
@Table(name = "invitations")
class Invitation(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true)
    var token: String = UUID.randomUUID().toString(),

    @Column(name = "organization_id", nullable = false)
    var organizationId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: OrganizationRole = OrganizationRole.VIEWER,

    @Column(nullable = false)
    var email: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: InvitationStatus = InvitationStatus.PENDING,

    @Column(name = "created_by", nullable = false)
    var createdBy: UUID,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant = Instant.now().plus(1, ChronoUnit.HOURS),
) : BaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as Invitation
        return id == that.id
    }

    override fun hashCode(): Int = Objects.hash(id)
}
