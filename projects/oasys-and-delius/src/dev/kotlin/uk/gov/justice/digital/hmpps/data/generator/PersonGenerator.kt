package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integration.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integration.delius.person.entity.PersonDetail
import uk.gov.justice.digital.hmpps.integration.delius.reference.entity.ReferenceData
import java.time.LocalDate

object PersonGenerator {
    val REGISTERED_PERSON =
        generate("R123456", firstName = "Reginald", surname = "Regency", dob = LocalDate.now().minusYears(27))
    val RELEASED_PERSON =
        generate("B123456", firstName = "Barry", surname = "Brown", dob = LocalDate.now().minusYears(39))
    val CUSTODY_PERSON =
        generate("C123456", firstName = "Charles", surname = "Chaplin", dob = LocalDate.now().minusYears(42))

    val GENDER = ReferenceDataGenerator.generate("GEN", "Gender")
    val ETHNICITY = ReferenceDataGenerator.generate("ETH", "Ethnicity")
    val LANGUAGE = ReferenceDataGenerator.generate("LAN", "Language")
    val RELIGION = ReferenceDataGenerator.generate("REL", "Religion")

    val DETAILED_PERSON =
        generate(
            crn = "D123456",
            firstName = "Daniel",
            secondName = "David",
            surname = "Danube",
            dob = LocalDate.now().minusYears(36),
            noms = "D1234YZ",
            pnc = "2011/0593710D",
            cro = "89861/11W",
            gender = GENDER,
            ethnicity = ETHNICITY,
            language = LANGUAGE,
            religion = RELIGION,
            emailAddress = "dan@gmail.com",
            telephoneNumber = "0191 256 7234",
            mobileNumber = "07345617263"
        )

    fun generate(
        crn: String,
        noms: String? = null,
        pnc: String? = null,
        cro: String? = null,
        firstName: String,
        secondName: String? = null,
        thirdName: String? = null,
        surname: String,
        dob: LocalDate,
        telephoneNumber: String? = null,
        mobileNumber: String? = null,
        emailAddress: String? = null,
        gender: ReferenceData? = null,
        ethnicity: ReferenceData? = null,
        language: ReferenceData? = null,
        religion: ReferenceData? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonDetail(
        crn,
        noms,
        pnc,
        cro,
        firstName,
        secondName,
        thirdName,
        surname,
        dob,
        telephoneNumber,
        mobileNumber,
        emailAddress,
        gender,
        ethnicity,
        language,
        religion,
        softDeleted,
        id
    )

    fun generatePerson(
        crn: String,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Person(crn, softDeleted, id)
}

fun PersonDetail.asPerson() = Person(crn, softDeleted, id)