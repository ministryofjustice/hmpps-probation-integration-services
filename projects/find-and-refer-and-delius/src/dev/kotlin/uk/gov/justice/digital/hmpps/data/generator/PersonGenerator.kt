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
    val EXCLUSION = generatePerson(
        "E123456",
        nomsNumber = "E4321BA",
        gender = GENDER_MALE,
        ethnicity = ETHNICITY,
        forename = "Ricky",
        secondName = "Brian",
        thirdName = "Alan",
        surname = "Brown",
        exclusionMessage = "There is an exclusion on this person"
    )
    val RESTRICTION = generatePerson(
        "R123456",
        nomsNumber = "R4321BA",
        gender = GENDER_MALE,
        ethnicity = ETHNICITY,
        forename = "William",
        secondName = "James",
        thirdName = "Harold",
        surname = "Wilson",
        restrictionMessage = "There is a restriction on this person"
    )
    val RESTRICTION_EXCLUSION = generatePerson(
        "B123456",
        nomsNumber = "B4321BA",
        gender = GENDER_MALE,
        ethnicity = ETHNICITY,
        forename = "Bob",
        secondName = "Reginald",
        thirdName = "Harold",
        surname = "Jones",
        exclusionMessage = "You are excluded from viewing this case",
        restrictionMessage = "You are restricted from viewing this case"
    )

    private fun generatePerson(
        crn: String,
        nomsNumber: String,
        forename: String,
        secondName: String? = null,
        thirdName: String? = null,
        surname: String,
        gender: ReferenceData,
        ethnicity: ReferenceData,
        exclusionMessage: String? = null,
        restrictionMessage: String? = null,
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
        surname = surname,
        exclusionMessage = exclusionMessage,
        restrictionMessage = restrictionMessage,
    )
}
