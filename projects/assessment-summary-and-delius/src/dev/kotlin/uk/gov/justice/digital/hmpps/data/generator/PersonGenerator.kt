package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonManager

object PersonGenerator {
    val NO_RISK = generate("N123456")
    val LOW_RISK = generate("L123456")
    val MEDIUM_RISK = generate("M123456")
    val HIGH_RISK = generate("H123456")
    val VERY_HIGH_RISK = generate("V123456")

    fun generate(
        crn: String,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Person(crn, softDeleted, id)

    fun generateManager(
        person: Person,
        probationAreaId: Long = ProviderGenerator.DEFAULT_PROVIDER_ID,
        teamId: Long = ProviderGenerator.DEFAULT_TEAM_ID,
        staffId: Long = ProviderGenerator.DEFAULT_STAFF_ID,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonManager(person, probationAreaId, teamId, staffId, active, softDeleted, id)

    fun generateEvent(
        person: Person,
        number: String = "1",
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Event(number, person.id, active, softDeleted, id)
}