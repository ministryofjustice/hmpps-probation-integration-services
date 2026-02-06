package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.DEFAULT_PERSON
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.DEFAULT_ADDRESS_STATUS
import uk.gov.justice.digital.hmpps.entity.PersonAddress
import java.time.LocalDate

object PersonAddressGenerator {
    val DEFAULT_PERSON_ADDRESS = PersonAddress(
        IdGenerator.getAndIncrement(), DEFAULT_PERSON, DEFAULT_ADDRESS_STATUS,
        "Building Name", "123", "Street Name", "Town City",
        "District", "County", "AB1 2CD",
        LocalDate.of(2020, 1, 1), null, false
    )
}