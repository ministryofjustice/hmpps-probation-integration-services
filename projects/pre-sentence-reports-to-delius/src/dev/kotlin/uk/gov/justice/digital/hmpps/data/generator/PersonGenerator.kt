package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.DEFAULT_ADDRESS_STATUS
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.DEFAULT_TITLE
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.PersonAddress
import java.time.LocalDate

object PersonGenerator {
    val DEFAULT_PERSON = Person(
        IdGenerator.getAndIncrement(),
        "X012771",
        DEFAULT_TITLE, "Bob", "Tony", null, "Jones",
        LocalDate.of(1980, 1, 1), false, null, null
    )

    fun getAddress() = PersonAddress(
        IdGenerator.getAndIncrement(), DEFAULT_PERSON, DEFAULT_ADDRESS_STATUS,
        "Building Name", "123", "Street Name", "Town City",
        "District", "County", "AB1 2CD",
        LocalDate.of(2020, 1, 1), null, false
    )
}