package com.blacksmith.metalstore.client.domain.entity

import com.blacksmith.metalstore.shared.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
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
data class Client(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val organizationId: UUID,

    @Column(nullable = false)
    val name: String,

    @JdbcTypeCode(SqlTypes.VARCHAR)
    val email: String? = null,

    @JdbcTypeCode(SqlTypes.VARCHAR)
    val phone: String? = null,

    @Column(length = 500)
    val address: String? = null,

    @JdbcTypeCode(SqlTypes.VARCHAR)
    val vatNumber: String? = null,

    @Column(length = 2000)
    val notes: String? = null,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val status: ClientStatus = ClientStatus.ACTIVE
) : BaseEntity()
