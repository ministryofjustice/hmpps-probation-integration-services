package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.ContactAddress
import uk.gov.justice.digital.hmpps.entity.PersonalContact
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import java.time.LocalDate

object PersonalContactGenerator {
    val CURRENT_EMPLOYER_TYPE = ReferenceData(IdGenerator.getAndIncrement(), "CE", "Current Employer")

    val EMPLOYER_ADDRESS = generateAddress(
        personId = IdGenerator.getAndIncrement(),
        status = AddressGenerator.MAIN_STATUS,
        buildingName = "Employer Building",
        buildingNumber = "1",
        streetName = "Employer Street",
        townCity = "Town City",
        district = "District",
        county = "County",
        postcode = "NE30 3ZZ",
        telephoneNumber = "01912111111",
    )

    val DEFAULT_EMPLOYER = generate(
        personId = PersonGenerator.DEFAULT.id,
        forename = "Billy",
        middleNames = "The",
        surname = "Kid",
        relationshipType = CURRENT_EMPLOYER_TYPE,
        address = EMPLOYER_ADDRESS,
    )

    val ENDED_EMPLOYER = generate(
        personId = PersonGenerator.DEFAULT.id,
        forename = "Old",
        surname = "Employer",
        relationshipType = CURRENT_EMPLOYER_TYPE,
        endDate = LocalDate.now().minusDays(1),
    )

    fun generate(
        personId: Long,
        forename: String,
        middleNames: String? = null,
        surname: String,
        relationshipType: ReferenceData,
        mobileNumber: String? = null,
        address: ContactAddress? = null,
        startDate: LocalDate = LocalDate.now().minusMonths(3),
        endDate: LocalDate? = null,
        id: Long = IdGenerator.getAndIncrement(),
    ) = PersonalContact(
        id = id,
        personId = personId,
        forename = forename,
        middleNames = middleNames,
        surname = surname,
        relationshipType = relationshipType,
        mobileNumber = mobileNumber,
        address = address,
        startDate = startDate,
        endDate = endDate,
    )

    fun generateAddress(
        personId: Long = PersonGenerator.DEFAULT.id,
        status: ReferenceData = AddressGenerator.MAIN_STATUS,
        buildingName: String? = null,
        buildingNumber: String? = null,
        streetName: String? = null,
        townCity: String? = null,
        district: String? = null,
        county: String? = null,
        postcode: String? = null,
        telephoneNumber: String? = null,
        id: Long = IdGenerator.getAndIncrement(),
    ) = ContactAddress(
        id = id,
        personId = personId,
        status = status,
        buildingName = buildingName,
        buildingNumber = buildingNumber,
        streetName = streetName,
        townCity = townCity,
        district = district,
        county = county,
        postcode = postcode,
        telephoneNumber = telephoneNumber,
    )
}
