package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.Person
import uk.gov.justice.digital.hmpps.integrations.delius.PersonAddress
import uk.gov.justice.digital.hmpps.integrations.delius.ReferenceData
import java.time.LocalDate

object PersonGenerator {

    val DEFAULT_PERSON = generatePerson(crn = "A000001")
    val DS_ADDRESS_STATUS = ReferenceDataGenerator.generateDataset(Dataset.ADDRESS_STATUS)
    val DEFAULT_ADDRESS_STATUS = ReferenceDataGenerator.generateReferenceData(DS_ADDRESS_STATUS, "ADS1")
    val DEFAULT_ADDRESS = generatePersonAddress(DEFAULT_PERSON, DEFAULT_ADDRESS_STATUS)
    val END_DATED_ADDRESS =
        generatePersonAddress(DEFAULT_PERSON, DEFAULT_ADDRESS_STATUS, endDate = LocalDate.now().minusDays(1))

    fun generatePerson(
        crn: String,
        id: Long = IdGenerator.getAndIncrement(),
        firstName: String = "First",
        secondName: String? = "Middle",
        thirdName: String? = null,
        surname: String = "Surname",
        addresses: List<PersonAddress> = listOf(),
        dateOfBirth: LocalDate = LocalDate.now().minusYears(30L),
        prisonerNumber: String? = "1234AB",
        softDeleted: Boolean = false
    ) = Person(
        crn = crn,
        id = id,
        firstName = firstName,
        secondName = secondName,
        thirdName = thirdName,
        surname = surname,
        addresses = addresses,
        dateOfBirth = dateOfBirth,
        prisonerNumber = prisonerNumber,
        softDeleted = softDeleted,
    )

    fun generatePersonAddress(
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
        id,
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
        softDeleted
    )
}