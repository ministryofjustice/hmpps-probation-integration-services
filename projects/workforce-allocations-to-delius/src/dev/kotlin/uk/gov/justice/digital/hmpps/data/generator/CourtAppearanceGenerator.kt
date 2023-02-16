package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
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
        court: Court = CourtGenerator.DEFAULT,
        date: LocalDate = LocalDate.now().minusMonths(5),
        id: Long = IdGenerator.getAndIncrement()
    ): CourtAppearance {
        return CourtAppearance(
            id,
            date,
            event,
            court,
            false
        )
    }

    private fun CaseViewEvent.asEvent() = Event(id, number, PersonGenerator.CASE_VIEW.asPerson(), active, softDeleted)
    private fun CaseViewPerson.asPerson() = Person(id, crn, null, forename, secondName, thirdName, surname, softDeleted)
}
