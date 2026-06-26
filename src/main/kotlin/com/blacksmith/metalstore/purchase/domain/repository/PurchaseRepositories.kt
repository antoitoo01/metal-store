package com.blacksmith.metalstore.purchase.domain.repository

import com.blacksmith.metalstore.purchase.domain.entity.PurchaseOrder
import com.blacksmith.metalstore.purchase.domain.entity.PurchaseOrderLine
import com.blacksmith.metalstore.purchase.domain.entity.PurchaseOrderStatus
import com.blacksmith.metalstore.purchase.domain.entity.Supplier
import com.blacksmith.metalstore.purchase.domain.entity.SupplierStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface SupplierRepository : JpaRepository<Supplier, UUID> {
    fun findByOrganizationId(organizationId: UUID, pageable: Pageable): Page<Supplier>
    fun findByOrganizationIdAndStatus(organizationId: UUID, status: SupplierStatus, pageable: Pageable): Page<Supplier>
    fun findByOrganizationIdAndNameContainingIgnoreCase(organizationId: UUID, name: String, pageable: Pageable): Page<Supplier>
    fun findByOrganizationIdAndNameContainingIgnoreCaseAndStatus(organizationId: UUID, name: String, status: SupplierStatus, pageable: Pageable): Page<Supplier>
    fun findByOrganizationIdAndVatNumber(organizationId: UUID, vatNumber: String): Supplier?
}

@Repository
interface PurchaseOrderRepository : JpaRepository<PurchaseOrder, UUID> {
    @Query("""
        SELECT po FROM PurchaseOrder po
        WHERE po.organizationId = :orgId
        AND (:q = '' OR LOWER(po.poNumber) LIKE CONCAT('%', :q, '%')
          OR LOWER(po.supplierName) LIKE CONCAT('%', :q, '%'))
        AND (:status IS NULL OR po.status = :status)
        AND (:supplierId IS NULL OR po.supplierId = :supplierId)
        ORDER BY po.issueDate DESC
    """)
    fun findAllFiltered(
        @Param("orgId") orgId: UUID,
        @Param("q") q: String,
        @Param("status") status: PurchaseOrderStatus?,
        @Param("supplierId") supplierId: UUID?,
        pageable: Pageable
    ): Page<PurchaseOrder>
    fun findByOrganizationIdAndStatus(organizationId: UUID, status: PurchaseOrderStatus): List<PurchaseOrder>
    fun countByOrganizationId(organizationId: UUID): Long
}

@Repository
interface PurchaseOrderLineRepository : JpaRepository<PurchaseOrderLine, UUID> {
    fun findByPoId(poId: UUID): List<PurchaseOrderLine>
    fun findByPoIdOrderByLineNumber(poId: UUID): List<PurchaseOrderLine>
}
