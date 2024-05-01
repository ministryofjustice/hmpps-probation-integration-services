package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonManager

object PersonGenerator {
    val PERSON_OPD_NEW = generatePerson("A000001", "A0001BC")
    val PERSON_MANAGER = generatePersonManager(PERSON_OPD_NEW)
    fun generatePerson(
        crn: String,
        noms: String? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Person(crn, noms, softDeleted, id)

    fun generatePersonManager(
        person: Person,
        providerId: Long = IdGenerator.getAndIncrement(),
        teamId: Long = IdGenerator.getAndIncrement(),
        staffId: Long = IdGenerator.getAndIncrement(),
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonManager(person, providerId, teamId, staffId, active, softDeleted, id)

    fun generateEvent(
        person: Person,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Event(person, active, softDeleted, id)
}
