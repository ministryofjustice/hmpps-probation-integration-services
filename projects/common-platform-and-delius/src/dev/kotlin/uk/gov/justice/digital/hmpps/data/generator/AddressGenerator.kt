package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonAddress
import java.time.LocalDate

object AddressGenerator {
    val MAIN_ADDRESS = generate(person = PersonGenerator.DEFAULT)

    fun generate(
        id: Long? = IdGenerator.getAndIncrement(),
        person: Person,
        notes: String? = null,
        postcode: String? = null,
    ) = PersonAddress(
        id = id,
        start = LocalDate.now(),
        status = ReferenceDataGenerator.MAIN_ADDRESS_STATUS,
        person = person,
        notes = notes,
        postcode = postcode,
        type = ReferenceDataGenerator.AWAITING_ASSESSMENT
    )
}
