package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.entity.PrisonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.entity.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import java.time.ZonedDateTime

object PersonGenerator {
    val DEFAULT = generate("T140223", "A1234YZ")
    val FUZZY_SEARCH = generate("F123456")
    val SENTENCED_WITHOUT_NSI = generate("S123456")
    val NO_APPOINTMENTS = generate("N049975")

    val COMMUNITY_RESPONSIBLE = generate("C025519")
    val COMMUNITY_NOT_RESPONSIBLE = generate("C014150")

    val EXCLUSION = generate("E123456", exclusionMessage = "There is an exclusion on this person")
    val RESTRICTION = generate("R123456", restrictionMessage = "There is a restriction on this person")
    val RESTRICTION_EXCLUSION = generate(
        "B123456",
        exclusionMessage = "You are excluded from viewing this case",
        restrictionMessage = "You are restricted from viewing this case"
    )

    fun generate(
        crn: String,
        nomsId: String? = null,
        exclusionMessage: String? = null,
        restrictionMessage: String? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Person(crn, nomsId, exclusionMessage, restrictionMessage, softDeleted, id)

    fun generatePersonManager(
        person: Person,
        staff: Staff,
        team: Team,
        responsibleOfficer: ResponsibleOfficer? = null,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonManager(person, staff, team, responsibleOfficer, active, softDeleted, id)

    fun generatePrisonManager(
        person: Person,
        staff: Staff,
        team: Team,
        responsibleOfficer: ResponsibleOfficer? = null,
        emailAddress: String? = "manager@prison.gov.uk",
        telephone: String? = "020 010 3445",
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = PrisonManager(person.id, staff, team, responsibleOfficer, emailAddress, telephone, active, softDeleted, id)

    fun generateResponsibleOfficer(
        communityManager: PersonManager?,
        prisonManager: PrisonManager? = null,
        endDate: ZonedDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = ResponsibleOfficer(communityManager, prisonManager, endDate, id)
}
