package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import java.time.LocalDate

object PersonGenerator {
val TITLE = ReferenceData(IdGenerator.getAndIncrement(), "MR", "Mr")

val DEFAULT = generate(
        crn = "A000001",
        forename = "Billy",
        secondName = "The",
        surname = "Kid",
        dateOfBirth = LocalDate.of(1980, 3, 17),
        telephoneNumber = "01912525252",
        mobileNumber = "07707123456",
        emailAddress = "test@test.com",
        niNumber = "XX000000X",
        title = TITLE,
        )

val NO_OPTIONAL_FIELDS = generate(crn = "A000002")

fun generate(
        crn: String,
        forename: String = "Test",
        secondName: String? = null,
        thirdName: String? = null,
        surname: String = "Person",
        dateOfBirth: LocalDate = LocalDate.of(1990, 1, 1),
telephoneNumber: String? = null,
mobileNumber: String? = null,
emailAddress: String? = null,
niNumber: String? = null,
title: ReferenceData? = null,
id: Long = IdGenerator.getAndIncrement(),
    ) = Person(
        id = id,
        crn = crn,
        forename = forename,
        secondName = secondName,
        thirdName = thirdName,
        surname = surname,
        dateOfBirth = dateOfBirth,
        telephoneNumber = telephoneNumber,
        mobileNumber = mobileNumber,
        emailAddress = emailAddress,
        niNumber = niNumber,
        title = title,
        )
}
