package com.blacksmith.metalstore.quote.domain.dto.request

import com.blacksmith.metalstore.quote.domain.entity.Quote
import java.time.LocalDate
import java.util.UUID

data class CreateQuoteRequest(
    val clientId: UUID? = null,
    val customerName: String? = null,
    val customerVat: String? = null,
    val customerAddress: String? = null,
    val validUntil: LocalDate? = null,
    val notes: String? = null
) {
    fun toEntity(tenantId: UUID, quoteNumber: String) = Quote(
        tenantId = tenantId,
        quoteNumber = quoteNumber,
        clientId = clientId,
        customerName = customerName,
        customerVat = customerVat,
        customerAddress = customerAddress,
        validUntil = validUntil,
        notes = notes
    )
}
