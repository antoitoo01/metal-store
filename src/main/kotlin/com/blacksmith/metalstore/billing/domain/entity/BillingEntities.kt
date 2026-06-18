package com.blacksmith.metalstore.billing.domain.entity

import com.blacksmith.metalstore.shared.BaseEntity
import jakarta.persistence.*
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Objects
import java.util.UUID

@Entity
@Table(
    name = "price_list",
    indexes = [
        Index(name = "idx_price_tenant", columnList = "organization_id"),
        Index(name = "idx_price_profile", columnList = "profile_id"),
        Index(name = "idx_price_item", columnList = "item_id")
    ]
)
class PriceListItem(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val organizationId: UUID,

    val profileId: UUID? = null,

    val itemId: UUID? = null,

    @Column(nullable = false, precision = 12, scale = 4)
    val unitPrice: BigDecimal,

    val validFrom: LocalDate? = null,

    val validTo: LocalDate? = null,

    @Column(columnDefinition = "TEXT")
    val notes: String? = null
) : BaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as PriceListItem
        return id == that.id
    }

    override fun hashCode(): Int = Objects.hash(id)
}

enum class InvoiceStatus {
    DRAFT, ISSUED, PAID, CANCELLED;

    fun canTransitionTo(target: InvoiceStatus): Boolean = when (this) {
        DRAFT -> target == ISSUED || target == CANCELLED
        ISSUED -> target == PAID || target == CANCELLED
        PAID, CANCELLED -> false
    }
}

@Entity
@Table(
    name = "invoices",
    indexes = [
        Index(name = "idx_invoice_tenant", columnList = "organization_id"),
        Index(name = "idx_invoice_status", columnList = "status")
    ]
)
class Invoice(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val organizationId: UUID,

    @Column(nullable = false, unique = true, length = 50)
    val invoiceNumber: String,

    @Column(length = 255)
    val customerName: String? = null,
    @Column(length = 20)
    val customerVat: String? = null,
    @Column(columnDefinition = "TEXT")
    val customerAddress: String? = null,

    @Column(nullable = false)
    val issueDate: LocalDate = LocalDate.now(),

    val dueDate: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: InvoiceStatus = InvoiceStatus.DRAFT,

    @Column(precision = 14, scale = 4)
    val subtotal: BigDecimal = BigDecimal.ZERO,

    @Column(precision = 14, scale = 4)
    val vatTotal: BigDecimal = BigDecimal.ZERO,

    @Column(precision = 14, scale = 4)
    val total: BigDecimal = BigDecimal.ZERO,

    @Column(columnDefinition = "TEXT")
    val notes: String? = null
) : BaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as Invoice
        return id == that.id
    }

    override fun hashCode(): Int = Objects.hash(id)
}

@Entity
@Table(
    name = "invoice_lines",
    indexes = [
        Index(name = "idx_line_invoice", columnList = "invoice_id")
    ]
)
class InvoiceLine(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val invoiceId: UUID,

    @Column(nullable = false)
    val lineNumber: Int,

    val profileId: UUID? = null,
    val itemId: UUID? = null,

    @Column(nullable = false, length = 500)
    val description: String,

    @field:Positive
    @Column(nullable = false, precision = 12, scale = 4)
    val quantity: BigDecimal,

    @field:Positive
    @Column(nullable = false, precision = 12, scale = 4)
    val unitPrice: BigDecimal,

    @Column(precision = 5, scale = 2)
    val vatRate: BigDecimal = BigDecimal("21.00"),

    @Column(nullable = false, precision = 14, scale = 4)
    val totalPrice: BigDecimal
) : BaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as InvoiceLine
        return id == that.id
    }

    override fun hashCode(): Int = Objects.hash(id)
}
