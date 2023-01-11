package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import java.time.LocalDate

object PersonGenerator {
    val DEFAULT = generate("X123456")
    val NEW_PM = generate("T123456")
    val HISTORIC_PM = generate("T223456")

    fun generate(crn: String, pncNumber: String? = null, id: Long = IdGenerator.getAndIncrement()) = Person(
        id = id,
        crn = crn,
        forename = "Test",
        secondName = "Test",
        surname = "Test",
        dateOfBirth = LocalDate.now().minusYears(27),
        gender = ReferenceDataGenerator.GENDER_MALE,
        pncNumber = pncNumber
    )
}
