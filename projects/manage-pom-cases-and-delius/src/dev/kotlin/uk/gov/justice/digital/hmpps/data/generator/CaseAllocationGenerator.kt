package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.EventGenerator.generateDisposal
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator.generateEvent
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.CaseAllocation
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceData
import java.time.ZonedDateTime

object CaseAllocationGenerator {
    val DEFAULT_CA = generateCaseAllocation(
        generateDisposal(generateEvent()).event,
        PersonGenerator.DEFAULT,
        ReferenceDataGenerator.DECISION_NORMAL,
        ZonedDateTime.now().minusDays(3)
    )

    val INACTIVE_CA = generateCaseAllocation(
        generateDisposal(generateEvent(), active = false).event,
        PersonGenerator.DEFAULT,
        ReferenceDataGenerator.DECISION_ENHANCED,
        ZonedDateTime.now().minusDays(2)
    )

    val PREVIOUS_CA = generateCaseAllocation(
        generateDisposal(generateEvent()).event,
        PersonGenerator.DEFAULT,
        ReferenceDataGenerator.DECISION_NOT_ASSESSED,
        ZonedDateTime.now().minusDays(5)
    )

    val ALL = listOf(DEFAULT_CA, INACTIVE_CA, PREVIOUS_CA)

    fun generateCaseAllocation(
        event: Event,
        person: Person,
        decision: ReferenceData? = null,
        decisionDate: ZonedDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = CaseAllocation(person, event, decision, decisionDate, id)
}
