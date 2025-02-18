package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.generateReferenceData
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator.DEFAULT_STAFF
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator.DEFAULT_TEAM
import uk.gov.justice.digital.hmpps.integrations.delius.*

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
    val DEFAULT_PERSON_MANAGER = generatePersonManager(DEFAULT_PERSON)

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

    fun generatePersonManager(
        person: Person,
        team: Team = DEFAULT_TEAM,
        staff: Staff = DEFAULT_STAFF,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonManager(person, team, staff, active, softDeleted, id)
}