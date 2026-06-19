package com.blacksmith.metalstore.billing.domain.repository

import com.blacksmith.metalstore.billing.domain.entity.Invoice
import com.blacksmith.metalstore.billing.domain.entity.InvoiceLine
import com.blacksmith.metalstore.billing.domain.entity.InvoiceStatus
import com.blacksmith.metalstore.billing.domain.entity.PriceListItem
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PriceListRepository : JpaRepository<PriceListItem, UUID> {
    fun findByOrganizationId(organizationId: UUID, pageable: Pageable): Page<PriceListItem>
    fun findByOrganizationIdAndProfileId(organizationId: UUID, profileId: UUID): List<PriceListItem>
    fun findByOrganizationIdAndItemId(organizationId: UUID, itemId: UUID): List<PriceListItem>
}

@Repository
interface InvoiceRepository : JpaRepository<Invoice, UUID> {
    fun findByOrganizationIdAndStatus(organizationId: UUID, status: InvoiceStatus): List<Invoice>
    fun countByOrganizationId(organizationId: UUID): Long

    @Query("""
        SELECT i FROM Invoice i
        WHERE i.organizationId = :orgId
        AND (:q = '' OR LOWER(i.customerName) LIKE CONCAT('%', :q, '%')
          OR LOWER(i.invoiceNumber) LIKE CONCAT('%', :q, '%'))
        AND (:status IS NULL OR i.status = :status)
        ORDER BY i.issueDate DESC
    """)
    fun findAllFiltered(
        @Param("orgId") orgId: UUID,
        @Param("q") q: String,
        @Param("status") status: InvoiceStatus?,
        pageable: Pageable
    ): Page<Invoice>
}

@Repository
interface InvoiceLineRepository : JpaRepository<InvoiceLine, UUID> {
    fun findByInvoiceId(invoiceId: UUID): List<InvoiceLine>
    fun findByInvoiceIdOrderByLineNumber(invoiceId: UUID): List<InvoiceLine>
}
