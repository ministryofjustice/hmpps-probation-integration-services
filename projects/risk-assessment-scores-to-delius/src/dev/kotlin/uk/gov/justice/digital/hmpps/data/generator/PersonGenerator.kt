package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person
import java.time.LocalDate

object PersonGenerator {
    val DEFAULT = generate("X552020", "A1234YZ")

    fun generate(crn: String, nomsId: String? = null, id: Long = IdGenerator.getAndIncrement()) = Person(
        id = id,
        crn = crn,
        nomsId = nomsId,
        forename = "Test",
        secondName = "Test",
        surname = "Test",
        dateOfBirth = LocalDate.now().minusYears(18),
        gender = ReferenceDataGenerator.GENDER_MALE
    )
}
