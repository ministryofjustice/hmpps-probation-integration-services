package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.entity.DisposalType
import java.time.LocalDate

object SentenceGenerator {
    fun generateSentence(event: Event, type: DisposalType, startDate: LocalDate, endDate: LocalDate): Disposal {
        return Disposal(
            id = IdGenerator.getAndIncrement(),
            type = type,
            event = event,
            custody = null,
            startDate = startDate,
            endDate = endDate,
            enteredSentenceEndDate = null,
            active = true,
            softDeleted = false
        )
    }
}