package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integration.delius.entity.Alias
import uk.gov.justice.digital.hmpps.integration.delius.entity.Person
import uk.gov.justice.digital.hmpps.integration.delius.entity.PersonAddress
import uk.gov.justice.digital.hmpps.integration.delius.entity.ReferenceData
import java.time.LocalDate

object PersonGenerator {
    val ETHNICITY = generateReferenceData("ETH")
    val GENDER = generateReferenceData("GEN")
    val NATIONALITY = generateReferenceData("NAT")
    val TITLE = generateReferenceData("TIT")
    val MAIN_ADDRESS = generateReferenceData("M", "Main Address")

    val MIN_PERSON =
        generatePerson("M123456", firstname = "Isabelle", surname = "Necessary", dob = LocalDate.of(1990, 3, 5))
    val FULL_PERSON = generatePerson(
        "F123456",
        "A3349EX",
        "2011/0593710D",
        "89861/11W",
        "FJ123456W",
        "94600E",
        "Frederick",
        "Paul",
        "Bernard",
        "Johnson",
        LocalDate.of(1975, 7, 15),
        "No Previous",
        "Freddy",
        "0191 755 4789",
        "07895746789",
        "fred@gmail.com",
        TITLE,
        GENDER,
        NATIONALITY,
        ETHNICITY,
        "Description of ethnicity"
    )

    val FULL_PERSON_ALIASES = listOf(
        generateAlias(
            FULL_PERSON.id, "Freddy", null, null, "Banter", LocalDate.of(1974, 2, 17)
        )
    )

    val FULL_PERSON_ADDRESSES = listOf(
        generateAddress(FULL_PERSON.id, MAIN_ADDRESS, "PC1 1TS", LocalDate.now().minusDays(30))
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
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Person(
        crn,
        nomsId,
        pnc,
        cro,
        niNumber,
        prisonerNumber,
        firstname,
        secondName,
        thirdName,
        surname,
        dob,
        previousSurname,
        preferredName,
        telephoneNumber,
        mobileNumber,
        emailAddress,
        title,
        gender,
        nationality,
        ethnicity,
        ethnicityDescription,
        softDeleted,
        id
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
        postcode: String,
        startDate: LocalDate,
        endDate: LocalDate? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonAddress(personId, status, postcode, startDate, endDate, softDeleted, id)
}