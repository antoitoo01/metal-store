package com.blacksmith.metalstore.billing.domain.entity

import org.junit.jupiter.api.Test

class InvoiceStatusTest {

    @Test
    fun `DRAFT can transition to ISSUED`() {
        assert(InvoiceStatus.DRAFT.canTransitionTo(InvoiceStatus.ISSUED))
    }

    @Test
    fun `DRAFT can transition to CANCELLED`() {
        assert(InvoiceStatus.DRAFT.canTransitionTo(InvoiceStatus.CANCELLED))
    }

    @Test
    fun `DRAFT cannot transition to PAID`() {
        assert(!InvoiceStatus.DRAFT.canTransitionTo(InvoiceStatus.PAID))
    }

    @Test
    fun `DRAFT cannot transition to DRAFT`() {
        assert(!InvoiceStatus.DRAFT.canTransitionTo(InvoiceStatus.DRAFT))
    }

    @Test
    fun `ISSUED can transition to PAID`() {
        assert(InvoiceStatus.ISSUED.canTransitionTo(InvoiceStatus.PAID))
    }

    @Test
    fun `ISSUED can transition to CANCELLED`() {
        assert(InvoiceStatus.ISSUED.canTransitionTo(InvoiceStatus.CANCELLED))
    }

    @Test
    fun `ISSUED cannot transition to DRAFT`() {
        assert(!InvoiceStatus.ISSUED.canTransitionTo(InvoiceStatus.DRAFT))
    }

    @Test
    fun `PAID is a terminal state`() {
        for (target in InvoiceStatus.entries) {
            assert(!InvoiceStatus.PAID.canTransitionTo(target))
        }
    }

    @Test
    fun `CANCELLED is a terminal state`() {
        for (target in InvoiceStatus.entries) {
            assert(!InvoiceStatus.CANCELLED.canTransitionTo(target))
        }
    }
}
