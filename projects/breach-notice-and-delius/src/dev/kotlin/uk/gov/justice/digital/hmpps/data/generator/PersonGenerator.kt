package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.generateReferenceData
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator.DEFAULT_STAFF
import uk.gov.justice.digital.hmpps.integrations.delius.*
import java.time.LocalDate

object PersonGenerator {

    val DEFAULT_PERSON = generatePerson(crn = "A000001")
    val DEFAULT_PERSON_MANAGER = generatePersonManager(DEFAULT_PERSON)

    val DS_ADDRESS_STATUS = ReferenceDataGenerator.generateDataset(Dataset.ADDRESS_STATUS)
    val DEFAULT_ADDRESS_STATUS = generateReferenceData(DS_ADDRESS_STATUS, "ADS1")
    val DEFAULT_ADDRESS = generatePersonAddress(DEFAULT_PERSON, DEFAULT_ADDRESS_STATUS)
    val END_DATED_ADDRESS =
        generatePersonAddress(DEFAULT_PERSON, DEFAULT_ADDRESS_STATUS, endDate = LocalDate.now().minusDays(1))

    val EXCLUSION = generatePerson("E123456", exclusionMessage = "There is an exclusion on this person")
    val RESTRICTION = generatePerson("R123456", restrictionMessage = "There is a restriction on this person")
    val RESTRICTION_EXCLUSION = generatePerson(
        "B123456",
        exclusionMessage = "You are excluded from viewing this case",
        restrictionMessage = "You are restricted from viewing this case"
    )

    val PSS_PERSON = generatePerson(crn = "P551234")
    val PSS_PERSON_MANAGER = generatePersonManager(PSS_PERSON)

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
        status: ReferenceData?,
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
        status,
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
        staff: Staff = DEFAULT_STAFF,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonManager(person, staff, active, softDeleted, id)
}