package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.generateReferenceData
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator.DEFAULT_STAFF
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator.DEFAULT_TEAM
import uk.gov.justice.digital.hmpps.integrations.delius.*
import java.time.LocalDate

object PersonGenerator {

    val DEFAULT_PERSON = generatePerson(crn = "A000001")
    val DEFAULT_PERSON_MANAGER = generatePersonManager(DEFAULT_PERSON)

    val DS_ADDRESS_TYPE = ReferenceDataGenerator.generateDataset(Dataset.ADDRESS_TYPE)
    val DEFAULT_ADDRESS_TYPE = generateReferenceData(DS_ADDRESS_TYPE, "ADT1")
    val DEFAULT_ADDRESS = generatePersonAddress(DEFAULT_PERSON, DEFAULT_ADDRESS_TYPE)
    val END_DATED_ADDRESS =
        generatePersonAddress(DEFAULT_PERSON, DEFAULT_ADDRESS_TYPE, endDate = LocalDate.now().minusDays(1))

    val EXCLUSION = generatePerson("E123456", exclusionMessage = "There is an exclusion on this person")
    val RESTRICTION = generatePerson("R123456", restrictionMessage = "There is a restriction on this person")
    val RESTRICTION_EXCLUSION = generatePerson(
        "B123456",
        exclusionMessage = "You are excluded from viewing this case",
        restrictionMessage = "You are restricted from viewing this case"
    )

    fun generatePerson(
        crn: String,
        firstName: String = "First",
        secondName: String? = "Middle",
        thirdName: String? = null,
        surname: String = "Surname",
        addresses: List<PersonAddress> = listOf(),
        exclusionMessage: String? = null,
        restrictionMessage: String? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Person(
        crn,
        null,
        firstName,
        secondName,
        thirdName,
        surname,
        addresses,
        exclusionMessage,
        restrictionMessage,
        softDeleted,
        id
    )

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
        endDate: LocalDate? = null,
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
        endDate,
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