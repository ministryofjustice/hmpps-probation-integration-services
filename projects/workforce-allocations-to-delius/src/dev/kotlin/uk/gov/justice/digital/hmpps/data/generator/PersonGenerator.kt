package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewPerson
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import java.time.LocalDate

object PersonGenerator {
    val DEFAULT = generate("X123456")
    val NEW_PM = generate("T123456")
    val HISTORIC_PM = generate("T223456")
    val CASE_VIEW = forCaseView("C123456")

    fun generate(crn: String, id: Long = IdGenerator.getAndIncrement()) = Person(
        id = id,
        crn = crn,
        forename = "Test",
        secondName = "Test",
        surname = "Test"
    )

    fun forCaseView(
        crn: String,
        pncNumber: String? = null,
        dateOfBirth: LocalDate = LocalDate.now().minusYears(30),
        id: Long = IdGenerator.getAndIncrement()
    ) = CaseViewPerson(
        id,
        crn,
        "Forename",
        "SecondName",
        null,
        "Surname",
        dateOfBirth,
        ReferenceDataGenerator.GENDER_MALE,
        pncNumber = pncNumber
    )
}
