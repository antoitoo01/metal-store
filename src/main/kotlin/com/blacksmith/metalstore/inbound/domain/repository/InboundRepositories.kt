package com.blacksmith.metalstore.inbound.domain.repository

import com.blacksmith.metalstore.inbound.domain.entity.InboundDeliveryNote
import com.blacksmith.metalstore.inbound.domain.entity.InboundDeliveryNoteLine
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface InboundDeliveryNoteRepository : JpaRepository<InboundDeliveryNote, UUID> {
    fun findByOrganizationId(organizationId: UUID, pageable: Pageable): Page<InboundDeliveryNote>
}

@Repository
interface InboundDeliveryNoteLineRepository : JpaRepository<InboundDeliveryNoteLine, UUID> {
    fun findByDeliveryNoteIdOrderByLineNumber(deliveryNoteId: UUID): List<InboundDeliveryNoteLine>
}
