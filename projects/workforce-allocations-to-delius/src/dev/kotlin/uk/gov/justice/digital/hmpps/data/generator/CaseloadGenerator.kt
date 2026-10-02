package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.caseload.Caseload
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Team

object CaseloadGenerator {
    val DEFAULT = generate()

    fun generate(
        person: Person = PersonGenerator.DEFAULT,
        staff: Staff = StaffGenerator.DEFAULT,
        team: Team = TeamGenerator.DEFAULT,
        roleCode: String = "OM",
        trustProviderFlag: Boolean = false,
        eventId: Long? = EventGenerator.DEFAULT.id,
        id: Long = IdGenerator.getAndIncrement(),
    ) = Caseload(
        id = id,
        person = person,
        staff = staff,
        team = team,
        roleCode = roleCode,
        trustProviderFlag = trustProviderFlag,
        eventId = eventId,
    )
}

