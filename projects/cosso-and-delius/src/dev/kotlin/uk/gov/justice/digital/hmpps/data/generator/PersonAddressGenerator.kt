package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.PersonAddress

object PersonAddressGenerator {
    val DEFAULT_PERSON_MAIN_ADDRESS = PersonAddress(
        id = IdGenerator.getAndIncrement(),
        person = PersonGenerator.DEFAULT_PERSON,
        status = ReferenceDataGenerator.MAIN_ADDRESS_STATUS,
        addressNumber = "1",
        buildingName = "The House",
        streetName = "The Street",
        townCity = "London",
        district = "London",
        postcode = "SW1A 1AA",
        county = "Greater London",
        softDeleted = false
    )
}