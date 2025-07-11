package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.*

object RestrictionGenerator {
    fun generate(person: Person, user: User) = Restriction(
        person = LimitedAccessPerson(person.crn, person.exclusionMessage, person.restrictionMessage, person.id),
        user = LimitedAccessUser(user.username, user.id),
        end = null,
        id = id()
    )
}
