package com.blacksmith.metalstore.outbound.domain.entity

import com.blacksmith.metalstore.shared.BaseEntity
import jakarta.persistence.*
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Objects
import java.util.UUID

enum class OutboundDeliveryNoteStatus {
    DRAFT, CONFIRMED, CANCELLED;

    fun canTransitionTo(target: OutboundDeliveryNoteStatus): Boolean = when (this) {
        DRAFT -> target == CONFIRMED || target == CANCELLED
        CONFIRMED, CANCELLED -> false
    }
}

@Entity
@Table(
    name = "outbound_delivery_notes",
    indexes = [
        Index(name = "idx_odn_tenant", columnList = "organization_id"),
        Index(name = "idx_odn_status", columnList = "status")
    ]
)
class OutboundDeliveryNote(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val organizationId: UUID,

    @Column(nullable = false, unique = true, length = 50)
    val number: String,

    val customerId: UUID? = null,

    @Column(length = 255)
    val customerName: String? = null,

    @Column(length = 20)
    val customerVat: String? = null,

    @Column(length = 500)
    val customerAddress: String? = null,

    @Column(nullable = false)
    val issueDate: LocalDate = LocalDate.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: OutboundDeliveryNoteStatus = OutboundDeliveryNoteStatus.DRAFT,

    @Column(precision = 14, scale = 4)
    val totalAmount: BigDecimal = BigDecimal.ZERO,

    @Column(columnDefinition = "TEXT")
    val notes: String? = null
) : BaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as OutboundDeliveryNote
        return id == that.id
    }

    override fun hashCode(): Int = Objects.hash(id)
}

@Entity
@Table(
    name = "outbound_delivery_note_lines",
    indexes = [
        Index(name = "idx_odnline_note", columnList = "delivery_note_id")
    ]
)
class OutboundDeliveryNoteLine(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "delivery_note_id", nullable = false)
    val deliveryNoteId: UUID,

    @Column(nullable = false)
    val lineNumber: Int,

    val profileId: UUID? = null,

    val itemId: UUID? = null,

    @Column(nullable = false, length = 500)
    val description: String,

    @field:Positive
    @Column(nullable = false, precision = 12, scale = 4)
    val quantity: BigDecimal,

    @field:PositiveOrZero
    @Column(precision = 12, scale = 4)
    val unitPrice: BigDecimal = BigDecimal.ZERO,

    @Column(precision = 5, scale = 2)
    val vatRate: BigDecimal = BigDecimal("21.00"),

    @Column(columnDefinition = "TEXT")
    val notes: String? = null
) : BaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as OutboundDeliveryNoteLine
        return id == that.id
    }

    override fun hashCode(): Int = Objects.hash(id)
}
