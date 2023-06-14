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

    fun generate(
        crn: String,
        nomsId: String? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Person(crn, nomsId, softDeleted, id)

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
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = PrisonManager(person.id, staff, team, responsibleOfficer, emailAddress, active, softDeleted, id)

    fun generateResponsibleOfficer(
        communityManager: PersonManager?,
        prisonManager: PrisonManager? = null,
        endDate: ZonedDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = ResponsibleOfficer(communityManager, prisonManager, endDate, id)
}
