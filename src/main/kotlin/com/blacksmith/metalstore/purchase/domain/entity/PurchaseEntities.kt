package com.blacksmith.metalstore.purchase.domain.entity

import com.blacksmith.metalstore.shared.BaseEntity
import jakarta.persistence.*
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Objects
import java.util.UUID

enum class SupplierStatus { ACTIVE, INACTIVE }

@Entity
@Table(
    name = "suppliers",
    indexes = [
        Index(name = "idx_supplier_tenant", columnList = "organization_id"),
        Index(name = "idx_supplier_name", columnList = "organization_id, name")
    ]
)
class Supplier(
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
    val status: SupplierStatus = SupplierStatus.ACTIVE
) : BaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as Supplier
        return id == that.id
    }

    override fun hashCode(): Int = Objects.hash(id)
}

enum class PurchaseOrderStatus {
    DRAFT, ISSUED, RECEIVED, CANCELLED;

    fun canTransitionTo(target: PurchaseOrderStatus): Boolean = when (this) {
        DRAFT -> target == ISSUED || target == CANCELLED
        ISSUED -> target == RECEIVED || target == CANCELLED
        RECEIVED, CANCELLED -> false
    }
}

@Entity
@Table(
    name = "purchase_orders",
    indexes = [
        Index(name = "idx_po_tenant", columnList = "organization_id"),
        Index(name = "idx_po_status", columnList = "status")
    ]
)
class PurchaseOrder(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val organizationId: UUID,

    @Column(nullable = false, unique = true, length = 50)
    val poNumber: String,

    val supplierId: UUID? = null,

    @Column(length = 255)
    val supplierName: String? = null,

    @Column(length = 20)
    val supplierVat: String? = null,

    @Column(columnDefinition = "TEXT")
    val supplierAddress: String? = null,

    @Column(nullable = false)
    val issueDate: LocalDate = LocalDate.now(),

    val expectedDate: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: PurchaseOrderStatus = PurchaseOrderStatus.DRAFT,

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
        val that = other as PurchaseOrder
        return id == that.id
    }

    override fun hashCode(): Int = Objects.hash(id)
}

@Entity
@Table(
    name = "purchase_order_lines",
    indexes = [
        Index(name = "idx_poline_po", columnList = "purchase_order_id")
    ]
)
class PurchaseOrderLine(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "purchase_order_id", nullable = false)
    val poId: UUID,

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
        val that = other as PurchaseOrderLine
        return id == that.id
    }

    override fun hashCode(): Int = Objects.hash(id)
}
