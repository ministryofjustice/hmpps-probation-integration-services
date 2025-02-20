package uk.gov.justice.digital.hmpps.data.generator.personalDetails

import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.USER
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.ContactAddress
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.PersonAddress
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.PersonDocument
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.PersonalContactEntity
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.Dataset
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

    val EXCLUSION = generatePersonDetails(
        "E123456",
        exclusionMessage = "There is an exclusion on this person"
    )
    val RESTRICTION =
        generatePersonDetails(
            "R123456",
            restrictionMessage = "There is a restriction on this person"
        )
    val RESTRICTION_EXCLUSION = generatePersonDetails(
        "B123456",
        exclusionMessage = "You are excluded from viewing this case",
        restrictionMessage = "You are restricted from viewing this case"
    )

    val ALIAS_1 = generateAlias("Sam", "Edward", "Smith", PERSONAL_DETAILS.id)
    val ALIAS_2 = generateAlias("Joe", "Richard", "Jones", PersonGenerator.OVERVIEW.id)

    val DISABILITY_1_RD = ReferenceData(IdGenerator.getAndIncrement(), "D20", "Some Illness")
    val DISABILITY_2_RD = ReferenceData(IdGenerator.getAndIncrement(), "D20", "Blind")
    val PERSONAL_CIRCUMSTANCE_1_RD = PersonalCircumstanceType(IdGenerator.getAndIncrement(), "E02", "Employed")
    val PERSONAL_CIRCUMSTANCE_SUBTYPE_1 =
        PersonalCircumstanceSubType(IdGenerator.getAndIncrement(), "Full-time employed (30 or more hours per week")
    val PERSONAL_CIRCUMSTANCE_2_RD = PersonalCircumstanceType(IdGenerator.getAndIncrement(), "A20", "Owns house")
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
        """
            Comment added by Harry Kane on 29/10/2024 at 14:39
            Note 1
            ---------------------------------------------------------
            Comment added by Tom Brady on 29/10/2024 at 14:56
            Note 2
        """.trimIndent()
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
        notes = """
            Comment added by Harry Kane on 29/10/2024 at 14:39
            Circumstance Note 1
            ---------------------------------------------------------
            Comment added by Tom Brady on 29/10/2024 at 14:56
            Circumstance Note 2
        """.trimIndent(),
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
        LocalDate.now(),
        RELATIONSHIP_TYPE,
        CONTACT_ADDRESS,
        USER,
        notes = """
            Comment added by Harry Kane on 29/10/2024 at 14:39
            Contact note 1
            ---------------------------------------------------------
            Comment added by Tom Brady on 29/10/2024 at 14:56
            Contact note 2
        """.trimIndent(),
    )

    val ADDRESS_TYPE = Dataset(IdGenerator.getAndIncrement(), "ADDRESS TYPE")
    val ADDRESS_STATUS = Dataset(IdGenerator.getAndIncrement(), "ADDRESS STATUS")

    val PERSON_ADDRESS_STATUS_1 = ReferenceData(IdGenerator.getAndIncrement(), "M", "Main Address", ADDRESS_STATUS.id)
    val PERSON_PREVIOUS_ADDRESS_STATUS =
        ReferenceData(IdGenerator.getAndIncrement(), "P", "Previous Address", ADDRESS_STATUS.id)
    val PERSON_ADDRESS_STATUS_2 =
        ReferenceData(IdGenerator.getAndIncrement(), "A", "Another Address", ADDRESS_STATUS.id)
    val PERSON_ADDRESS_TYPE_1 = ReferenceData(IdGenerator.getAndIncrement(), "T1", "Address type 1", ADDRESS_TYPE.id)
    val PERSON_ADDRESS_TYPE_2 = ReferenceData(IdGenerator.getAndIncrement(), "T2", "Address type 2", ADDRESS_TYPE.id)
    val PERSON_ADDRESS_1 = generatePersonAddress(
        "31",
        "Test Street",
        "Test town",
        "Test County",
        "NE2 56A",
        PERSONAL_DETAILS.id,
        PERSON_ADDRESS_STATUS_1,
        PERSON_ADDRESS_TYPE_1,
        verified = true,
        notes = """
            Comment added by Harry Kane on 29/10/2024 at 14:39
            main address note 1
            ---------------------------------------------------------
            Comment added by Tom Brady on 29/10/2024 at 14:56
            main address note 2
        """.trimIndent()
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
        PERSON_PREVIOUS_ADDRESS_STATUS,
        PERSON_ADDRESS_TYPE_2,
        endDate = LocalDate.now().minusYears(1),
        notes = """
            Comment added by Harry Kane on 29/10/2024 at 14:39
            previous address note 1
            ---------------------------------------------------------
            Comment added by Tom Brady on 29/10/2024 at 14:56
            previous address note 2
        """.trimIndent()
    )

    val NULL_ADDRESS = PersonAddress(
        PERSONAL_DETAILS.id,
        0L,
        PERSON_ADDRESS_STATUS_2,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        LocalDate.now(),
        null,
        null,
        false,
        LocalDate.now(),
        USER.id,
        USER,
        LocalDate.now(),
        0,
        false,
        "Some Notes",
        0,
        null,
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
        verified: Boolean? = null,
        notes: String? = null

    ) = PersonAddress(
        personId = personId,
        id = null,
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
        lastUpdatedUserId = USER.id,
        lastUpdatedUser = USER,
        notes = notes,
    )

    fun generatePersonDetails(
        crn: String,
        forename: String = "TestForename",
        secondName: String = "SecondName",
        surname: String = "Surname",
        preferredName: String = "PreferredName",
        gender: ReferenceData = GENDER_FEMALE,
        religion: ReferenceData = RELIGION_DEFAULT,
        sexualOrientation: ReferenceData = SEXUAL_ORIENTATION,
        language: ReferenceData = LANGUAGE_RD,
        previousSurname: String = "PreviousSurname",
        genderIdentity: ReferenceData = GENDER_IDENTITY_RD,
        genderIdentityDescription: String = "genderIdentityDescription",
        requiresInterpreter: Boolean = false,
        exclusionMessage: String? = null,
        restrictionMessage: String? = null
    ) = Person(
        id = IdGenerator.getAndIncrement(),
        crn = crn,
        pnc = "1964/6108598D",
        noms = "G9566GQ",
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
        exclusionMessage = exclusionMessage,
        restrictionMessage = restrictionMessage
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

    fun generateAlias(forename: String, secondName: String, surname: String, personId: Long) = Alias(
        id = IdGenerator.getAndIncrement(),
        forename = forename,
        secondName = secondName,
        surname = surname,
        personId = personId
    )
}

