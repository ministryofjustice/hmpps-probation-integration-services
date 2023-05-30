package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team

object PersonGenerator {
    val DEFAULT = generate(crn = "A000001")
    fun generate(
        crn: String,
        id: Long = IdGenerator.getAndIncrement()
    ) = Person(id = id, crn = crn)
}

object PersonManagerGenerator {
    fun generate(
        person: Person,
        team: Team = TeamGenerator.generate(),
        staff: Staff = StaffGenerator.generate(teams = listOf(team)),
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonManager(
        id = id,
        personId = person.id,
        staff = staff,
        team = team
    )
}
