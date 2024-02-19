package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewEvent
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewPerson
import uk.gov.justice.digital.hmpps.integrations.delius.courtappearance.Court
import uk.gov.justice.digital.hmpps.integrations.delius.courtappearance.CourtAppearance
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import java.time.LocalDate

object CourtAppearanceGenerator {
    var DEFAULT = generate(EventGenerator.CASE_VIEW.asEvent())

    fun generate(
        event: Event,
        type: ReferenceData = ReferenceDataGenerator.SENTENCE_APPEARANCE,
        court: Court = CourtGenerator.DEFAULT,
        date: LocalDate = LocalDate.now().minusMonths(5),
        id: Long = IdGenerator.getAndIncrement()
    ): CourtAppearance {
        return CourtAppearance(
            id,
            date,
            type,
            event,
            court,
            57L,
            false
        )
    }

    private fun CaseViewEvent.asEvent() = Event(id, number, PersonGenerator.CASE_VIEW.asPerson(), active, softDeleted)
    private fun CaseViewPerson.asPerson() = Person(id, crn, null, forename, secondName, thirdName, surname, softDeleted)
}
