package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integration.delius.entity.*
import java.time.LocalDate
import java.time.LocalDateTime

object PersonGenerator {
    val ETHNICITY = generateReferenceData("ETH")
    val GENDER = generateReferenceData("GEN")
    val NATIONALITY = generateReferenceData("NAT")
    val TITLE = generateReferenceData("TIT")
    val MAIN_ADDRESS = generateReferenceData("M", "Main Address")

    val MIN_PERSON =
        generatePerson("M123456", firstname = "Isabelle", surname = "Necessary", dob = LocalDate.of(1990, 3, 5))
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
        dob = LocalDate.of(1975, 7, 15),
        previousSurname = "No Previous",
        preferredName = "Freddy",
        telephoneNumber = "0191 755 4789",
        mobileNumber = "07895746789",
        emailAddress = "fred@gmail.com",
        title = TITLE,
        gender = GENDER,
        nationality = NATIONALITY,
        ethnicity = ETHNICITY,
        ethnicityDescription = "Description of ethnicity",
        exclusionMessage = "This case is excluded because ...",
        restrictionMessage = "This case is restricted because ..."
    )

    val FULL_PERSON_ALIASES = listOf(
        generateAlias(
            FULL_PERSON.id, "Freddy", null, null, "Banter", LocalDate.of(1974, 2, 17)
        )
    )

    val FULL_PERSON_ADDRESSES = listOf(
        generateAddress(
            FULL_PERSON.id,
            MAIN_ADDRESS,
            "1",
            null,
            "Main Street",
            "",
            "   ",
            "London",
            "PC1 1TS",
            LocalDate.now().minusDays(30)
        )
    )

    val FULL_PERSON_EXCLUSIONS = listOf(
        generateExclusion(FULL_PERSON.id, "SomeUser1"),
        generateExclusion(FULL_PERSON.id, "PastEndDatedUser", LocalDateTime.now().minusDays(30)),
    )

    val FULL_PERSON_RESTRICTIONS = listOf(
        generateRestriction(FULL_PERSON.id, "SomeUser2"),
        generateRestriction(FULL_PERSON.id, "FutureEndDatedUser", LocalDateTime.now().plusDays(30)),
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
        dob: LocalDate,
        previousSurname: String? = null,
        preferredName: String? = null,
        telephoneNumber: String? = null,
        mobileNumber: String? = null,
        emailAddress: String? = null,
        title: ReferenceData? = null,
        gender: ReferenceData? = null,
        nationality: ReferenceData? = null,
        ethnicity: ReferenceData? = null,
        ethnicityDescription: String? = null,
        exclusionMessage: String? = null,
        restrictionMessage: String? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
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
        dob = dob,
        previousSurname = previousSurname,
        preferredName = preferredName,
        telephoneNumber = telephoneNumber,
        mobileNumber = mobileNumber,
        emailAddress = emailAddress,
        title = title,
        gender = gender,
        nationality = nationality,
        ethnicity = ethnicity,
        ethnicityDescription = ethnicityDescription,
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
        id: Long = IdGenerator.getAndIncrement()
    ) = Alias(personId, firstName, secondName, thirdName, surname, dateOfBirth, softDeleted, id)

    fun generateAddress(
        personId: Long,
        status: ReferenceData,
        addressNumber: String?,
        buildingName: String?,
        streetName: String?,
        townCity: String?,
        county: String?,
        district: String?,
        postcode: String?,
        startDate: LocalDate,
        endDate: LocalDate? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonAddress(
        personId,
        status,
        addressNumber,
        buildingName,
        streetName,
        townCity,
        county,
        district,
        postcode,
        startDate,
        endDate,
        softDeleted,
        id,
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
}