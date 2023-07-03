package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.service.entity.Person
import uk.gov.justice.digital.hmpps.service.entity.PersonManager

object PersonManagerGenerator {
    val DEFAULT = generate(PersonGenerator.DEFAULT)
    val NON_CUSTODIAL_MANAGER = generate(PersonGenerator.NON_CUSTODIAL)

    fun generate(person: Person, softDeleted: Boolean = false, id: Long = IdGenerator.getAndIncrement()) =
        PersonManager(
            person,
            ProviderGenerator.DEFAULT_STAFF,
            ProviderGenerator.DEFAULT_TEAM,
            softDeleted,
            true,
            id
        )
}
