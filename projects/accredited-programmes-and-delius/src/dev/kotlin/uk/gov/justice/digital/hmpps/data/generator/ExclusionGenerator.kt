package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.*

object ExclusionGenerator {
    fun generate(person: Person, user: User) = Exclusion(
        person = LimitedAccessPerson(person.crn, person.exclusionMessage, person.restrictionMessage, person.id),
        user = LimitedAccessUser(user.username, user.id),
        end = null,
        id = id()
    )
}
