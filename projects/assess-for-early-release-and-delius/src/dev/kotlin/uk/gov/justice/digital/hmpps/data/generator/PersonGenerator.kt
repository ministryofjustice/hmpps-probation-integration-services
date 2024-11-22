package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.*

object PersonGenerator {
    val DEFAULT_PERSON = generatePerson("T123456")
    val PERSON_ENDED_TEAM_LOCATION = generatePerson("T123457")
    val DEFAULT_CM = generateManager(DEFAULT_PERSON)
    val CM_ENDED_TEAM_LOCATION =
        generateManager(person = PERSON_ENDED_TEAM_LOCATION, team = ProviderGenerator.TEAM_ENDED_OR_NULL_LOCATIONS)

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
