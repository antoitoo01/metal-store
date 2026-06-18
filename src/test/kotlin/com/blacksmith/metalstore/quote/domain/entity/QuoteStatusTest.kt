package com.blacksmith.metalstore.quote.domain.entity

import org.junit.jupiter.api.Test

class QuoteStatusTest {

    @Test
    fun `DRAFT can transition to ISSUED`() {
        assert(QuoteStatus.DRAFT.canTransitionTo(QuoteStatus.ISSUED))
    }

    @Test
    fun `DRAFT can transition to CANCELLED`() {
        assert(QuoteStatus.DRAFT.canTransitionTo(QuoteStatus.CANCELLED))
    }

    @Test
    fun `DRAFT cannot transition to ACCEPTED`() {
        assert(!QuoteStatus.DRAFT.canTransitionTo(QuoteStatus.ACCEPTED))
    }

    @Test
    fun `DRAFT cannot transition to REJECTED`() {
        assert(!QuoteStatus.DRAFT.canTransitionTo(QuoteStatus.REJECTED))
    }

    @Test
    fun `DRAFT cannot transition to DRAFT`() {
        assert(!QuoteStatus.DRAFT.canTransitionTo(QuoteStatus.DRAFT))
    }

    @Test
    fun `ISSUED can transition to ACCEPTED`() {
        assert(QuoteStatus.ISSUED.canTransitionTo(QuoteStatus.ACCEPTED))
    }

    @Test
    fun `ISSUED can transition to REJECTED`() {
        assert(QuoteStatus.ISSUED.canTransitionTo(QuoteStatus.REJECTED))
    }

    @Test
    fun `ISSUED can transition to CANCELLED`() {
        assert(QuoteStatus.ISSUED.canTransitionTo(QuoteStatus.CANCELLED))
    }

    @Test
    fun `ISSUED cannot transition to DRAFT`() {
        assert(!QuoteStatus.ISSUED.canTransitionTo(QuoteStatus.DRAFT))
    }

    @Test
    fun `ACCEPTED is a terminal state`() {
        for (target in QuoteStatus.entries) {
            assert(!QuoteStatus.ACCEPTED.canTransitionTo(target))
        }
    }

    @Test
    fun `REJECTED is a terminal state`() {
        for (target in QuoteStatus.entries) {
            assert(!QuoteStatus.REJECTED.canTransitionTo(target))
        }
    }

    @Test
    fun `CANCELLED is a terminal state`() {
        for (target in QuoteStatus.entries) {
            assert(!QuoteStatus.CANCELLED.canTransitionTo(target))
        }
    }
}
