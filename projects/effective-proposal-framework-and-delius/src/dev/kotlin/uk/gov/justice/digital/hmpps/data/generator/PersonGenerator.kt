package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.epf.entity.Person
import uk.gov.justice.digital.hmpps.epf.entity.ReferenceData
import java.time.LocalDate

object PersonGenerator {
    val DEFAULT_GENDER = generateGender("Male")
    val DEFAULT = generate("N123456")

    fun generate(crn: String, softDeleted: Boolean = false, id: Long = IdGenerator.getAndIncrement()) =
        Person(id, crn, "David", "Bruce", "", "Banner", LocalDate.now().minusYears(18), DEFAULT_GENDER, softDeleted)

    fun generateGender(
        code: String,
        description: String = "$code description",
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceData(id, code, description)
}
