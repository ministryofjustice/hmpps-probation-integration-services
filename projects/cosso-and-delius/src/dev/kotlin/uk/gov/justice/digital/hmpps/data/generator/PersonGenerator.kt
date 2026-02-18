package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Person
import java.time.LocalDate

object PersonGenerator {
    val PERSON_NO_MAIN_ADDRESS = Person(
        offenderId = IdGenerator.getAndIncrement(),
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
        softDeleted = false
    )
    val DEFAULT_PERSON = Person(
        offenderId = IdGenerator.getAndIncrement(),
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
        softDeleted = false
    )
    val PERSON_IN_PRISON = Person(
        offenderId = IdGenerator.getAndIncrement(),
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
        softDeleted = false
    )
}