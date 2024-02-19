package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewEvent
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person

object EventGenerator {
    val DEFAULT = generate(eventNumber = "1")
    val NEW = generate(eventNumber = "2")
    val HISTORIC = generate(eventNumber = "3")
    val DELETED = generate(eventNumber = "1", softDeleted = true)
    val INACTIVE = generate(eventNumber = "99", active = false)

    val CASE_VIEW = forCaseView()

    fun generate(
        person: Person = PersonGenerator.DEFAULT,
        eventNumber: String = "1",
        id: Long = IdGenerator.getAndIncrement(),
        active: Boolean = true,
        softDeleted: Boolean = false
    ) = Event(id, eventNumber, person, active, softDeleted)

    private fun forCaseView(
        personId: Long = PersonGenerator.CASE_VIEW.id,
        eventNumber: String = "1",
        id: Long = IdGenerator.getAndIncrement(),
        active: Boolean = true,
        softDeleted: Boolean = false
    ) = CaseViewEvent(id, personId, eventNumber, active, softDeleted)
}
