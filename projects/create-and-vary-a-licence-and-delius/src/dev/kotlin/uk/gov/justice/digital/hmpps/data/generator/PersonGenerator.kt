package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import java.time.ZonedDateTime

object PersonGenerator {
    val DEFAULT_PERSON = generatePerson("T123456")
    val DEFAULT_CM = generateManager()
    val DEFAULT_RO = generateResponsibleOfficer()

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
        provider: Provider = ProviderGenerator.DEFAULT_PROVIDER,
        team: Team = ProviderGenerator.DEFAULT_TEAM,
        staff: Staff = StaffGenerator.DEFAULT,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonManager(provider, team, staff, active, softDeleted, id)

    fun generateResponsibleOfficer(
        person: Person = DEFAULT_PERSON,
        communityManager: PersonManager = DEFAULT_CM,
        startDate: ZonedDateTime = ZonedDateTime.now().minusDays(7),
        endDate: ZonedDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = ResponsibleOfficer(person, communityManager, startDate, endDate, id)
}
