package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import java.time.LocalDate

object PersonGenerator {

    val GENDER_MALE = ReferenceData(IdGenerator.getAndIncrement(), "M", "Male")
    val ETHNICITY = ReferenceData(IdGenerator.getAndIncrement(), "X", "White")

    val PERSON_1 = generatePerson(
        crn = "X123456",
        nomsNumber = "A1234AB",
        gender = GENDER_MALE,
        ethnicity = ETHNICITY,
        forename = "John",
        secondName = "Terry",
        thirdName = "David",
        surname = "Smith",
    )

    val PERSON_2 = generatePerson(
        crn = "X654321",
        nomsNumber = "A4321BA",
        gender = GENDER_MALE,
        ethnicity = ETHNICITY,
        forename = "Barry",
        secondName = "Jim",
        thirdName = "Bruce",
        surname = "Wayne",
    )

    private fun generatePerson(
        crn: String,
        nomsNumber: String,
        forename: String,
        secondName: String? = null,
        thirdName: String? = null,
        surname: String,
        gender: ReferenceData,
        ethnicity: ReferenceData
    ) = Person(
        id = IdGenerator.getAndIncrement(),
        crn = crn,
        gender = gender,
        ethnicity = ethnicity,
        nomsNumber = nomsNumber,
        forename = forename,
        secondName = secondName,
        thirdName = thirdName,
        dateOfBirth = LocalDate.now().minusYears(50),
        surname = surname
    )
}