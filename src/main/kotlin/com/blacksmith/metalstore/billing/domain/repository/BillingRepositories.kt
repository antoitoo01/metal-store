package com.blacksmith.metalstore.billing.domain.repository

import com.blacksmith.metalstore.billing.domain.entity.Invoice
import com.blacksmith.metalstore.billing.domain.entity.InvoiceLine
import com.blacksmith.metalstore.billing.domain.entity.InvoiceStatus
import com.blacksmith.metalstore.billing.domain.entity.PriceListItem
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PriceListRepository : JpaRepository<PriceListItem, UUID> {
    fun findByTenantId(tenantId: UUID, pageable: Pageable): Page<PriceListItem>
    fun findByTenantIdAndProfileId(tenantId: UUID, profileId: UUID): List<PriceListItem>
    fun findByTenantIdAndItemId(tenantId: UUID, itemId: UUID): List<PriceListItem>
}

@Repository
interface InvoiceRepository : JpaRepository<Invoice, UUID> {
    fun findByTenantIdOrderByIssueDateDesc(tenantId: UUID, pageable: Pageable): Page<Invoice>
    fun findByTenantIdAndStatus(tenantId: UUID, status: InvoiceStatus): List<Invoice>
    fun countByTenantId(tenantId: UUID): Long
}

@Repository
interface InvoiceLineRepository : JpaRepository<InvoiceLine, UUID> {
    fun findByInvoiceId(invoiceId: UUID): List<InvoiceLine>
    fun findByInvoiceIdOrderByLineNumber(invoiceId: UUID): List<InvoiceLine>
}
