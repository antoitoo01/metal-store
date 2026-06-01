package com.blacksmith.metalstore.auth.audit

import org.slf4j.LoggerFactory
import org.slf4j.Marker
import org.slf4j.MarkerFactory
import org.springframework.stereotype.Component

@Component
class AuditLogger {

    companion object {
        private val log = LoggerFactory.getLogger("AUDIT")
        val AUDIT_MARKER: Marker = MarkerFactory.getMarker("AUDIT")
    }

    data class AuditEvent(
        val action: String,
        val entityType: String,
        val entityId: String? = null,
        val tenantId: String? = null,
        val userId: String? = null,
        val details: Map<String, Any?>? = null
    )

    fun log(event: AuditEvent) {
        log.info(
            AUDIT_MARKER,
            "action={} entity={} id={} tenant={} user={} details={}",
            event.action, event.entityType, event.entityId,
            event.tenantId, event.userId, event.details
        )
    }

    fun warn(event: AuditEvent) {
        log.warn(
            AUDIT_MARKER,
            "action={} entity={} id={} tenant={} user={} details={}",
            event.action, event.entityType, event.entityId,
            event.tenantId, event.userId, event.details
        )
    }
}
