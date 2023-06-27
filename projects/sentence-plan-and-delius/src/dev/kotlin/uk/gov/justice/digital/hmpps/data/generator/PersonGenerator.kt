package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.service.Person
import java.time.LocalDate

object PersonGenerator {
    val DEFAULT = generate("X123123")
    val NON_CUSTODIAL = generate("X123124")

    fun generate(crn: String, softDeleted: Boolean = false, id: Long = IdGenerator.getAndIncrement()) =
        Person(
            listOf(),
            "Banner",
            "David",
            "Bruce",
            "",
            id,
            "NOMISID",
            crn,
            LocalDate.now().minusYears(18),
            ReferenceDataGenerator.TIER_1,
            softDeleted
        )
}
