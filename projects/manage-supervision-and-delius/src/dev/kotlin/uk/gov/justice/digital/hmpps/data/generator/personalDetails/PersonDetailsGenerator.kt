package uk.gov.justice.digital.hmpps.data.generator.personalDetails

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator.USER
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.LocalDate
import java.time.ZonedDateTime

object PersonDetailsGenerator {
    val GENDER_FEMALE = ReferenceData(IdGenerator.getAndIncrement(), "F", "Female")
    val RELIGION_DEFAULT = ReferenceData(IdGenerator.getAndIncrement(), "C", "Christian")
    val SEXUAL_ORIENTATION = ReferenceData(IdGenerator.getAndIncrement(), "H", "Heterosexual")
    val LANGUAGE_RD = ReferenceData(IdGenerator.getAndIncrement(), "E", "Arabic")
    val GENDER_IDENTITY_RD = ReferenceData(IdGenerator.getAndIncrement(), "GI", "Test Gender Identity")

    val PERSONAL_DETAILS = generatePersonDetails(
        "X000005",
        "Caroline",
        "Louise",
        "Bloggs",
        "Caz",
        GENDER_FEMALE,
        RELIGION_DEFAULT,
        SEXUAL_ORIENTATION,
        LANGUAGE_RD,
        "Smith",
        GENDER_IDENTITY_RD,
        "Some gender description",
        requiresInterpreter = true
    )

    val ALIAS_1 = generateAlias("Sam", "Edward", "Smith", PERSONAL_DETAILS.id)
    val ALIAS_2 = generateAlias("Joe", "Richard", "Jones", PersonGenerator.OVERVIEW.id)

    val DISABILITY_1_RD = ReferenceData(IdGenerator.getAndIncrement(), "D20", "Some Illness")
    val DISABILITY_2_RD = ReferenceData(IdGenerator.getAndIncrement(), "D20", "Blind")
    val PERSONAL_CIRCUMSTANCE_1_RD = ReferenceData(IdGenerator.getAndIncrement(), "E02", "Employed")
    val PERSONAL_CIRCUMSTANCE_SUBTYPE_1 =
        PersonalCircumstanceSubType(IdGenerator.getAndIncrement(), "Full-time employed (30 or more hours per week")
    val PERSONAL_CIRCUMSTANCE_2_RD = ReferenceData(IdGenerator.getAndIncrement(), "A20", "Owns house")
    val PERSONAL_CIRCUMSTANCE_SUBTYPE_2 = PersonalCircumstanceSubType(IdGenerator.getAndIncrement(), "Has children")
    val PROVISION_1_RD = ReferenceData(IdGenerator.getAndIncrement(), "BB01", "Braille")
    val PROVISION_2_RD = ReferenceData(IdGenerator.getAndIncrement(), "BC20", "Lots of breaks")

    val DISABILITY_1 = Disability(
        IdGenerator.getAndIncrement(),
        PERSONAL_DETAILS.id,
        DISABILITY_1_RD,
        LocalDate.now().minusDays(1),
        LocalDate.now().minusDays(1),
        USER,
    )
    val DISABILITY_2 = Disability(
        IdGenerator.getAndIncrement(),
        PERSONAL_DETAILS.id,
        DISABILITY_2_RD,
        LocalDate.now().minusDays(2),
        LocalDate.now().minusDays(2),
        USER,
    )

    val PROVISION_1 = Provision(
        IdGenerator.getAndIncrement(),
        PERSONAL_DETAILS.id,
        PROVISION_1_RD,
        LocalDate.now().minusDays(1),
        LocalDate.now().minusDays(1),
        USER,
    )
    val PROVISION_2 = Provision(
        IdGenerator.getAndIncrement(),
        PERSONAL_DETAILS.id,
        PROVISION_2_RD,
        LocalDate.now().minusDays(2),
        LocalDate.now().minusDays(2),
        USER,
    )

    val PERSONAL_CIRC_1 = PersonalCircumstance(
        IdGenerator.getAndIncrement(),
        PERSONAL_DETAILS.id,
        PERSONAL_CIRCUMSTANCE_1_RD,
        PERSONAL_CIRCUMSTANCE_SUBTYPE_1,
        LocalDate.now().minusDays(1),
        USER,
        notes = "Some Notes",
        evidenced = true,
        LocalDate.now().minusDays(1)
    )
    val PERSONAL_CIRC_2 = PersonalCircumstance(
        IdGenerator.getAndIncrement(),
        PERSONAL_DETAILS.id,
        PERSONAL_CIRCUMSTANCE_2_RD,
        PERSONAL_CIRCUMSTANCE_SUBTYPE_2,
        LocalDate.now().minusDays(1),
        USER,
        notes = "Some Notes",
        evidenced = true,
        LocalDate.now().minusDays(1)
    )

    val PERSONAL_CIRC_PREV = PersonalCircumstance(
        IdGenerator.getAndIncrement(),
        PERSONAL_DETAILS.id,
        PERSONAL_CIRCUMSTANCE_2_RD,
        PERSONAL_CIRCUMSTANCE_SUBTYPE_2,
        LocalDate.now().minusDays(1),
        USER,
        notes = "Previous circumstance Notes",
        evidenced = true,
        LocalDate.now().minusDays(8),
        endDate = LocalDate.now().minusDays(3),
    )

    val RELATIONSHIP_TYPE = ReferenceData(IdGenerator.getAndIncrement(), "FM01", "Family Member")
    val CONTACT_ADDRESS = generateContactAddress("31", "Test Steet", "Test town", "Test County", "NE1 56A")
    val PERSONAL_CONTACT_1 = PersonalContactEntity(
        IdGenerator.getAndIncrement(),
        PERSONAL_DETAILS,
        "Sam",
        "Steven",
        "Smith",
        "Brother",
        "email.test",
        "0897676554",
        LocalDate.now(),
        LocalDate.now(),
        RELATIONSHIP_TYPE,
        CONTACT_ADDRESS,
        USER
    )

    val PERSON_ADDRESS_STATUS_1 = ReferenceData(IdGenerator.getAndIncrement(), "M", "Main Address")
    val PERSON_ADDRESS_STATUS_2 = ReferenceData(IdGenerator.getAndIncrement(), "A", "Another Address")
    val PERSON_ADDRESS_TYPE_1 = ReferenceData(IdGenerator.getAndIncrement(), "T1", "Address type 1")
    val PERSON_ADDRESS_TYPE_2 = ReferenceData(IdGenerator.getAndIncrement(), "T2", "Address type 2")
    val PERSON_ADDRESS_1 = generatePersonAddress(
        "31",
        "Test Street",
        "Test town",
        "Test County",
        "NE2 56A",
        PERSONAL_DETAILS.id,
        PERSON_ADDRESS_STATUS_1,
        PERSON_ADDRESS_TYPE_1,
        verified = true
    )
    val PERSON_ADDRESS_2 = generatePersonAddress(
        "43",
        "Test Avenue",
        "Test town",
        "Test County",
        "NE4 5AN",
        PERSONAL_DETAILS.id,
        PERSON_ADDRESS_STATUS_2,
        PERSON_ADDRESS_TYPE_2,
        verified = true
    )

    val PREVIOUS_ADDRESS = generatePersonAddress(
        "43",
        "Test Avenue",
        "Test town",
        "Test County",
        "NE4 END",
        PERSONAL_DETAILS.id,
        PERSON_ADDRESS_STATUS_2,
        PERSON_ADDRESS_TYPE_2,
        endDate = LocalDate.now().minusYears(1)
    )

    val NULL_ADDRESS = PersonAddress(
        PERSONAL_DETAILS.id,
        PERSON_ADDRESS_STATUS_2,
        PERSON_ADDRESS_TYPE_2,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        telephoneNumber = null,
        LocalDate.now(),
        null,
        true,
        LocalDate.now(),
        USER,
        false,
        IdGenerator.getAndIncrement()
    )

    val DOCUMENT_1 = generateDocument(PERSONAL_DETAILS.id, "A001", "induction.doc", "DOCUMENT")
    val DOCUMENT_2 = generateDocument(PERSONAL_DETAILS.id, "A002", "other.doc", "DOCUMENT")

    fun generateContactAddress(
        addressNumber: String,
        streetName: String,
        town: String,
        county: String,
        postcode: String,
    ) = ContactAddress(
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
        lastUpdatedUser = USER
    )

    fun generatePersonAddress(
        addressNumber: String,
        streetName: String,
        town: String,
        county: String,
        postcode: String,
        personId: Long,
        status: ReferenceData,
        type: ReferenceData,
        endDate: LocalDate? = null,
        verified: Boolean? = null
    ) = PersonAddress(
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
        endDate = endDate,
        status = status,
        type = type,
        typeVerified = verified,
        telephoneNumber = "0191876865",
        lastUpdatedUser = USER
    )

    fun generatePersonDetails(
        crn: String,
        forename: String,
        secondName: String,
        surname: String,
        preferredName: String,
        gender: ReferenceData,
        religion: ReferenceData,
        sexualOrientation: ReferenceData,
        language: ReferenceData,
        previousSurname: String,
        genderIdentity: ReferenceData,
        genderIdentityDescription: String,
        requiresInterpreter: Boolean = false
    ) = Person(
        id = IdGenerator.getAndIncrement(),
        crn = crn,
        pnc = "1964/6108598D",
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
        language = language,
        previousSurname = previousSurname,
        genderIdentity = genderIdentity,
        genderIdentityDescription = genderIdentityDescription,
        requiresInterpreter = requiresInterpreter,
    )

    fun generateDocument(
        personId: Long,
        alfrescoId: String,
        name: String,
        documentType: String,
        primaryKeyId: Long? = null
    ): PersonDocument {
        val doc = PersonDocument()
        doc.id = IdGenerator.getAndIncrement()
        doc.lastUpdated = ZonedDateTime.now().minusDays(1)
        doc.alfrescoId = alfrescoId
        doc.name = name
        doc.personId = personId
        doc.primaryKeyId = primaryKeyId
        doc.type = documentType
        return doc
    }

    fun generateCourtDocument(
        personId: Long,
        alfrescoId: String,
        name: String,
        documentType: String,
        primaryKeyId: Long? = null
    ): CourtReportDocument {
        val doc = CourtReportDocument()
        doc.id = IdGenerator.getAndIncrement()
        doc.lastUpdated = ZonedDateTime.now().minusDays(1)
        doc.alfrescoId = alfrescoId
        doc.name = name
        doc.personId = personId
        doc.primaryKeyId = primaryKeyId
        doc.type = documentType
        return doc
    }

    fun generateEventDocument(
        personId: Long,
        alfrescoId: String,
        name: String,
        documentType: String,
        primaryKeyId: Long? = null
    ): EventDocument {
        val doc = EventDocument()
        doc.id = IdGenerator.getAndIncrement()
        doc.lastUpdated = ZonedDateTime.now().minusDays(3)
        doc.alfrescoId = alfrescoId
        doc.name = name
        doc.personId = personId
        doc.primaryKeyId = primaryKeyId
        doc.type = documentType
        return doc
    }

    fun generateAlias(forename: String, secondName: String, surname: String, personId: Long) = Alias(
        id = IdGenerator.getAndIncrement(),
        forename = forename,
        secondName = secondName,
        surname = surname,
        personId = personId
    )
}

