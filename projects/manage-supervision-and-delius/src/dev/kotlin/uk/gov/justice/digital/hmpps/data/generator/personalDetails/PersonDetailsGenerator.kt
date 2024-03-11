package uk.gov.justice.digital.hmpps.data.generator.personalDetails

import jakarta.persistence.*
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.Disability
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.PersonalCircumstance
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.PersonalCircumstanceSubType
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.Provision
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.awt.ComponentOrientation
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

object PersonDetailsGenerator {
    val GENDER_FEMALE =  ReferenceData(IdGenerator.getAndIncrement(), "F", "Female")
    val RELIGION_DEFAULT =  ReferenceData(IdGenerator.getAndIncrement(), "C", "Christian")
    val SEXUAL_ORIENTATION =  ReferenceData(IdGenerator.getAndIncrement(), "H", "Hetrosexual")

    val PERSONAL_DETAILS = generatePersonDetails("X000005", "Caroline",
        "Louise", "Bloggs", "Caz", GENDER_FEMALE, RELIGION_DEFAULT, SEXUAL_ORIENTATION )

    val DISABILITY_1_RD = ReferenceData(IdGenerator.getAndIncrement(), "D20", "Some Illness")
    val DISABILITY_2_RD = ReferenceData(IdGenerator.getAndIncrement(), "D20", "Blind")
    val PERSONAL_CIRCUMSTANCE_1_RD = ReferenceData(IdGenerator.getAndIncrement(), "E02", "Employed")
    val PERSONAL_CIRCUMSTANCE_SUBTYPE_1 = PersonalCircumstanceSubType(IdGenerator.getAndIncrement(), "Full-time employed (30 or more hours per week")
    val PERSONAL_CIRCUMSTANCE_2_RD = ReferenceData(IdGenerator.getAndIncrement(), "A20", "Owns house")
    val PERSONAL_CIRCUMSTANCE_SUBTYPE_2 = PersonalCircumstanceSubType(IdGenerator.getAndIncrement(), "Has children")
    val PROVISION_1_RD = ReferenceData(IdGenerator.getAndIncrement(), "BB01", "Brail")
    val PROVISION_2_RD = ReferenceData(IdGenerator.getAndIncrement(), "BC20", "Lots of breaks")

    val DISABILITY_1 = Disability(IdGenerator.getAndIncrement(), PERSONAL_DETAILS.id, DISABILITY_1_RD, LocalDate.now().minusDays(1), LocalDate.now().minusDays(1))
    val DISABILITY_2 = Disability(IdGenerator.getAndIncrement(), PERSONAL_DETAILS.id, DISABILITY_1_RD, LocalDate.now().minusDays(2), LocalDate.now().minusDays(2))


    val PROVISION_1 = Provision(IdGenerator.getAndIncrement(), PERSONAL_DETAILS.id, DISABILITY_1_RD, LocalDate.now().minusDays(1), LocalDate.now().minusDays(1))
    val PROVISION_2 = Provision(IdGenerator.getAndIncrement(), PERSONAL_DETAILS.id, DISABILITY_2_RD, LocalDate.now().minusDays(2), LocalDate.now().minusDays(2))

    val PERSONAL_CIRC_1 = PersonalCircumstance(IdGenerator.getAndIncrement(), PERSONAL_DETAILS.id,
        PERSONAL_CIRCUMSTANCE_1_RD, PERSONAL_CIRCUMSTANCE_SUBTYPE_1, LocalDate.now().minusDays(1),LocalDate.now().minusDays(1))
    val PERSONAL_CIRC_2 = PersonalCircumstance(IdGenerator.getAndIncrement(), PERSONAL_DETAILS.id,
        PERSONAL_CIRCUMSTANCE_2_RD, PERSONAL_CIRCUMSTANCE_SUBTYPE_2, LocalDate.now().minusDays(1),LocalDate.now().minusDays(1))

    val RELATIONSHIP_TYPE = ReferenceData(IdGenerator.getAndIncrement(), "FM01", "Family Member")
    val CONTACT_ADDRESS = generateContactAddress("31","Test Steet", "Test town", "Test County", "NE1 56A")
    val PERSONAL_CONTACT_1 = PersonalContact(IdGenerator.getAndIncrement(), "Sam", "Steven","Smith", "Brother", RELATIONSHIP_TYPE, CONTACT_ADDRESS)

    val PERSON_ADDRESS_STATUS = ReferenceData(IdGenerator.getAndIncrement(), "M", "Main Address")
    val PERSON_ADDRESS_TYPE = ReferenceData(IdGenerator.getAndIncrement(), "T1", "Address type 1")
    val PERSON_ADDRESS = generatePersonAddress("31","Test Steet", "Test town", "Test County", "NE2 56A", PERSONAL_DETAILS.id, PERSON_ADDRESS_STATUS, PERSON_ADDRESS_TYPE)

    fun generateContactAddress(addressNumber: String, streetName: String, town: String, county: String, postcode: String, ) = ContactAddress(
        id = IdGenerator.getAndIncrement(),
        buildingName = null,
        addressNumber = addressNumber,
        county = county,
        streetName = streetName,
        district = null,
        town = town,
        postcode = postcode,
        softDeleted = false,
        telephoneNumber = null,
        lastUpdated = LocalDate.now().minusDays(10),

        )

    fun generatePersonAddress(addressNumber: String, streetName: String, town: String, county: String, postcode: String, personId: Long, status: ReferenceData , type: ReferenceData) = PersonAddress(
        personId = personId,
        id = IdGenerator.getAndIncrement(),
        buildingName = null,
        buildingNumber = addressNumber,
        county = county,
        streetName = streetName,
        district = null,
        town = town,
        postcode = postcode,
        softDeleted = false,
        lastUpdated = LocalDate.now().minusDays(10),
        startDate = LocalDate.now().minusDays(10),
        status = status,
        type = type
        )

    fun generatePersonDetails(crn: String, forename: String, secondName: String, surname: String, preferredName: String,
        gender: ReferenceData, religion: ReferenceData, sexualOrientation: ReferenceData) = PersonDetails(
        id = IdGenerator.getAndIncrement(),
        crn = crn,
        pnc = "2017/123400000F",
        forename = forename,
        secondName = secondName,
        surname = surname,
        preferredName = preferredName,
        dateOfBirth = LocalDate.now().minusYears(40),
        telephoneNumber = "0987657432",
        mobileNumber = "07986789351",
        emailAddress = "testemail",
        gender = gender,
        religion = religion,
        sexualOrientation = sexualOrientation,
        personalCircumstances = emptyList(),
        disabilities = emptyList(),
        provisions = emptyList(),
        personalContacts = emptyList()
    )


}

