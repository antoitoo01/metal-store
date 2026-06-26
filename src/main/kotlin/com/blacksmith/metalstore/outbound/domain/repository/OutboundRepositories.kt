package com.blacksmith.metalstore.outbound.domain.repository

import com.blacksmith.metalstore.outbound.domain.entity.OutboundDeliveryNote
import com.blacksmith.metalstore.outbound.domain.entity.OutboundDeliveryNoteLine
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OutboundDeliveryNoteRepository : JpaRepository<OutboundDeliveryNote, UUID> {
    fun findByOrganizationId(organizationId: UUID, pageable: Pageable): Page<OutboundDeliveryNote>
}

@Repository
interface OutboundDeliveryNoteLineRepository : JpaRepository<OutboundDeliveryNoteLine, UUID> {
    fun findByDeliveryNoteIdOrderByLineNumber(deliveryNoteId: UUID): List<OutboundDeliveryNoteLine>
}
