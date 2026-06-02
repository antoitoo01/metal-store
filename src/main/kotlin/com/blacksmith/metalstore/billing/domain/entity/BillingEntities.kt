package com.blacksmith.metalstore.billing.domain.entity

import com.blacksmith.metalstore.shared.BaseEntity
import jakarta.persistence.*
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(
    name = "price_list",
    indexes = [
        Index(name = "idx_price_tenant", columnList = "tenantId"),
        Index(name = "idx_price_profile", columnList = "profileId"),
        Index(name = "idx_price_item", columnList = "itemId")
    ]
)
data class PriceListItem(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val tenantId: UUID,

    val profileId: UUID? = null,

    val itemId: UUID? = null,

    @Column(nullable = false, precision = 12, scale = 4)
    val unitPrice: BigDecimal,

    val validFrom: LocalDate? = null,

    val validTo: LocalDate? = null,

    val notes: String? = null
) : BaseEntity()

enum class InvoiceStatus {
    DRAFT, ISSUED, PAID, CANCELLED
}

@Entity
@Table(
    name = "invoices",
    indexes = [
        Index(name = "idx_invoice_tenant", columnList = "tenantId"),
        Index(name = "idx_invoice_status", columnList = "status")
    ]
)
data class Invoice(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val tenantId: UUID,

    @Column(nullable = false, unique = true)
    val invoiceNumber: String,

    val customerName: String? = null,
    val customerVat: String? = null,
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

    val notes: String? = null
) : BaseEntity()

@Entity
@Table(
    name = "invoice_lines",
    indexes = [
        Index(name = "idx_line_invoice", columnList = "invoiceId")
    ]
)
data class InvoiceLine(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val invoiceId: UUID,

    @Column(nullable = false)
    val lineNumber: Int,

    val profileId: UUID? = null,
    val itemId: UUID? = null,

    @Column(nullable = false)
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
) : BaseEntity()
