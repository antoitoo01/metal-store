package com.blacksmith.metalstore.client.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

enum class ClientStatus { ACTIVE, INACTIVE }

@Entity
@Table(
    name = "clients",
    indexes = [
        Index(name = "idx_client_tenant", columnList = "tenantId"),
        Index(name = "idx_client_name", columnList = "tenantId, name")
    ]
)
data class Client(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val tenantId: UUID,

    @Column(nullable = false)
    val name: String,

    val email: String? = null,

    val phone: String? = null,

    @Column(length = 500)
    val address: String? = null,

    val vatNumber: String? = null,

    @Column(length = 2000)
    val notes: String? = null,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val status: ClientStatus = ClientStatus.ACTIVE,

    @Column(nullable = false, updatable = false)
    val createdDate: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val lastModifiedDate: LocalDateTime = LocalDateTime.now()
)
