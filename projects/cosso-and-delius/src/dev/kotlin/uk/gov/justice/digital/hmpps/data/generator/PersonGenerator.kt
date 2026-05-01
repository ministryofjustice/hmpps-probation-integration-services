package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.Person
import java.time.LocalDate

object PersonGenerator {
    val PERSON_NO_MAIN_ADDRESS = Person(
        id = id(),
        crn = "X123456",
        firstName = "Kyle",
        secondName = "Mark",
        thirdName = "David",
        surname = "Smith",
        telephoneNumber = "02234567890",
        mobileNumber = "07700940123",
        title = ReferenceDataGenerator.MR_TITLE,
        dateOfBirth = LocalDate.of(1985, 3, 2),
        emailAddress = "example2@example.com",
    )
    val DEFAULT_PERSON = Person(
        id = id(),
        crn = "X123456",
        firstName = "Bob",
        secondName = "Tom",
        thirdName = "Billy",
        surname = "Jones",
        telephoneNumber = "01234567890",
        mobileNumber = "07700900123",
        title = ReferenceDataGenerator.MR_TITLE,
        dateOfBirth = LocalDate.of(1980, 1, 1),
        emailAddress = "example@example.com",
    )
    val PERSON_IN_PRISON = Person(
        id = id(),
        crn = "X123457",
        firstName = "Bob",
        secondName = "Tom",
        thirdName = "Billy",
        surname = "Jones",
        telephoneNumber = "01234567890",
        mobileNumber = "07700900123",
        title = ReferenceDataGenerator.MR_TITLE,
        dateOfBirth = LocalDate.of(1986, 2, 3),
        emailAddress = "inPrison@example.com",
    )
    val PERSON_WITH_RESPONSIBLE_OFFICER_WITHOUT_USER = Person(
        id = id(),
        crn = "X123459",
        firstName = "Sam",
        secondName = "Alex",
        thirdName = "Lee",
        surname = "Taylor",
        dateOfBirth = LocalDate.of(1988, 4, 5),
    )
}
