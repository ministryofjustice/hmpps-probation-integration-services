package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.caseload.Caseload
import uk.gov.justice.digital.hmpps.integrations.delius.caseload.CaseloadPerson
import uk.gov.justice.digital.hmpps.integrations.delius.caseload.CaseloadStaff
import uk.gov.justice.digital.hmpps.integrations.delius.caseload.CaseloadTeam
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team

object CaseloadGenerator {
    fun generate(
        person: Person,
        team: Team,
        staffInTeam: List<Staff> = listOf(),
        id: Long = IdGenerator.getAndIncrement()
    ) = Caseload(
        id = id,
        person = CaseloadPerson(person.id, person.crn),
        team = CaseloadTeam(team.id, team.code, staffInTeam.map { CaseloadStaff(it.id, it.code) })
    )
}
