package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Person
import java.time.LocalDate

object PersonGenerator {
    val PERSON1 = Person(
        id = IdGenerator.getAndIncrement(),
        crn = "X123456",
        pnc = null,
        noms = null,
        forename = "John",
        secondName = "Bob",
        surname = "Smith",
        preferredName = null,
        dateOfBirth = LocalDate.of(1980, 1, 1),
        dateOfDeath = null,
        telephoneNumber = null,
        mobileNumber = null,
        emailAddress = null,
        gender = ReferenceDataGenerator.GENDER_MALE,
        sexualOrientation = null,
        genderIdentity = null,
        genderIdentityDescription = null,
    )
}