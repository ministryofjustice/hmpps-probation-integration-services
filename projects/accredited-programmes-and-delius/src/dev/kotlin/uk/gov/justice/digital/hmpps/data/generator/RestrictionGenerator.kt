package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.LimitedAccessPerson
import uk.gov.justice.digital.hmpps.entity.LimitedAccessUser
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.Restriction
import uk.gov.justice.digital.hmpps.entity.staff.User

object RestrictionGenerator {
    fun generate(person: Person, user: User) = Restriction(
        person = LimitedAccessPerson(person.crn, person.exclusionMessage, person.restrictionMessage, person.id),
        user = LimitedAccessUser(user.username, user.id),
        end = null,
        id = id()
    )
}
