package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.event.nsi.NsiStatus
import uk.gov.justice.digital.hmpps.integrations.delius.service.toOutcomeRecall
import uk.gov.justice.digital.hmpps.integrations.delius.service.toRecallRejectedOrWithdrawn

class InterventionServiceTest {

    @Test
    fun `toRecallRejectedOrWithdrawn returns true `() {

        val nsiStatus = NsiStatus(IdGenerator.getAndIncrement(), "REC05", "REC05")
        val nsi = SentenceGenerator.generateBreachNsi(SentenceGenerator.CURRENT_SENTENCE, status = nsiStatus)
        assertEquals(true, nsi.toRecallRejectedOrWithdrawn())
    }

    @Test
    fun `toRecallRejectedOrWithdrawn returns false due to no outcome`() {

        val nsiStatus = NsiStatus(IdGenerator.getAndIncrement(), "REC03", "REC03")
        val nsi =
            SentenceGenerator.generateBreachNsi(SentenceGenerator.CURRENT_SENTENCE, status = nsiStatus, outcome = null)
        assertEquals(false, nsi.toRecallRejectedOrWithdrawn())
    }

    @Test
    fun `toRecallRejectedOrWithdrawn returns null due to other status enum`() {

        val nsiStatus = NsiStatus(IdGenerator.getAndIncrement(), "OTHER", "OTHER")
        val nsi =
            SentenceGenerator.generateBreachNsi(SentenceGenerator.CURRENT_SENTENCE, status = nsiStatus, outcome = null)
        assertEquals(null, nsi.toRecallRejectedOrWithdrawn())
    }

    @Test
    fun `toRecallRejectedOrWithdrawn returns true due to outcome type enum`() {

        val nsiStatus = NsiStatus(IdGenerator.getAndIncrement(), "REC03", "REC03")
        val outcome = ReferenceData("REC02", "REC02", IdGenerator.getAndIncrement())
        val nsi = SentenceGenerator.generateBreachNsi(
            SentenceGenerator.CURRENT_SENTENCE,
            status = nsiStatus,
            outcome = outcome
        )
        assertEquals(true, nsi.toRecallRejectedOrWithdrawn())
    }

    @Test
    fun `toOutcome returns null due to null outcome`() {

        val nsiStatus = NsiStatus(IdGenerator.getAndIncrement(), "REC03", "REC03")
        val nsi =
            SentenceGenerator.generateBreachNsi(SentenceGenerator.CURRENT_SENTENCE, status = nsiStatus, outcome = null)
        assertEquals(null, nsi.toOutcomeRecall())
    }

    @Test
    fun `toOutcome returns false due to outcome type`() {

        val nsiStatus = NsiStatus(IdGenerator.getAndIncrement(), "REC03", "REC03")
        val outcome = ReferenceData("REC02", "REC02", IdGenerator.getAndIncrement())
        val nsi = SentenceGenerator.generateBreachNsi(
            SentenceGenerator.CURRENT_SENTENCE,
            status = nsiStatus,
            outcome = outcome
        )
        assertEquals(false, nsi.toOutcomeRecall())
    }

    @Test
    fun `toOutcome returns true due to outcome type`() {

        val nsiStatus = NsiStatus(IdGenerator.getAndIncrement(), "REC03", "REC03")
        val outcome = ReferenceData("REC03", "REC03", IdGenerator.getAndIncrement())
        val nsi = SentenceGenerator.generateBreachNsi(
            SentenceGenerator.CURRENT_SENTENCE,
            status = nsiStatus,
            outcome = outcome
        )
        assertEquals(true, nsi.toOutcomeRecall())
    }
}

