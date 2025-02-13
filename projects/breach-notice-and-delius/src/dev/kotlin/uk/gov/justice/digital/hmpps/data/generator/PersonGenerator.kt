package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.Person
import uk.gov.justice.digital.hmpps.integrations.delius.PersonAddress
import uk.gov.justice.digital.hmpps.integrations.delius.ReferenceData

object PersonGenerator {

    val DEFAULT_PERSON = Person(
        crn = "A000001",
        firstName = "First",
        secondName = "Middle",
        thirdName = null,
        surname = "Surname",
        addresses = listOf(),
        softDeleted = false,
        id = IdGenerator.getAndIncrement(),
    )

    val DEFAULT_ADDRESS_TYPE = generateAddressType("ADT1")
    val DEFAULT_ADDRESS = generatePersonAddress(DEFAULT_PERSON, DEFAULT_ADDRESS_TYPE)

    private fun generateAddressType(
        code: String,
        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceData(code, description, id)

    private fun generatePersonAddress(
        person: Person,
        type: ReferenceData?,
        buildingName: String? = "Building Name",
        buildingNumber: String? = "Building Number",
        streetName: String? = "Street Name",
        townCity: String? = "Town / City",
        district: String? = "District",
        county: String? = "County",
        postcode: String? = "PO57 0DE",
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonAddress(
        person,
        type,
        buildingName,
        buildingNumber,
        streetName,
        townCity,
        district,
        county,
        postcode,
        softDeleted,
        id
    )
}