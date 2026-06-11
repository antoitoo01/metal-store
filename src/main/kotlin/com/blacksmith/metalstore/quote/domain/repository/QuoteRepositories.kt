package com.blacksmith.metalstore.quote.domain.repository

import com.blacksmith.metalstore.quote.domain.entity.Quote
import com.blacksmith.metalstore.quote.domain.entity.QuoteLine
import com.blacksmith.metalstore.quote.domain.entity.QuoteStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface QuoteRepository : JpaRepository<Quote, UUID> {
    fun findByTenantIdOrderByIssueDateDesc(tenantId: UUID, pageable: Pageable): Page<Quote>
    fun findByTenantIdAndStatus(tenantId: UUID, status: QuoteStatus): List<Quote>
    fun findByTenantIdAndClientIdOrderByIssueDateDesc(tenantId: UUID, clientId: UUID, pageable: Pageable): Page<Quote>
    fun countByTenantId(tenantId: UUID): Long
}

@Repository
interface QuoteLineRepository : JpaRepository<QuoteLine, UUID> {
    fun findByQuoteId(quoteId: UUID): List<QuoteLine>
    fun findByQuoteIdOrderByLineNumber(quoteId: UUID): List<QuoteLine>
}
