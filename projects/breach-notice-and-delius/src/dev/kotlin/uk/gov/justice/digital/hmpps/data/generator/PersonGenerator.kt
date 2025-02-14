package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.generateReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.Dataset
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

    val DS_ADDRESS_TYPE = ReferenceDataGenerator.generateDataset(Dataset.ADDRESS_TYPE)
    val DEFAULT_ADDRESS_TYPE = generateReferenceData(DS_ADDRESS_TYPE, "ADT1")
    val DEFAULT_ADDRESS = generatePersonAddress(DEFAULT_PERSON, DEFAULT_ADDRESS_TYPE)

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