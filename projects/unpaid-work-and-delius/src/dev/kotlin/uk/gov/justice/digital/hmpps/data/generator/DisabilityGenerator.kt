package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.DisabilityEntity
import java.time.LocalDate

object DisabilityGenerator {
    val DEFAULT = generate()

    fun generate(id: Long = IdGenerator.getAndIncrement()) = DisabilityEntity(
        id,
        CaseGenerator.DEFAULT,
        null,
        ReferenceDataGenerator.DISABILITY_HEARING,
        "wears hearing aid",
        LocalDate.now().minusMonths(5),
        null
    )
}
