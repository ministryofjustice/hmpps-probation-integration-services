package uk.gov.justice.digital.hmpps.service

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Equality
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonAddress

data class InsertPersonResult(
    val person: Person,
    val personManager: PersonManager,
    val equality: Equality,
    var address: PersonAddress?
)