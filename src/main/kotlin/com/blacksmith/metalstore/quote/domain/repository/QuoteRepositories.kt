package com.blacksmith.metalstore.quote.domain.repository

import com.blacksmith.metalstore.quote.domain.entity.Quote
import com.blacksmith.metalstore.quote.domain.entity.QuoteLine
import com.blacksmith.metalstore.quote.domain.entity.QuoteStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface QuoteRepository : JpaRepository<Quote, UUID> {
    @Query("""
        SELECT q FROM Quote q
        WHERE q.organizationId = :orgId
        AND (:q = '' OR LOWER(q.quoteNumber) LIKE CONCAT('%', :q, '%')
          OR LOWER(q.customerName) LIKE CONCAT('%', :q, '%'))
        AND (:status IS NULL OR q.status = :status)
        AND (:clientId IS NULL OR q.clientId = :clientId)
        ORDER BY q.issueDate DESC
    """)
    fun findAllFiltered(
        @Param("orgId") orgId: UUID,
        @Param("q") q: String,
        @Param("status") status: QuoteStatus?,
        @Param("clientId") clientId: UUID?,
        pageable: Pageable
    ): Page<Quote>

    fun findByOrganizationIdAndStatus(organizationId: UUID, status: QuoteStatus): List<Quote>
    fun countByOrganizationId(organizationId: UUID): Long
}

@Repository
interface QuoteLineRepository : JpaRepository<QuoteLine, UUID> {
    fun findByQuoteId(quoteId: UUID): List<QuoteLine>
    fun findByQuoteIdOrderByLineNumber(quoteId: UUID): List<QuoteLine>
}
