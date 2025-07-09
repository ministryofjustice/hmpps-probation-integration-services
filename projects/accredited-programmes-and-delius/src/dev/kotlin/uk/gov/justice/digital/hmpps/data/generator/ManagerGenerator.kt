package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Manager
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.Staff
import uk.gov.justice.digital.hmpps.entity.Team

object ManagerGenerator {
    fun generate(person: Person, staff: Staff, team: Team) = Manager(
        id = IdGenerator.getAndIncrement(),
        person = person,
        staff = staff,
        team = team,
        active = true,
        softDeleted = false
    )
}
