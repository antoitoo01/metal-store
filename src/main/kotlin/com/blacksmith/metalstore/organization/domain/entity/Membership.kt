package com.blacksmith.metalstore.organization.domain.entity

import com.blacksmith.metalstore.shared.BaseEntity
import jakarta.persistence.*
import java.util.Objects
import java.util.UUID

@Entity
@Table(
    name = "memberships",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "organization_id"])],
    indexes = [
        Index(name = "idx_membership_org", columnList = "organization_id"),
        Index(name = "idx_membership_user", columnList = "user_id"),
    ]
)
class Membership(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    var userId: UUID,

    @Column(name = "organization_id", nullable = false)
    var organizationId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: OrganizationRole,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: MembershipStatus = MembershipStatus.ACTIVE,

    @Column(name = "invited_by")
    var invitedBy: UUID? = null,
) : BaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as Membership
        return id == that.id
    }

    override fun hashCode(): Int = Objects.hash(id)
}
