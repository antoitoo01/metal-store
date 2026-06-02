package com.blacksmith.metalstore.quote.domain.entity

import com.blacksmith.metalstore.shared.BaseEntity
import jakarta.persistence.*
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

enum class QuoteStatus {
    DRAFT, ISSUED, ACCEPTED, REJECTED, CANCELLED
}

@Entity
@Table(
    name = "quotes",
    indexes = [
        Index(name = "idx_quote_tenant", columnList = "tenantId"),
        Index(name = "idx_quote_status", columnList = "status")
    ]
)
data class Quote(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val tenantId: UUID,

    @Column(nullable = false, unique = true)
    val quoteNumber: String,

    val clientId: UUID? = null,

    val customerName: String? = null,
    val customerVat: String? = null,
    val customerAddress: String? = null,

    @Column(nullable = false)
    val issueDate: LocalDate = LocalDate.now(),

    val validUntil: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: QuoteStatus = QuoteStatus.DRAFT,

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
    name = "quote_lines",
    indexes = [
        Index(name = "idx_qline_quote", columnList = "quoteId")
    ]
)
data class QuoteLine(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val quoteId: UUID,

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
