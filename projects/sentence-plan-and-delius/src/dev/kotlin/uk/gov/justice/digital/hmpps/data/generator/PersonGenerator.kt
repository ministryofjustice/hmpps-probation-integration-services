package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.service.Person
import java.time.LocalDate

object PersonGenerator {
    val DEFAULT = generate("X123123")

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
            softDeleted,
            ReferenceDataGenerator.TIER_1
        )
}
