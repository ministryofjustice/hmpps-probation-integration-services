package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.epf.entity.Person
import uk.gov.justice.digital.hmpps.epf.entity.ReferenceData
import java.time.LocalDate

object PersonGenerator {
    val DEFAULT_GENDER = generateGender("Male")
    val DEFAULT = generate("N123456", "A1234YZ")
    val EXCLUDED = generate("E123456", currentExclusion = true)
    val RESTRICTED = generate("R123456", currentRestriction = true)
    val WITH_RELEASE_DATE = generate("F123456")

    fun generate(
        crn: String,
        nomsId: String? = null,
        currentExclusion: Boolean = false,
        currentRestriction: Boolean = false,
        softDeleted: Boolean = false,
        rsrScore: Double = 10.1,
        id: Long = IdGenerator.getAndIncrement()
    ) = Person(
        id,
        crn,
        nomsId,
        DEFAULT_GENDER,
        LocalDate.now().minusYears(18),
        "David",
        "Bruce",
        "",
        "Banner",
        currentExclusion,
        currentRestriction,
        rsrScore,
        softDeleted
    )

    fun generateGender(
        code: String,
        description: String = "$code description",
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceData(id, code, description)
}
