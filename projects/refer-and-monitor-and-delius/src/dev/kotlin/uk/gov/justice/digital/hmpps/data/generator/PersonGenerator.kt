package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.entity.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import java.time.ZonedDateTime

object PersonGenerator {
    val DEFAULT = generate("T140223")
    val FUZZY_SEARCH = generate("F123456")
    val SENTENCED_WITHOUT_NSI = generate("S123456")

    val COMMUNITY_RESPONSIBLE = generate("C025519")
    val COMMUNITY_NOT_RESPONSIBLE = generate("C014150")

    fun generate(crn: String, softDeleted: Boolean = false, id: Long = IdGenerator.getAndIncrement()) =
        Person(crn, softDeleted, id)

    fun generatePersonManager(
        person: Person,
        staff: Staff,
        responsibleOfficer: ResponsibleOfficer? = null,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonManager(person, staff, responsibleOfficer, active, softDeleted, id)

    fun generateResponsibleOfficer(
        communityManager: PersonManager?,
        endDate: ZonedDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = ResponsibleOfficer(communityManager, endDate, id)
}
