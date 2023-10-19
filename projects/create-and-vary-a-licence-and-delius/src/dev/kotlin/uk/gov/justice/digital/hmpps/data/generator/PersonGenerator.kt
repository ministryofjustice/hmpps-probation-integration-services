package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team

object PersonGenerator {
    val DEFAULT_PERSON = generatePerson("T123456")
    val DEFAULT_CM = generateManager(DEFAULT_PERSON)

    val PERSON_CREATE_LC = generatePerson("L453621")

    fun generatePerson(
        crn: String,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Person(
        crn,
        softDeleted,
        id
    )

    fun generateManager(
        person: Person,
        provider: Provider = ProviderGenerator.DEFAULT_PROVIDER,
        team: Team = ProviderGenerator.DEFAULT_TEAM,
        staff: Staff = StaffGenerator.DEFAULT,
        softDeleted: Boolean = false,
        active: Boolean = true,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonManager(person, provider, team, staff, softDeleted, active, id)
}
