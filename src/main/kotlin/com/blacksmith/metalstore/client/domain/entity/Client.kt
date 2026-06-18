package com.blacksmith.metalstore.client.domain.entity

import com.blacksmith.metalstore.shared.BaseEntity
import jakarta.persistence.*
import java.util.Objects
import java.util.UUID

enum class ClientStatus { ACTIVE, INACTIVE }

@Entity
@Table(
    name = "clients",
    indexes = [
        Index(name = "idx_client_tenant", columnList = "organization_id"),
        Index(name = "idx_client_name", columnList = "organization_id, name")
    ]
)
class Client(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val organizationId: UUID,

    @Column(nullable = false)
    val name: String,

    @Column(length = 255)
    val email: String? = null,

    @Column(length = 50)
    val phone: String? = null,

    @Column(length = 500)
    val address: String? = null,

    @Column(length = 20)
    val vatNumber: String? = null,

    @Column(length = 2000)
    val notes: String? = null,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val status: ClientStatus = ClientStatus.ACTIVE
) : BaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as Client
        return id == that.id
    }

    override fun hashCode(): Int = Objects.hash(id)
}
