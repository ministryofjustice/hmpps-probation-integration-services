package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.courtreport.CourtAppearance
import uk.gov.justice.digital.hmpps.integrations.delius.courtreport.CourtReport
import uk.gov.justice.digital.hmpps.integrations.delius.courtreport.Event
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person

object CourtReportGenerator {
    val DEFAULT_EVENT = generateEvent("1")
    val DEFAULT_CA = generateAppearance(DEFAULT_EVENT)
    val DEFAULT = generate(PersonGenerator.DEFAULT, DEFAULT_CA)

    fun generate(
        person: Person,
        courtAppearance: CourtAppearance,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = CourtReport(id, person, courtAppearance, softDeleted)

    fun generateAppearance(event: Event, softDeleted: Boolean = false, id: Long = IdGenerator.getAndIncrement()) =
        CourtAppearance(id, event, softDeleted)

    fun generateEvent(
        eventNumber: String,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Event(id, eventNumber, active, softDeleted)
}
