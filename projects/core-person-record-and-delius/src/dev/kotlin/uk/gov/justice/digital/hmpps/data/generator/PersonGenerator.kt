package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.integration.delius.entity.*
import java.time.LocalDate
import java.time.LocalDateTime

object PersonGenerator {
    val FULL_PERSON_ID = IdGenerator.getAndIncrement()
    val MIN_PERSON_ID = IdGenerator.getAndIncrement()
    val ETHNICITY = generateReferenceData("ETH")
    val RELIGION = generateReferenceData("REL")
    val RELIGION_HX = generateReferenceData("REL_HX")
    val GENDER = generateReferenceData("GEN")
    val GENDER_IDENTITY = generateReferenceData("GID")
    val NATIONALITY = generateReferenceData("NAT")
    val SECOND_NATIONALITY = NATIONALITY
    val TITLE = generateReferenceData("TIT")
    val PREVIOUS_ADDRESS = generateReferenceData("P", "Previous Address")
    val MAIN_ADDRESS = generateReferenceData("M", "Main Address")
    val SEXUAL_ORIENTATION = generateReferenceData("SEO")
    val DRIVERS_LICENCE = generateReferenceData("DRL", "Drivers Licence")
    val RELIGION_HISTORY =
        generateReligionHistory(
            FULL_PERSON_ID, LocalDate.now().minusDays(30),
            LocalDate.now().minusDays(10)
        )
    val SELF_DESCRIBED_RELIGION_HISTORY =
        generateReligionHistory(
            FULL_PERSON_ID, "Self-described religion", LocalDate.now().minusDays(10),
            LocalDate.now().minusDays(1),
        )

    private fun generateReligionHistory(
        personId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ) =
        ReligionHistory(
            IdGenerator.getAndIncrement(),
            personId = personId,
            startDate = startDate,
            endDate = endDate,
            referenceData = RELIGION_HX
        )

    private fun generateReligionHistory(
        personId: Long,
        religionDescription: String,
        startDate: LocalDate,
        endDate: LocalDate
    ) =
        ReligionHistory(
            IdGenerator.getAndIncrement(),
            personId = personId,
            startDate = startDate,
            endDate = endDate,
            religionDescription = religionDescription
        )

    val MIN_PERSON =
        generatePerson(
            "M123456",
            firstname = "Isabelle",
            surname = "Necessary",
            dateOfBirth = LocalDate.of(1990, 3, 5),
            id = MIN_PERSON_ID
        )
    val FULL_PERSON = generatePerson(
        crn = "F123456",
        nomsId = "A3349EX",
        pnc = "2011/0593710D",
        cro = "89861/11W",
        niNumber = "FJ123456W",
        prisonerNumber = "94600E",
        firstname = "Frederick",
        secondName = "Paul",
        thirdName = "Bernard",
        surname = "Johnson",
        dateOfBirth = LocalDate.of(1975, 7, 15),
        dateOfDeath = LocalDate.of(2015, 8, 15),
        previousSurname = "No Previous",
        preferredName = "Freddy",
        telephoneNumber = "0191 755 4789",
        mobileNumber = "07895746789",
        emailAddress = "fred@gmail.com",
        title = TITLE,
        gender = GENDER,
        genderIdentity = GENDER_IDENTITY,
        genderIdentityDescription = "Self-described gender identity",
        nationality = NATIONALITY,
        secondNationality = SECOND_NATIONALITY,
        ethnicity = ETHNICITY,
        ethnicityDescription = "Self-described ethnicity",
        religion = RELIGION,
        religionDescription = "Self-described faith",
        sexualOrientation = SEXUAL_ORIENTATION,
        exclusionMessage = "This case is excluded because ...",
        restrictionMessage = "This case is restricted because ...",
        id = FULL_PERSON_ID
    )

    val FULL_PERSON_ALIASES = listOf(
        generateAlias(
            FULL_PERSON_ID,
            "Freddy",
            null,
            null,
            "Banter",
            LocalDate.of(1974, 2, 17),
            gender = GENDER
        )
    )

    val FULL_PERSON_RELIGION_HISTORY = listOf(RELIGION_HISTORY, SELF_DESCRIBED_RELIGION_HISTORY)

    val FULL_PERSON_ADDRESSES = listOf(
        generateAddress(
            personId = FULL_PERSON_ID,
            status = MAIN_ADDRESS,
            addressNumber = "1",
            buildingName = null,
            streetName = "Main Street",
            townCity = "",
            county = "   ",
            district = "London",
            postcode = "PC1 1TS",
            uprn = 123456789,
            telephoneNumber = "01234 567890",
            notes = "Some notes about this address",
            startDate = LocalDate.now().minusDays(30),
        ),
        generateAddress(
            personId = FULL_PERSON_ID,
            status = PREVIOUS_ADDRESS,
            postcode = "NF1 1NF",
            noFixedAbode = true,
            startDate = LocalDate.now().minusDays(60),
            endDate = LocalDate.now().minusDays(30),
        )
    )

    val FULL_PERSON_IDENTIFIERS = listOf(
        AdditionalIdentifier(
            id = id(),
            personId = FULL_PERSON_ID,
            type = DRIVERS_LICENCE,
            value = "BANTE707155F99XX",
        )
    )

    val FULL_PERSON_EXCLUSIONS = listOf(
        generateExclusion(FULL_PERSON_ID, "SomeUser1"),
        generateExclusion(FULL_PERSON_ID, "PastEndDatedUser", LocalDateTime.now().minusDays(30)),
    )

    val FULL_PERSON_RESTRICTIONS = listOf(
        generateRestriction(FULL_PERSON_ID, "SomeUser2"),
        generateRestriction(FULL_PERSON_ID, "FutureEndDatedUser", LocalDateTime.now().plusDays(30)),
    )

    val SENTENCES = listOf(
        generateDisposal(FULL_PERSON_ID, LocalDate.of(2024, 8, 7), active = true, softDeleted = false),
        generateDisposal(FULL_PERSON_ID, LocalDate.of(2024, 8, 5), active = false, softDeleted = false),
        generateDisposal(FULL_PERSON_ID, LocalDate.of(2024, 8, 4), active = false, softDeleted = true),
        generateDisposal(FULL_PERSON_ID, LocalDate.of(2024, 8, 3), active = true, softDeleted = false)
    )

    fun generateReferenceData(
        code: String,
        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceData(code, description, id)

    fun generatePerson(
        crn: String,
        nomsId: String? = null,
        pnc: String? = null,
        cro: String? = null,
        niNumber: String? = null,
        prisonerNumber: String? = null,
        firstname: String,
        secondName: String? = null,
        thirdName: String? = null,
        surname: String,
        dateOfBirth: LocalDate,
        dateOfDeath: LocalDate? = null,
        previousSurname: String? = null,
        preferredName: String? = null,
        telephoneNumber: String? = null,
        mobileNumber: String? = null,
        emailAddress: String? = null,
        title: ReferenceData? = null,
        gender: ReferenceData? = null,
        genderIdentity: ReferenceData? = null,
        genderIdentityDescription: String? = null,
        nationality: ReferenceData? = null,
        secondNationality: ReferenceData? = null,
        ethnicity: ReferenceData? = null,
        ethnicityDescription: String? = null,
        religion: ReferenceData? = null,
        religionDescription: String? = null,
        exclusionMessage: String? = null,
        restrictionMessage: String? = null,
        sexualOrientation: ReferenceData? = null,
        softDeleted: Boolean = false,
        id: Long
    ) = Person(
        crn = crn,
        nomsId = nomsId,
        pnc = pnc,
        cro = cro,
        niNumber = niNumber,
        prisonerNumber = prisonerNumber,
        firstName = firstname,
        secondName = secondName,
        thirdName = thirdName,
        surname = surname,
        dateOfBirth = dateOfBirth,
        dateOfDeath = dateOfDeath,
        previousSurname = previousSurname,
        preferredName = preferredName,
        telephoneNumber = telephoneNumber,
        mobileNumber = mobileNumber,
        emailAddress = emailAddress,
        title = title,
        gender = gender,
        genderIdentity = genderIdentity,
        genderIdentityDescription = genderIdentityDescription,
        nationality = nationality,
        secondNationality = secondNationality,
        ethnicity = ethnicity,
        ethnicityDescription = ethnicityDescription,
        religion = religion,
        religionDescription = religionDescription,
        sexualOrientation = sexualOrientation,
        exclusionMessage = exclusionMessage,
        restrictionMessage = restrictionMessage,
        softDeleted = softDeleted,
        id = id,
    )

    fun generateAlias(
        personId: Long,
        firstName: String,
        secondName: String?,
        thirdName: String?,
        surname: String,
        dateOfBirth: LocalDate,
        softDeleted: Boolean = false,
        gender: ReferenceData? = null,
        id: Long = IdGenerator.getAndIncrement(),
    ) = Alias(personId, firstName, secondName, thirdName, surname, dateOfBirth, softDeleted, gender, id)

    fun generateAddress(
        personId: Long,
        status: ReferenceData,
        addressNumber: String? = null,
        buildingName: String? = null,
        streetName: String? = null,
        townCity: String? = null,
        county: String? = null,
        district: String? = null,
        postcode: String? = null,
        uprn: Long? = null,
        startDate: LocalDate,
        noFixedAbode: Boolean = false,
        telephoneNumber: String? = null,
        notes: String? = null,
        endDate: LocalDate? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement(),
    ) = PersonAddress(
        personId = personId,
        status = status,
        addressNumber = addressNumber,
        buildingName = buildingName,
        streetName = streetName,
        townCity = townCity,
        county = county,
        district = district,
        postcode = postcode,
        uprn = uprn,
        noFixedAbode = noFixedAbode,
        telephoneNumber = telephoneNumber,
        notes = notes,
        startDate = startDate,
        endDate = endDate,
        softDeleted = softDeleted,
        id = id,
    )

    private fun generateExclusion(
        personId: Long,
        username: String,
        endDate: LocalDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Exclusion(personId, LimitedAccessUser(username, IdGenerator.getAndIncrement()), endDate, id)

    private fun generateRestriction(
        personId: Long,
        username: String,
        endDate: LocalDateTime? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Restriction(personId, LimitedAccessUser(username, IdGenerator.getAndIncrement()), endDate, id)

    private fun generateDisposal(
        personId: Long,
        startDate: LocalDate,
        active: Boolean,
        softDeleted: Boolean
    ) = Disposal(IdGenerator.getAndIncrement(), personId, startDate, active, softDeleted)
}