package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewEvent
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import java.time.LocalDate

object EventGenerator {
    val DEFAULT = generate(eventNumber = "1")
    val NEW = generate(eventNumber = "2")
    val HISTORIC = generate(eventNumber = "3")
    val DELETED = generate(eventNumber = "1", softDeleted = true)
    val HAS_INITIAL_ALLOCATION = generate(eventNumber = "4")
    val INACTIVE = generate(eventNumber = "99", active = false)
    val REALLOCATION = generate(eventNumber = "5")

    val CASE_VIEW = forCaseView()

    fun generate(
        person: Person = PersonGenerator.DEFAULT,
        eventNumber: String = "1",
        id: Long = IdGenerator.getAndIncrement(),
        referralDate: LocalDate = LocalDate.now().minusMonths(6),
        failureToComplyCount: Int = 0,
        breachEndDate: LocalDate? = null,
        active: Boolean = true,
        softDeleted: Boolean = false
    ) = Event(id, eventNumber, person, referralDate, failureToComplyCount, breachEndDate, active, softDeleted)

    private fun forCaseView(
        personId: Long = PersonGenerator.CASE_VIEW.id,
        eventNumber: String = "10",
        id: Long = IdGenerator.getAndIncrement(),
        failureToComplyCount: Int = 2,
        breachEndDate: LocalDate? = null,
        active: Boolean = true,
        softDeleted: Boolean = false
    ) = CaseViewEvent(id, personId, eventNumber, failureToComplyCount, breachEndDate, active, softDeleted)
}
